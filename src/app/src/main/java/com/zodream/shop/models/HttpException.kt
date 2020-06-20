package com.zodream.shop.models

import org.json.JSONObject

data class HttpException(var code: Int = 404, var message: String = "not found", var description: String = ""): IJsonFormatter {
    override fun toJSON(): JSONObject {
        return JSONObject()
    }

    override fun fromJSON(data: JSONObject) {
        code = data.getInt("code")
        message = data.getString("message")
        description = data.getString("description")
    }

}