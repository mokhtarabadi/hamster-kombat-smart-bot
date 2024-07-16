package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.ClickerUser
import com.example.hamsterkombatbot.model.DailyCombo
import com.google.gson.annotations.SerializedName

@Keep
data class DailyClaimComboResponse(
    @SerializedName("clickerUser") val clickerUser: ClickerUser,
    @SerializedName("dailyCombo") val dailyCombo: DailyCombo
)
