package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.Boost
import com.google.gson.annotations.SerializedName

@Keep
data class BoostsForBuyResponse(@SerializedName("boostsForBuy") val boostsForBuy: List<Boost>)
