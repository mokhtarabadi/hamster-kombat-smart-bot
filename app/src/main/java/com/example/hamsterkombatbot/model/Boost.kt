package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Boost(
    @SerializedName("cooldownSeconds") val cooldownSeconds: Int, // 3600
    @SerializedName("earnPerTap") val earnPerTap: Int, // 1
    @SerializedName("id") val id: String, // BoostEarnPerTap
    @SerializedName("maxLevel") val maxLevel: Int, // 6
    @SerializedName("maxTaps") val maxTaps: Int, // 0
    @SerializedName("price") val price: Int // 2000
)
