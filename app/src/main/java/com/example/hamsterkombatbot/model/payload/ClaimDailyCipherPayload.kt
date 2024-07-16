package com.example.hamsterkombatbot.model.payload

import com.google.gson.annotations.SerializedName

data class ClaimDailyCipherPayload(@SerializedName("cipher") val cipher: String)
