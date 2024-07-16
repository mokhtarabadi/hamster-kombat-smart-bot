package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AuthByTelegramWebappResponse(
    @SerializedName("authToken")
    val authToken:
        String, // 1717670733755xPv1vVXlyOHUVHV36C2cl9JcImLaS15z6DSbO4m3s64002ISajUSgcEcnksROHK11247026399
    @SerializedName("status") val status: String // Ok
)
