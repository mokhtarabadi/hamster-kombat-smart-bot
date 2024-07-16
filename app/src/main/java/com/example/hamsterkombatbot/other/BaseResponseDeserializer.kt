package com.example.hamsterkombatbot.other

import BaseResponse
import ValidateFailedData
import ValidateFailedResponse
import ValidateSuccessData
import ValidateSuccessResponse
import com.google.gson.*
import java.lang.reflect.Type

class BaseResponseDeserializer : JsonDeserializer<BaseResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BaseResponse {
        val jsonObject = json.asJsonObject

        val success = jsonObject.get("success")?.asBoolean
        val code = jsonObject.get("code")?.asString
        val message = jsonObject.get("message")?.asString

        return if (success == true) {
            val data =
                context.deserialize<ValidateSuccessData>(
                    jsonObject.get("data"),
                    ValidateSuccessData::class.java
                )
            ValidateSuccessResponse(success, data)
        } else {
            val data =
                context.deserialize<ValidateFailedData>(
                    jsonObject.get("data"),
                    ValidateFailedData::class.java
                )
            ValidateFailedResponse(code ?: "", message ?: "", data)
        }
    }
}
