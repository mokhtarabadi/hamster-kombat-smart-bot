package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Task(
    @SerializedName("channelId") val channelId: Long, // -1002075341442
    @SerializedName("id") val id: String, // hamster_drop
    @SerializedName("link") val link: String, // https://hamsterkombat.art
    @SerializedName("periodicity") val periodicity: String, // Once
    @SerializedName("rewardCoins") val rewardCoins: Int, // 100000
    @SerializedName("rewardsByDays") val rewardsByDays: List<RewardsByDay>,
    // isCompleted
    @SerializedName("isCompleted") val isCompleted: Boolean,
    // completedAt
    @SerializedName("completedAt") val completedAt: String,
    // days
    @SerializedName("days") val days: Int
) {
    @Keep
    data class RewardsByDay(
        @SerializedName("days") val days: Int, // 1
        @SerializedName("rewardCoins") val rewardCoins: Int // 500
    )
}
