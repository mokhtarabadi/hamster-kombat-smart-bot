package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.Task
import com.google.gson.annotations.SerializedName

@Keep data class ListTasksResponse(@SerializedName("tasks") val tasks: List<Task>)
