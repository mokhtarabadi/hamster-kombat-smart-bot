package com.example.hamsterkombatbot.model.payload

import com.google.gson.annotations.SerializedName

data class CheckTaskPayload(@SerializedName("taskId") val taskId: String)
