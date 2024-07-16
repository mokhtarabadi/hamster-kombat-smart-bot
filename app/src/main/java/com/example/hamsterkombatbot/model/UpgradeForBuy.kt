package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UpgradeForBuy(
    @SerializedName("condition") val condition: Condition?,
    @SerializedName("cooldownSeconds") val cooldownSeconds: Int, // 0
    @SerializedName("currentProfitPerHour") val currentProfitPerHour: Int, // 0
    @SerializedName("expiresAt") val expiresAt: String, // 2024-04-17T15:00:00.000Z
    @SerializedName("id") val id: String, // support_team
    @SerializedName("isAvailable") val isAvailable: Boolean, // true
    @SerializedName("isExpired") val isExpired: Boolean, // false
    @SerializedName("level") val level: Int, // 1
    @SerializedName("maxLevel") val maxLevel: Int, // 1
    @SerializedName("name") val name: String, // Support team
    @SerializedName("price") val price: Int, // 750
    @SerializedName("profitPerHour") val profitPerHour: Int, // 70
    @SerializedName("profitPerHourDelta") val profitPerHourDelta: Int, // 70
    @SerializedName("section") val section: String, // PR&Team
    @SerializedName("totalCooldownSeconds") val totalCooldownSeconds: Int, // 0
    @SerializedName("welcomeCoins") val welcomeCoins: Int // 7500000
) {
    @Keep
    data class Condition(
        @SerializedName("channelId") val channelId: Long, // -1002075341442
        @SerializedName("level") val level: Int, // 5
        @SerializedName("link") val link: String, // https://t.me/+kK14mIuJR2hlMmNi
        @SerializedName("links") val links: List<String>,
        @SerializedName("moreReferralsCount") val moreReferralsCount: Int, // 1
        @SerializedName("referralCount") val referralCount: Int, // 2
        @SerializedName("subscribeLink")
        val subscribeLink:
            String, // https://www.youtube.com/@HamsterKombat_Official?sub_confirmation=1
        @SerializedName("_type") val type: String, // ByUpgrade
        @SerializedName("upgradeId") val upgradeId: String // facebook_ads
    )
}
