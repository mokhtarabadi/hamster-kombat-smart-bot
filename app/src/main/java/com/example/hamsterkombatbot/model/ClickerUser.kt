package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ClickerUser(
    @SerializedName("airdropTasks") val airdropTasks: List<AirdropTask>,
    @SerializedName("availableTaps") val availableTaps: Int, // 2990
    @SerializedName("balanceCoins") val balanceCoins: Double, // 5991.247499999996
    @SerializedName("boosts") val boosts: List<Boost>,
    @SerializedName("earnPassivePerHour") val earnPassivePerHour: Int, // 1370
    @SerializedName("earnPassivePerSec") val earnPassivePerSec: Double, // 0.3807
    @SerializedName("earnPerTap") val earnPerTap: Int, // 4
    @SerializedName("exchangeId") val exchangeId: String, // bybit
    @SerializedName("id") val id: String, // 1247026399
    @SerializedName("lastPassiveEarn") val lastPassiveEarn: Double, // 0
    @SerializedName("lastSyncUpdate") val lastSyncUpdate: Int, // 1717670761
    @SerializedName("level") val level: Int, // 2
    @SerializedName("maxTaps") val maxTaps: Int, // 3000
    @SerializedName("referral") val referral: Referral?,
    @SerializedName("referralsCount") val referralsCount: Int, // 0
    @SerializedName("tapsRecoverPerSec") val tapsRecoverPerSec: Int, // 3
    @SerializedName("tasks") val tasks: List<Task>,
    @SerializedName("totalCoins") val totalCoins: Double, // 51901.2475
    @SerializedName("upgrades") val upgrades: List<UpgradeForBuy>
) {
    @Keep
    data class Referral(@SerializedName("friend") val friend: Friend) {
        @Keep
        data class Friend(
            @SerializedName("firstName") val firstName: String, // Nadi
            @SerializedName("id") val id: Long, // 369699268
            @SerializedName("isBot") val isBot: Boolean, // false
            @SerializedName("languageCode") val languageCode: String, // en
            @SerializedName("lastName") val lastName: String,
            @SerializedName("username") val username: String, // Someonenewfortoday
            @SerializedName("welcomeBonusCoins") val welcomeBonusCoins: Int // 5000
        )
    }
}
