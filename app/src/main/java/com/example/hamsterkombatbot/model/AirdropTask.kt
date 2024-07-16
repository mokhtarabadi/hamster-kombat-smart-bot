package com.example.hamsterkombatbot.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AirdropTask(
    @SerializedName("channelId") val channelId: Long, // -1002075341442
    @SerializedName("id") val id: String, // airdrop_connect_ton_wallet
    @SerializedName("link") val link: String, // https://t.me/hamster_kombat
    @SerializedName("periodicity") val periodicity: String, // Once
    @SerializedName("rewardTickets") val rewardTickets: Int // 0
)
