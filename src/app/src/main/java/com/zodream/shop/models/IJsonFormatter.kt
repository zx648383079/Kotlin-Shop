package com.zodream.shop.models

import org.json.JSONObject

interface IJsonFormatter {

    fun fromJSON(data: JSONObject)

    fun toJSON(): JSONObject
}