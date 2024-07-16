package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DailyCipher(
    @SerializedName("bonusCoins") val bonusCoins: Int, // 1000000
    @SerializedName("cipher") val cipher: String, // VE93O
    @SerializedName("isClaimed") val isClaimed: Boolean, // false
    @SerializedName("remainSeconds") val remainSeconds: Int // 37980
)
