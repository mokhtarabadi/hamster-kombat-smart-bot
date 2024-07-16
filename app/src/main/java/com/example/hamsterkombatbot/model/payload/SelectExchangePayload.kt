package com.example.hamsterkombatbot.model.payload

import com.google.gson.annotations.SerializedName

data class SelectExchangePayload(@SerializedName("exchangeId") val exchangeId: String)
