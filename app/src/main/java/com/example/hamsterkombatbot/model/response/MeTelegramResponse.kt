package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MeTelegramResponse(
    @SerializedName("status") val status: String, // Ok
    @SerializedName("telegramUser") val telegramUser: TelegramUser
) {
    @Keep
    data class TelegramUser(
        @SerializedName("firstName") val firstName: String, // Mohammad Reza
        @SerializedName("id") val id: Long, // 1247026399
        @SerializedName("isBot") val isBot: Boolean, // false
        @SerializedName("languageCode") val languageCode: String, // en
        @SerializedName("lastName") val lastName: String,
        @SerializedName("username") val username: String // mokhtarabadi1997
    )
}
