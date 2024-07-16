package com.example.hamsterkombatbot.model.response

import com.google.gson.annotations.SerializedName

class AddReferralResponse(
    @SerializedName("friendFirstName") val friendFirstName: String,
    @SerializedName("welcomeCoins") val welcomeCoins: Int,
)
