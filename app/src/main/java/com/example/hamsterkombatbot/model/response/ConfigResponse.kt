package com.example.hamsterkombatbot.model.response

import androidx.annotation.Keep
import com.example.hamsterkombatbot.model.*
import com.google.gson.annotations.SerializedName

@Keep
data class ConfigResponse(
    @SerializedName("clickerConfig") val clickerConfig: ClickerConfig,
    @SerializedName("dailyCipher") val dailyCipher: DailyCipher,
    @SerializedName("feature") val feature: List<String>
) {
    @Keep
    data class ClickerConfig(
        @SerializedName("airdropTasks") val airdropTasks: List<AirdropTask>,
        @SerializedName("airdrops") val airdrops: List<Any>,
        @SerializedName("boosts") val boosts: List<Boost>,
        @SerializedName("depositsUpdateCooldownSeconds")
        val depositsUpdateCooldownSeconds: Int, // 60
        @SerializedName("exchanges") val exchanges: List<Exchange>,
        @SerializedName("guidLink") val guidLink: GuidLink,
        @SerializedName("levelUp") val levelUp: LevelUp,
        @SerializedName("maxPassiveDtSeconds") val maxPassiveDtSeconds: Int, // 10800
        @SerializedName("referral") val referral: Referral,
        @SerializedName("tasks") val tasks: List<Task>,
        @SerializedName("upgrades") val upgrades: List<Upgrade>,
        @SerializedName("userLevels_balanceCoins")
        val userLevelsBalanceCoins: List<UserLevelsBalanceCoin>
    ) {

        @Keep
        data class Exchange(
            @SerializedName("bonus") val bonus: Int, // 100
            @SerializedName("id") val id: String, // binance
            @SerializedName("name") val name: String, // Binance
            @SerializedName("players") val players: Int // 150000
        )

        @Keep
        data class GuidLink(
            @SerializedName("br")
            val br:
                String, // https://hamster-kombat.notion.site/Hamster-Kombat-manual-7e53c342eef143de8bc9c8262ea3a36d
            @SerializedName("en")
            val en:
                String, // https://hamster-kombat.notion.site/Hamster-Kombat-manual-7e53c342eef143de8bc9c8262ea3a36d
            @SerializedName("latam")
            val latam:
                String, // https://hamster-kombat.notion.site/Hamster-Kombat-manual-7e53c342eef143de8bc9c8262ea3a36d
            @SerializedName("ru")
            val ru:
                String, // https://hamster-kombat.notion.site/Hamster-Kombat-b14906aba45243c58c9f634ce1b38d1e
            @SerializedName("uz")
            val uz:
                String, // https://hamster-kombat.notion.site/Hamster-Kombat-manual-7e53c342eef143de8bc9c8262ea3a36d
            @SerializedName("vn")
            val vn:
                String // https://hamster-kombat.notion.site/Hamster-Kombat-manual-7e53c342eef143de8bc9c8262ea3a36d
        )

        @Keep
        data class LevelUp(
            @SerializedName("earnPerTap") val earnPerTap: Int, // 1
            @SerializedName("maxTaps") val maxTaps: Int // 500
        )

        @Keep
        data class Referral(
            @SerializedName("base") val base: Base,
            @SerializedName("premium") val premium: Premium
        ) {
            @Keep
            data class Base(
                @SerializedName("levelUp") val levelUp: LevelUp,
                @SerializedName("welcome") val welcome: Int // 5000
            ) {
                @Keep
                data class LevelUp(
                    @SerializedName("1") val x1: Int, // 0
                    @SerializedName("10") val x10: Int, // 3000000
                    @SerializedName("2") val x2: Int, // 20000
                    @SerializedName("3") val x3: Int, // 30000
                    @SerializedName("4") val x4: Int, // 40000
                    @SerializedName("5") val x5: Int, // 60000
                    @SerializedName("6") val x6: Int, // 100000
                    @SerializedName("7") val x7: Int, // 250000
                    @SerializedName("8") val x8: Int, // 500000
                    @SerializedName("9") val x9: Int // 1000000
                )
            }

            @Keep
            data class Premium(
                @SerializedName("levelUp") val levelUp: LevelUp,
                @SerializedName("welcome") val welcome: Int // 25000
            ) {
                @Keep
                data class LevelUp(
                    @SerializedName("1") val x1: Int, // 0
                    @SerializedName("10") val x10: Int, // 6000000
                    @SerializedName("2") val x2: Int, // 25000
                    @SerializedName("3") val x3: Int, // 50000
                    @SerializedName("4") val x4: Int, // 75000
                    @SerializedName("5") val x5: Int, // 100000
                    @SerializedName("6") val x6: Int, // 150000
                    @SerializedName("7") val x7: Int, // 500000
                    @SerializedName("8") val x8: Int, // 1000000
                    @SerializedName("9") val x9: Int // 2000000
                )
            }
        }

        @Keep
        data class UserLevelsBalanceCoin(
            @SerializedName("coinsToLeveUp") val coinsToLeveUp: Long, // 5000
            @SerializedName("level") val level: Int // 1
        )
    }
}
