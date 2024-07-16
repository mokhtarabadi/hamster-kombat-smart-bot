package com.example.hamsterkombatbot.service

import ValidateFailedResponse
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.hamsterkombatbot.R
import com.example.hamsterkombatbot.activity.HamsterActivity
import com.example.hamsterkombatbot.api.LicenseAPI
import com.example.hamsterkombatbot.helper.PreferencesHelper
import com.example.hamsterkombatbot.model.ClickerUser
import com.example.hamsterkombatbot.repository.HamsterRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

class MainForegroundService : Service() {

    companion object {
        private const val TAG = "ForegroundService"

        const val PING_ACTION = "com.example.service.ping"
        const val PONG_RESPONSE_ACTION = "com.example.service.pong.response"
        const val STOP_ACTION = "com.example.service.stop"
    }

    private val notificationId = 123
    private val channelId = "foreground_service_channel"
    private val coroutineScope = SupervisorJob()

    private var currentAccount = 1

    private val pingReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == PING_ACTION) {
                    val responseIntent =
                        Intent(this@MainForegroundService, HamsterActivity::class.java)
                    responseIntent.action = PONG_RESPONSE_ACTION
                    responseIntent.putExtra("isRunning", true)

                    LocalBroadcastManager.getInstance(this@MainForegroundService)
                        .sendBroadcast(responseIntent)

                    // Log.d(TAG, "Service pinged")
                } else if (intent.action == STOP_ACTION) {
                    Log.d(TAG, "Stopping service")
                    coroutineScope.cancel()
                    stopSelf()
                }
            }
        }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(TAG, "Coroutine Error: $throwable")
        updateNotification(getString(R.string.background_service_error, throwable.message))
    }

    private lateinit var notification: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()

        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_ACTION)
        intentFilter.addAction(PING_ACTION)
        localBroadcastManager.registerReceiver(pingReceiver, intentFilter)

        createNotificationChannel()

        val notificationIntent =
            Intent(this, HamsterActivity::class.java) // Replace with your activity class
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.hamster_kombat_service))
                .setContentText(getString(R.string.running))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.hamster) // Replace with your icon drawable
                .setOngoing(true)
                .setWhen(0)
                .setAutoCancel(true)
                .setSilent(true)

        startForeground(notificationId, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val job = SupervisorJob()
        val coroutineScope = CoroutineScope(Dispatchers.IO + job + coroutineExceptionHandler)

        coroutineScope.launch {
            while (isActive) {
                Log.d(TAG, "Current account: $currentAccount")
                withContext(Dispatchers.Main) { startLoop(currentAccount) }

                currentAccount += 1
                if (currentAccount == 4) {
                    val waitTime = PreferencesHelper.getRoundDelayBackground()
                    val nextRound =
                        System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(waitTime.toLong())
                    val formattedTime =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(nextRound)

                    updateNotification(
                        getString(R.string.wait_for_minutes_next_round, waitTime, formattedTime)
                    )
                    delay(TimeUnit.MINUTES.toMillis(waitTime.toLong()))
                    currentAccount = 1
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pingReceiver)
        Log.d(TAG, "onDestroy")
    }

    private fun updateNotification(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            notification
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            startForeground(notificationId, notification.build())
        }
    }

    private fun updateStatus(
        event: HamsterRepository.HamsterEvent?,
        state: HamsterRepository.HamsterState?,
        clickerUser: ClickerUser?
    ) {
        val eventText =
            when (event) {
                HamsterRepository.HamsterEvent.BalanceNotEnough -> {
                    getString(R.string.balance_not_enough)
                }
                HamsterRepository.HamsterEvent.BoostsEmpty -> {
                    getString(R.string.no_boosts)
                }
                is HamsterRepository.HamsterEvent.BuyBoost -> {
                    getString(R.string.boost_bought)
                }
                is HamsterRepository.HamsterEvent.BuyUpgrade -> {
                    getString(R.string.upgrade_bought)
                }
                is HamsterRepository.HamsterEvent.CheckMorseCode -> {
                    getString(R.string.check_morse_code)
                }
                is HamsterRepository.HamsterEvent.CheckTask -> {
                    getString(R.string.check_task)
                }
                is HamsterRepository.HamsterEvent.ClaimDailyCombo -> {
                    getString(R.string.combo_claimed)
                }
                HamsterRepository.HamsterEvent.ComboClaimed -> {
                    getString(R.string.combo_already_claimed_service)
                }
                HamsterRepository.HamsterEvent.ComboUpgradesEmpty -> {
                    getString(R.string.combo_upgrades_empty)
                }
                is HamsterRepository.HamsterEvent.Error -> {
                    getString(R.string.error, event.message)
                }
                HamsterRepository.HamsterEvent.Idle -> {
                    getString(R.string.idle)
                }
                HamsterRepository.HamsterEvent.Loading -> {
                    getString(R.string.loading)
                }
                is HamsterRepository.HamsterEvent.SelectExchange -> {
                    getString(R.string.select_exchange)
                }
                is HamsterRepository.HamsterEvent.Tap -> {
                    getString(R.string.tap_done)
                }
                HamsterRepository.HamsterEvent.TasksEmpty -> {
                    getString(R.string.no_tasks)
                }
                HamsterRepository.HamsterEvent.UpgradesEmpty -> {
                    getString(R.string.upgrades_empty_service)
                }
                is HamsterRepository.HamsterEvent.Wait -> {
                    getString(R.string.waiting_for_seconds, event.delay)
                }
                is HamsterRepository.HamsterEvent.Cooldown -> {
                    getString(R.string.cooldown_wait, event.cooldown)
                }
                is HamsterRepository.HamsterEvent.CooldownTooMuch -> {
                    getString(R.string.cooldown_too_much, event.cooldown)
                }
                else -> {
                    getString(R.string.unknown_state)
                }
            }

        val stateText =
            when (state) {
                is HamsterRepository.HamsterState.BoostsForBuy -> {
                    getString(R.string.get_list_boost_items)
                }
                is HamsterRepository.HamsterState.Error -> {
                    getString(R.string.error, state.message)
                }
                is HamsterRepository.HamsterState.GetConfig -> {
                    getString(R.string.get_config)
                }
                is HamsterRepository.HamsterState.GetUserDetails -> {
                    getString(R.string.get_user_details)
                }
                HamsterRepository.HamsterState.Initial -> {
                    getString(R.string.starting_for_account, currentAccount)
                }
                is HamsterRepository.HamsterState.ListTasks -> {
                    getString(R.string.get_list_of_tasks)
                }
                HamsterRepository.HamsterState.Loading -> {
                    getString(R.string.loading)
                }
                HamsterRepository.HamsterState.Ready -> {
                    getString(R.string.hamster_ready)
                }
                is HamsterRepository.HamsterState.Sync -> {
                    getString(R.string.synced)
                }
                is HamsterRepository.HamsterState.UpgradesForBuy -> {
                    getString(R.string.get_upgrades_list)
                }
                else -> {
                    getString(R.string.unknown_state)
                }
            }

        updateNotification(
            getString(
                    R.string.service_notification_text,
                    clickerUser?.availableTaps,
                    clickerUser?.maxTaps,
                    clickerUser?.balanceCoins?.toInt(),
                    clickerUser?.earnPassivePerHour,
                    clickerUser?.earnPerTap,
                    clickerUser?.level,
                    stateText,
                    eventText
                )
                .trimIndent()
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(
                        channelId,
                        getString(R.string.foreground_service_channel),
                        importance
                    )
                    .apply { description = getString(R.string.foreground_service_description) }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private suspend fun startLoop(accountNumber: Int) {
        val preferencesHelper = PreferencesHelper(this, accountNumber)
        if (preferencesHelper.getToken()?.isEmpty() == true) {
            Log.d(TAG, "No token, skipping")
            updateNotification(getString(R.string.no_token_skipping))
            return
        }

        val licenseToken = preferencesHelper.getLicenseToken()
        if (licenseToken?.isEmpty() == true) {
            Log.d(TAG, "No license token, skipping")
            updateNotification(getString(R.string.no_license_token_skipping))
            return
        }

        val licenseAPI = LicenseAPI()
        val validateResponse = licenseAPI.validateLicense(licenseToken!!)
        if (validateResponse is ValidateFailedResponse) {
            Log.d(TAG, "Error validating license: ${validateResponse.message}")
            updateNotification(
                getString(R.string.error_validating_license, validateResponse.message)
            )
            return
        }

        val repository = HamsterRepository(accountNumber)

        val state = repository.getState()
        val event = repository.getEvent()
        val clicker = repository.getCurrentClicker()

        val stateObserver =
            Observer<HamsterRepository.HamsterState> {
                updateStatus(event.value, it, clicker.value)
            }

        val eventObserver =
            Observer<HamsterRepository.HamsterEvent> {
                updateStatus(it, state.value, clicker.value)
            }

        state.observeForever(stateObserver)
        event.observeForever(eventObserver)

        repository.init()
        repository.autoTapper()
        repository.autoBoost()
        repository.autoTapper()
        repository.smartUpgrade()
        repository.autoCombo()

        state.removeObserver(stateObserver)
        event.removeObserver(eventObserver)
    }
}
