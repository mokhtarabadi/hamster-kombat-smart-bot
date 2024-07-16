package com.example.hamsterkombatbot.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.hamsterkombatbot.MainApplication
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.api.HamsterAPI
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.model.*
import com.example.hamsterkombatbot.model.payload.*
import com.example.hamsterkombatbot.model.response.*
import com.example.hamsterkombatbot.util.HamsterUtil
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlinx.coroutines.delay

class HamsterRepository(currentAccount: Int) {

    companion object {
        private const val TAG = "HamsterRepository"
    }

    private val preferencesHelper = PreferencesHelper(MainApplication.appContext, currentAccount)
    private val hamsterAPI = HamsterAPI(preferencesHelper)

    private val hamsterState: MutableLiveData<HamsterState> = MutableLiveData(HamsterState.Initial)
    private val hamsterEvent: MutableLiveData<HamsterEvent> = MutableLiveData(HamsterEvent.Idle)

    private val currentClicker = MutableLiveData<ClickerUser>()
    private val currentConfig = MutableLiveData<ConfigResponse.ClickerConfig>()
    private val currentCipher = MutableLiveData<DailyCipher>()
    private val currentCombo = MutableLiveData<DailyCombo>()

    private val currentBoosts = MutableLiveData<List<Boost>>()
    private val currentUpgrades = MutableLiveData<List<UpgradeForBuy>>()
    private val currentTasks = MutableLiveData<List<Task>>()

    fun getState(): LiveData<HamsterState> {
        return hamsterState
    }

    fun getEvent(): LiveData<HamsterEvent> {
        return hamsterEvent
    }

    fun getCurrentClicker(): LiveData<ClickerUser> {
        return currentClicker
    }

    fun getCurrentBoosts(): LiveData<List<Boost>> {
        return currentBoosts
    }

    fun getCurrentUpgrades(): LiveData<List<UpgradeForBuy>> {
        return currentUpgrades
    }

    fun getCurrentConfig(): LiveData<ConfigResponse.ClickerConfig> {
        return currentConfig
    }

    fun getCurrentCipher(): LiveData<DailyCipher> {
        return currentCipher
    }

    fun getCurrentCombo(): LiveData<DailyCombo> {
        return currentCombo
    }

    fun getCurrentTasks(): LiveData<List<Task>> {
        return currentTasks
    }

    suspend fun init() {
        hamsterState.value = HamsterState.Loading

        getMeTelegram()
        getConfig()
        sync()
        upgradesForBuy()
        boostsForBuy()
        listTasks()

        hamsterState.value = HamsterState.Ready
    }

    suspend fun refresh() {
        hamsterState.value = HamsterState.Loading

        sync()
        hamsterState.value = HamsterState.Ready

        if (
            hamsterEvent.value !is HamsterEvent.Wait && hamsterEvent.value !is HamsterEvent.Cooldown
        ) {
            hamsterEvent.value = HamsterEvent.Idle
        }
    }

    suspend fun autoTapper() {
        val min = preferencesHelper.getAutoTapMin()
        val max = preferencesHelper.getAutoTapMax()
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        while (currentClicker.value?.availableTaps!! > 0) {
            var random = (min..max).random()

            if (random > currentClicker.value?.availableTaps!!) {
                random = currentClicker.value?.availableTaps!!
            }

            tap(currentClicker.value?.availableTaps!! - random, random)

            val randomSleep = (delayMin..delayMax).random()
            hamsterEvent.value = HamsterEvent.Wait(randomSleep)
            delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))
        }

        hamsterEvent.value = HamsterEvent.Idle
    }

    suspend fun autoCombo() {
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        val currentCombo = currentCombo.value
        if (currentCombo == null) {
            hamsterEvent.value =
                HamsterEvent.Error(MainApplication.appContext.getString(R.string.no_combo_found))
            return
        }

        if (currentCombo.isClaimed) {
            hamsterEvent.value = HamsterEvent.ComboClaimed
            return
        }

        if (currentCombo.upgradeIds.size < 3) {
            hamsterEvent.value = HamsterEvent.ComboUpgradesEmpty
            return
        }

        claimDailyCombo()
        val randomSleep = (delayMin..delayMax).random()
        hamsterEvent.value = HamsterEvent.Wait(randomSleep)
        delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))
        hamsterEvent.value = HamsterEvent.Idle
    }

    suspend fun autoBoost() {
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        val maxBalanceUsage = preferencesHelper.getBalancePercentagePerAutoBoost() / 100.0

        // check for referral
        // addReferral()

        autoSelectExchange()
        checkForMorseCode()
        checkForTasks()
        checkForDailyTask()

        var remainingBalance = currentClicker.value?.balanceCoins?.times(maxBalanceUsage)

        val itemsCanBoots =
            currentBoosts.value?.filter { it.cooldownSeconds == 0 && it.id != "BoostEarnPerTap" }

        if (itemsCanBoots?.isEmpty() == true) {
            hamsterEvent.value = HamsterEvent.BoostsEmpty
            return
        }

        val boostsToBuy = mutableListOf<Boost>()

        // Buy cards based on remaining balance
        itemsCanBoots?.let { boostList ->
            boostList.forEach {
                Log.d(TAG, "available: ${it.id}")
                if (remainingBalance != null) {
                    if (remainingBalance >= it.price) {
                        remainingBalance -= it.price
                        boostsToBuy.add(it)
                        Log.d(TAG, "buying: ${it.id}")
                    }
                }
            }
        }

        Log.d(TAG, "remainingBalance: $remainingBalance")

        if (boostsToBuy.isEmpty()) {
            hamsterEvent.value = HamsterEvent.BalanceNotEnough
            return
        }

        boostsToBuy.forEach {
            buyBoost(it.id)
            val randomSleep = (delayMin..delayMax).random()
            hamsterEvent.value = HamsterEvent.Wait(randomSleep)
            delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))
        }

        hamsterEvent.value = HamsterEvent.Idle
    }

    suspend fun smartUpgrade() {
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        val maxBalanceUsage = preferencesHelper.getBalancePercentagePerSmartBuy() / 100.0
        var remainingBalance = currentClicker.value?.balanceCoins?.times(maxBalanceUsage)

        val maxCooldownSeconds = preferencesHelper.getMaxCooldownWaitMinutes() * 60

        if (remainingBalance == null) {
            return
        }

        while (true) {
            // check for balance
            if (remainingBalance <= 0) {
                hamsterEvent.value = HamsterEvent.BalanceNotEnough
                Log.d(TAG, "Balance not enough")
                break
            }

            // Filter available cards
            val cardToBuy =
                currentUpgrades.value
                    ?.filter {
                        it.isAvailable &&
                            !it.isExpired &&
                            it.profitPerHourDelta > 0 &&
                            it.price <= remainingBalance
                    }
                    ?.sortedByDescending { it.profitPerHourDelta.toDouble() / it.price }
                    ?.firstOrNull()

            if (cardToBuy == null) {
                hamsterEvent.value = HamsterEvent.UpgradesEmpty
                Log.d(TAG, "Upgrades empty")
                break
            }

            val cooldown = cardToBuy.cooldownSeconds
            if (cooldown > maxCooldownSeconds) {
                hamsterEvent.value = HamsterEvent.CooldownTooMuch(cooldown / 60)
                Log.d(TAG, "cooldown greater than maxCooldownSeconds")
                break
            }

            if (cooldown > 0) {
                hamsterEvent.value = HamsterEvent.Cooldown(cooldown / 60)
                Log.d(TAG, "Cooldown: $cooldown")
                delay(TimeUnit.SECONDS.toMillis(cooldown.toLong().plus(3)))
            }

            // Buy cards based on remaining balance
            Log.d(TAG, "buying: ${cardToBuy.id}")
            buyUpgrade(cardToBuy.id)
            val randomSleep = (delayMin..delayMax).random()
            hamsterEvent.value = HamsterEvent.Wait(randomSleep)
            delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))

            remainingBalance -= cardToBuy.price
            Log.d(TAG, "remainingBalance: $remainingBalance")
        }
    }

    private suspend fun getMeTelegram() {
        val response = hamsterAPI.meTelegram()
        if (response == null) {
            hamsterState.value =
                HamsterState.Error(
                    MainApplication.appContext.getString(R.string.error_getting_user_details)
                )
        } else {
            if (response.status != "Ok") {
                hamsterState.value =
                    HamsterState.Error(
                        MainApplication.appContext.getString(R.string.need_re_login_description)
                    )
            }

            hamsterState.value = HamsterState.GetUserDetails(response.telegramUser)
        }
    }

    private suspend fun getConfig() {
        val response = hamsterAPI.getConfig()
        if (response == null) {
            hamsterState.value =
                HamsterState.Error(
                    MainApplication.appContext.getString(R.string.error_getting_config)
                )
        } else {
            hamsterState.value = HamsterState.GetConfig(response)
            currentConfig.value = response.clickerConfig
            currentCipher.value = response.dailyCipher

            // currentBoosts.value = response.clickerConfig.boosts
            // currentTasks.value = response.clickerConfig.tasks
            // currentUpgrades.value = response.clickerConfig.upgrades
        }
    }

    private suspend fun sync() {
        val response = hamsterAPI.sync()
        if (response == null) {
            hamsterState.value =
                HamsterState.Error(MainApplication.appContext.getString(R.string.error_syncing))
        } else {
            hamsterState.value = HamsterState.Sync(response)
            currentClicker.value = response.clickerUser
        }
    }

    private suspend fun addReferral() {
        val response = hamsterAPI.addReferral(AddReferralPayload(1247026399))
        if (response == null) {
            Log.e(TAG, "Add referral failed")
        } else {
            Log.d(TAG, response.toString())
        }
    }

    private suspend fun upgradesForBuy() {
        val response = hamsterAPI.upgradesForBuy()
        if (response == null) {
            hamsterState.value =
                HamsterState.Error(
                    MainApplication.appContext.getString(R.string.error_getting_upgrades_for_buy)
                )
        } else {
            hamsterState.value = HamsterState.UpgradesForBuy(response)
            currentUpgrades.value = response.upgradesForBuy
            currentCombo.value = response.dailyCombo
        }
    }

    private suspend fun refreshUpgradesForBuy() {
        val response = hamsterAPI.upgradesForBuy()
        if (response != null) {
            currentUpgrades.value = response.upgradesForBuy
            currentCombo.value = response.dailyCombo
        } else {
            Log.e(TAG, "Failed to refresh upgrades for buy")
        }
    }

    private suspend fun boostsForBuy() {
        val response = hamsterAPI.boostsForBuy()
        if (response == null) {
            hamsterState.value =
                HamsterState.Error(
                    MainApplication.appContext.getString(R.string.error_getting_boosts_for_buy)
                )
        } else {
            hamsterState.value = HamsterState.BoostsForBuy(response)
            currentBoosts.value = response.boostsForBuy
        }
    }

    private suspend fun listTasks() {
        val response = hamsterAPI.listTasks()
        if (response == null) {
            hamsterState.value =
                HamsterState.Error(
                    MainApplication.appContext.getString(R.string.error_getting_tasks)
                )
        } else {
            hamsterState.value = HamsterState.ListTasks(response)
            currentTasks.value = response.tasks
        }
    }

    private suspend fun tap(availableTaps: Int, count: Int) {
        val response =
            hamsterAPI.tap(
                TapPayload(availableTaps, count, (System.currentTimeMillis() / 1000).toInt())
            )
        if (response == null) {
            hamsterEvent.value =
                HamsterEvent.Error(MainApplication.appContext.getString(R.string.error_tapping))
        } else {
            hamsterEvent.value = HamsterEvent.Tap(response)
            currentClicker.value = response.clickerUser
        }
    }

    private suspend fun buyUpgrade(upgradeId: String) {
        val response =
            hamsterAPI.buyUpgrade(BuyUpgradePayload(System.currentTimeMillis(), upgradeId))
        if (response == null) {
            hamsterEvent.value =
                HamsterEvent.Error(
                    MainApplication.appContext.getString(R.string.error_buying_upgrade)
                )
        } else {
            hamsterEvent.value = HamsterEvent.BuyUpgrade(response)
            currentClicker.value = response.clickerUser
            currentUpgrades.value = response.upgradesForBuy
            currentCombo.value = response.dailyCombo
        }
    }

    private suspend fun buyBoost(boostId: String) {
        val response =
            hamsterAPI.buyBoost(
                BuyBoostPayload((System.currentTimeMillis() / 1000).toInt(), boostId)
            )
        if (response == null) {
            hamsterEvent.value =
                HamsterEvent.Error(
                    MainApplication.appContext.getString(R.string.error_buying_boost)
                )
        } else {
            hamsterEvent.value = HamsterEvent.BuyBoost(response)
            currentClicker.value = response.clickerUser
            currentBoosts.value = response.boostsForBuy
        }
    }

    private suspend fun claimDailyCombo() {
        val response = hamsterAPI.claimDailyCombo()
        if (response == null) {
            hamsterEvent.value =
                HamsterEvent.Error(
                    MainApplication.appContext.getString(R.string.error_claiming_daily_combo)
                )
        } else {
            hamsterEvent.value = HamsterEvent.ClaimDailyCombo(response)
            currentClicker.value = response.clickerUser
            currentCombo.value = response.dailyCombo
        }
    }

    private suspend fun autoSelectExchange() {
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        // first check if user has an exchange if not select one
        if (currentClicker.value?.exchangeId.isNullOrEmpty()) {
            val exchanges = currentConfig.value?.exchanges?.sortedBy { it.bonus }
            val exchange = exchanges?.first()

            if (exchange == null) {
                hamsterEvent.value =
                    HamsterEvent.Error(
                        MainApplication.appContext.getString(R.string.no_exchange_found)
                    )
                return
            }

            val response = hamsterAPI.selectExchange(SelectExchangePayload(exchange.id))
            if (response == null) {
                hamsterEvent.value =
                    HamsterEvent.Error(
                        MainApplication.appContext.getString(R.string.error_selecting_exchange)
                    )
                return
            }

            hamsterEvent.value = HamsterEvent.SelectExchange(response)
            currentClicker.value = response.clickerUser

            // wait for selectExchange
            val randomSleep = (delayMin..delayMax).random()
            hamsterEvent.value = HamsterEvent.Wait(randomSleep)
            delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))
        }
    }

    private suspend fun checkForMorseCode() {
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        // check for morse codes
        currentCipher.value?.let {
            if (it.isClaimed) {
                return
            }

            val morseCode = it.cipher
            if (morseCode.isNotEmpty()) {
                val response =
                    hamsterAPI.claimDailyCipher(
                        ClaimDailyCipherPayload(HamsterUtil.cipherDecode(morseCode))
                    )
                if (response == null) {
                    hamsterEvent.value =
                        HamsterEvent.Error(
                            MainApplication.appContext.getString(R.string.error_checking_morse_code)
                        )
                    return
                }
                hamsterEvent.value = HamsterEvent.CheckMorseCode(response)
                currentClicker.value = response.clickerUser
                currentCipher.value = response.dailyCipher

                // wait for morse code
                val randomSleep = (delayMin..delayMax).random()
                hamsterEvent.value = HamsterEvent.Wait(randomSleep)
                delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))
            }
        }
    }

    private suspend fun checkForDailyTask() {
        val task = currentTasks.value?.firstOrNull { it.id == "streak_days" && !it.isCompleted }
        if (task == null) {
            hamsterEvent.value =
                HamsterEvent.Error(
                    MainApplication.appContext.getString(R.string.no_daily_task_found)
                )
            return
        }

        val response = hamsterAPI.checkTask(CheckTaskPayload(task.id))
        if (response == null) {
            hamsterEvent.value =
                HamsterEvent.Error(
                    MainApplication.appContext.getString(R.string.error_checking_daily_task)
                )
            return
        }

        refreshUpgradesForBuy()

        hamsterEvent.value = HamsterEvent.CheckTask(response)
        currentClicker.value = response.clickerUser
    }

    private suspend fun checkForTasks() {
        val delayMin = preferencesHelper.getMinDelay()
        val delayMax = preferencesHelper.getMaxDelay()

        // check tasks
        val tasks =
            currentTasks.value?.filter {
                it.periodicity == "Once" && !it.isCompleted && it.id != "invite_friends"
            }

        if (tasks?.isEmpty() == true) {
            hamsterEvent.value = HamsterEvent.TasksEmpty
            return
        }

        tasks?.forEach {
            val response = hamsterAPI.checkTask(CheckTaskPayload(it.id))
            if (response == null) {
                hamsterEvent.value =
                    HamsterEvent.Error(
                        MainApplication.appContext.getString(R.string.error_checking_task)
                    )
                return
            }
            hamsterEvent.value = HamsterEvent.CheckTask(response)
            currentClicker.value = response.clickerUser

            refreshUpgradesForBuy()

            // wait for task
            val randomSleep = (delayMin..delayMax).random()
            hamsterEvent.value = HamsterEvent.Wait(randomSleep)
            delay(TimeUnit.SECONDS.toMillis(randomSleep.toLong()))
        }
    }

    sealed class HamsterState {
        // initial
        data object Initial : HamsterState()

        // loading
        data object Loading : HamsterState()

        // error
        data class Error(val message: String) : HamsterState()

        // get user details
        data class GetUserDetails(val user: MeTelegramResponse.TelegramUser) : HamsterState()

        // get config
        data class GetConfig(val config: ConfigResponse) : HamsterState()

        // sync
        data class Sync(val sync: SyncResponse) : HamsterState()

        // upgrades for buy
        data class UpgradesForBuy(val upgrades: UpgradesForBuyResponse) : HamsterState()

        // boosts for buy
        data class BoostsForBuy(val boosts: BoostsForBuyResponse) : HamsterState()

        // list tasks
        data class ListTasks(val tasks: ListTasksResponse) : HamsterState()

        // ready
        data object Ready : HamsterState()
    }

    sealed class HamsterEvent {
        // idle
        data object Idle : HamsterEvent()

        // loading
        data object Loading : HamsterEvent()

        data object BoostsEmpty : HamsterEvent()

        // wait
        data class Wait(val delay: Int) : HamsterEvent()

        // cooldown
        data class Cooldown(val cooldown: Int) : HamsterEvent()

        // cooldown too much
        data class CooldownTooMuch(val cooldown: Int) : HamsterEvent()

        // error
        data class Error(val message: String) : HamsterEvent()

        // tap
        data class Tap(val tap: TapResponse) : HamsterEvent()

        // buy upgrade
        data class BuyUpgrade(val buyUpgrade: BuyUpgradeResponse) : HamsterEvent()

        // buy boost
        data class BuyBoost(val buyBoost: BuyBoostResponse) : HamsterEvent()

        // select exchange
        data class SelectExchange(val selectExchange: SelectExchangeResponse) : HamsterEvent()

        // check morse code
        data class CheckMorseCode(val checkMorseCode: ClaimDailyCipherResponse) : HamsterEvent()

        // check task
        data class CheckTask(val checkTask: CheckTaskResponse) : HamsterEvent()

        // tasks empty
        data object TasksEmpty : HamsterEvent()

        // upgrades empty
        data object UpgradesEmpty : HamsterEvent()

        // balance not enough
        data object BalanceNotEnough : HamsterEvent()

        // combo empty
        data object ComboUpgradesEmpty : HamsterEvent()

        // combo claimed
        data object ComboClaimed : HamsterEvent()
        // claim daily combo
        data class ClaimDailyCombo(val claimDailyCombo: DailyClaimComboResponse) : HamsterEvent()
    }
}
