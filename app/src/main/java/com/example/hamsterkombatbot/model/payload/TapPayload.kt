package com.example.hamsterkombatbot.model.payload

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TapPayload(
    @SerializedName("availableTaps") val availableTaps: Int, // 2963
    @SerializedName("count") val count: Int, // 13
    @SerializedName("timestamp") val timestamp: Int // 1717670752
)
