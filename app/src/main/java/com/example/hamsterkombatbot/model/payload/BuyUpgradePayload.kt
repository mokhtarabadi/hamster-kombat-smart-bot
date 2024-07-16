package com.example.hamsterkombatbot.model.payload

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BuyUpgradePayload(
    @SerializedName("timestamp") val timestamp: Long, // 1717670759668
    @SerializedName("upgradeId") val upgradeId: String // btc_pairs
)
