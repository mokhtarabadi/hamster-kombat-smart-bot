package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.ClickerUser
import com.google.gson.annotations.SerializedName

@Keep data class TapResponse(@SerializedName("clickerUser") val clickerUser: ClickerUser)
