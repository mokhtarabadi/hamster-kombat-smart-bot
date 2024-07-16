package com.example.hamsterkombatbot.model.payload

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep data class AddReferralPayload(@SerializedName("friendUserId") val friendUserId: Int)
