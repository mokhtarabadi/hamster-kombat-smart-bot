package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.DailyCombo
import com.example.hamsterkombatbot.model.UpgradeForBuy
import com.google.gson.annotations.SerializedName

@Keep
data class UpgradesForBuyResponse(
    @SerializedName("dailyCombo") val dailyCombo: DailyCombo,
    @SerializedName("sections") val sections: List<Section>,
    @SerializedName("upgradesForBuy") val upgradesForBuy: List<UpgradeForBuy>
) {
    @Keep
    data class Section(
        @SerializedName("isAvailable") val isAvailable: Boolean, // true
        @SerializedName("section") val section: String // Markets
    )
}
