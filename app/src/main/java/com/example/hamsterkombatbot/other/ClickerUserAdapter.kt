package com.example.hamsterkombatbot.other

import com.example.hamsterkombatbot.model.*
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class ClickerUserAdapter : TypeAdapter<ClickerUser>() {

    override fun write(out: JsonWriter, value: ClickerUser) {
        // Implement if serialization is needed
    }

    override fun read(json: JsonReader): ClickerUser {
        val jsonObject = JsonParser.parseReader(json).asJsonObject

        val gson = Gson()
        val airdropTasks = parseAsList(jsonObject, "airdropTasks", AirdropTask::class.java, gson)
        val boosts = parseAsList(jsonObject, "boosts", Boost::class.java, gson)
        val tasks = parseAsList(jsonObject, "tasks", Task::class.java, gson)
        val upgrades = parseAsList(jsonObject, "upgrades", UpgradeForBuy::class.java, gson)

        return ClickerUser(
            airdropTasks = airdropTasks,
            availableTaps = jsonObject["availableTaps"].asInt,
            balanceCoins = jsonObject["balanceCoins"].asDouble,
            boosts = boosts,
            earnPassivePerHour = jsonObject["earnPassivePerHour"].asInt,
            earnPassivePerSec = jsonObject["earnPassivePerSec"].asDouble,
            earnPerTap = jsonObject["earnPerTap"].asInt,
            exchangeId = jsonObject["exchangeId"].asString,
            id = jsonObject["id"].asString,
            lastPassiveEarn = jsonObject["lastPassiveEarn"].asDouble,
            lastSyncUpdate = jsonObject["lastSyncUpdate"].asInt,
            level = jsonObject["level"].asInt,
            maxTaps = jsonObject["maxTaps"].asInt,
            referral =
                jsonObject["referral"]?.let { gson.fromJson(it, ClickerUser.Referral::class.java) },
            referralsCount = jsonObject["referralsCount"].asInt,
            tapsRecoverPerSec = jsonObject["tapsRecoverPerSec"].asInt,
            tasks = tasks,
            totalCoins = jsonObject["totalCoins"].asDouble,
            upgrades = upgrades
        )
    }

    private fun <T> parseAsList(
        jsonObject: JsonObject,
        key: String,
        clazz: Class<T>,
        gson: Gson
    ): List<T> {
        val list = mutableListOf<T>()
        val element = jsonObject[key]
        if (element != null) {
            if (element.isJsonObject) {
                element.asJsonObject.entrySet().forEach { entry ->
                    val item = gson.fromJson(entry.value, clazz)
                    list.add(item)
                }
            }
        }
        return list
    }
}
