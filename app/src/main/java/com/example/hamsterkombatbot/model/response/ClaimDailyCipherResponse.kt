package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.ClickerUser
import com.example.hamsterkombatbot.model.DailyCipher
import com.google.gson.annotations.SerializedName

@Keep
data class ClaimDailyCipherResponse(
    @SerializedName("clickerUser") val clickerUser: ClickerUser,
    @SerializedName("dailyCipher") val dailyCipher: DailyCipher
)
