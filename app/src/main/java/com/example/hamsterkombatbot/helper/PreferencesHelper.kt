package com.example.hamsterkombatbot.helper

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.example.hamsterkombatbot.MainApplication
import com.google.gson.Gson

class PreferencesHelper(context: Context, accountID: Int) {

    companion object {
        const val TOKEN = "token"
        const val LICENCE_TOKEN = "license_token"

        const val MIN_DELAY = "min_delay"
        const val MAX_DELAY = "max_delay"

        const val BALANCE_PERCENTAGE_PER_SMART_BUY = "balance_percentage_per_smart_buy"
        const val BALANCE_PERCENTAGE_PER_AUTO_BOOST = "balance_percentage_per_auto_boost"

        const val AUTO_TAP_MIN = "auto_tap_min"
        const val AUTO_TAP_MAX = "auto_tap_max"

        const val MAX_COOLDOWN_WAIT_MINUTES = "max_cooldown_wait_minutes"

        const val HEADERS = "headers"

        private const val ROUND_DELAY_BACKGROUND = "round_delay_background"
        private const val CURRENT_ACCOUNT = "current_account"
        private const val APP_LAUNCHES = "app_launches"

        private var globalPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(MainApplication.appContext)

        // save current account int
        fun saveCurrentAccount(currentAccount: Int) {
            globalPreferences.edit().putInt(CURRENT_ACCOUNT, currentAccount).apply()
        }

        // get current account int
        fun getCurrentAccount(): Int {
            return globalPreferences.getInt(CURRENT_ACCOUNT, 1)
        }

        // get app launches int
        fun getAppLaunches(): Int {
            return globalPreferences.getInt(APP_LAUNCHES, 0)
        }

        // increment app launches
        fun incrementAppLaunches() {
            globalPreferences.edit().putInt(APP_LAUNCHES, getAppLaunches() + 1).apply()
        }

        // set round delay background
        fun setRoundDelayBackground(delay: Int) {
            globalPreferences.edit().putInt(ROUND_DELAY_BACKGROUND, delay).apply()
        }

        // get round delay background
        fun getRoundDelayBackground(): Int {
            return globalPreferences.getInt(ROUND_DELAY_BACKGROUND, 30)
        }
    }

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences("hamsterkombatbot_$accountID", Context.MODE_PRIVATE)

    // set token
    fun setToken(token: String) {
        sharedPreferences.edit().putString(TOKEN, token).apply()
    }

    // get token
    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN, "")
    }

    // is token exists
    fun isTokenExists(): Boolean {
        return sharedPreferences.contains(TOKEN)
    }

    // clear token
    fun clearToken() {
        sharedPreferences.edit().remove(TOKEN).apply()
    }

    // set min delay
    fun setMinDelay(delay: Int) {
        sharedPreferences.edit().putInt(MIN_DELAY, delay).apply()
    }

    // set max delay
    fun setMaxDelay(delay: Int) {
        sharedPreferences.edit().putInt(MAX_DELAY, delay).apply()
    }

    // get min delay
    fun getMinDelay(): Int {
        return sharedPreferences.getInt(MIN_DELAY, 1)
    }

    // get max delay
    fun getMaxDelay(): Int {
        return sharedPreferences.getInt(MAX_DELAY, 3)
    }

    // get balance percentage per smart buy
    fun getBalancePercentagePerSmartBuy(): Int {
        return sharedPreferences.getInt(BALANCE_PERCENTAGE_PER_SMART_BUY, 30)
    }

    // set balance percentage per smart buy
    fun setBalancePercentagePerSmartBuy(percentage: Int) {
        sharedPreferences.edit().putInt(BALANCE_PERCENTAGE_PER_SMART_BUY, percentage).apply()
    }

    // set auto tap min
    fun setAutoTapMin(min: Int) {
        sharedPreferences.edit().putInt(AUTO_TAP_MIN, min).apply()
    }

    // set auto tap max
    fun setAutoTapMax(max: Int) {
        sharedPreferences.edit().putInt(AUTO_TAP_MAX, max).apply()
    }

    // get auto tap min
    fun getAutoTapMin(): Int {
        return sharedPreferences.getInt(AUTO_TAP_MIN, 10)
    }

    // get auto tap max
    fun getAutoTapMax(): Int {
        return sharedPreferences.getInt(AUTO_TAP_MAX, 30)
    }

    fun saveHeaders(headers: Map<String, String>) {
        val gson = Gson()
        val json = gson.toJson(headers)
        sharedPreferences.edit().putString(HEADERS, json).apply()
    }

    fun getHeaders(): Map<String, String> {
        val gson = Gson()
        val json = sharedPreferences.getString(HEADERS, "[]")
        return gson.fromJson(json, Map::class.java) as Map<String, String>
    }

    // set license token
    fun setLicenseToken(token: String) {
        sharedPreferences.edit().putString(LICENCE_TOKEN, token).apply()
    }

    // get license token
    fun getLicenseToken(): String? {
        return sharedPreferences.getString(LICENCE_TOKEN, "")
    }

    // set balance percentage per auto boost
    fun setBalancePercentagePerAutoBoost(percentage: Int) {
        sharedPreferences.edit().putInt(BALANCE_PERCENTAGE_PER_AUTO_BOOST, percentage).apply()
    }

    // get balance percentage per auto boost
    fun getBalancePercentagePerAutoBoost(): Int {
        return sharedPreferences.getInt(BALANCE_PERCENTAGE_PER_AUTO_BOOST, 10)
    }

    // set max cooldown wait minutes
    fun setMaxCooldownWaitMinutes(minutes: Int) {
        sharedPreferences.edit().putInt(MAX_COOLDOWN_WAIT_MINUTES, minutes).apply()
    }

    // get max cooldown wait minutes
    fun getMaxCooldownWaitMinutes(): Int {
        return sharedPreferences.getInt(MAX_COOLDOWN_WAIT_MINUTES, 15)
    }
}
