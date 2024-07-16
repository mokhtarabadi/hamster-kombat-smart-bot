package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.ClickerUser
import com.example.hamsterkombatbot.model.DailyCombo
import com.example.hamsterkombatbot.model.UpgradeForBuy
import com.google.gson.annotations.SerializedName

@Keep
data class BuyUpgradeResponse(
    @SerializedName("clickerUser") val clickerUser: ClickerUser,
    @SerializedName("dailyCombo") val dailyCombo: DailyCombo,
    @SerializedName("upgradesForBuy") val upgradesForBuy: List<UpgradeForBuy>
)
