package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DailyCombo(
    @SerializedName("bonusCoins") val bonusCoins: Int, // 5000000
    @SerializedName("isClaimed") val isClaimed: Boolean, // false
    @SerializedName("remainSeconds") val remainSeconds: Int, // 4438
    @SerializedName("upgradeIds") val upgradeIds: List<String>
)
