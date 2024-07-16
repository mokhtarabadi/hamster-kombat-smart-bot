package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.ClickerUser
import com.example.hamsterkombatbot.model.Task
import com.google.gson.annotations.SerializedName

@Keep
data class CheckTaskResponse(
    @SerializedName("clickerUser") val clickerUser: ClickerUser,
    @SerializedName("task") val task: Task
)
