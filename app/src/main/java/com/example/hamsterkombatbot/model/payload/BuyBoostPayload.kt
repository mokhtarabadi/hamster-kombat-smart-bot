package com.example.hamsterkombatbot.model.payload

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BuyBoostPayload(
    @SerializedName("timestamp") val timestamp: Int, // 1717670756
    @SerializedName("boostId") val upgradeId: String // BoostFullAvailableTaps
)
