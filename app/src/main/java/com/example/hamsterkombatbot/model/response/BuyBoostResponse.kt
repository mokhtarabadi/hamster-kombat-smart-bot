package com.example.hamsterkombatbot.model.response

import com.example.hamsterkombatbot.model.Boost
import com.example.hamsterkombatbot.model.ClickerUser
import com.google.gson.annotations.SerializedName

data class BuyBoostResponse(
    @SerializedName("clickerUser") val clickerUser: ClickerUser,
    @SerializedName("boostsForBuy") val boostsForBuy: List<Boost>
)
