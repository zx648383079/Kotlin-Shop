package com.zodream.shop.models

import org.json.JSONObject

data class Site(
    var name: String = "shop",
    var version: String = "0.1",
    var logo: String = "",
    var goods: Int = 0,
    var category: Int = 0,
    var brand: Int = 0,
    var currency: String = "￥"): IJsonFormatter {
    override fun fromJSON(data: JSONObject) {
        name = data.getString("name");
        version = data.getString("version");
        logo = data.getString("logo");
        goods = data.getInt("goods");
        category = data.getInt("category");
        brand = data.getInt("brand");
        currency = data.getString("currency");
    }

    override fun toJSON(): JSONObject {
        TODO("Not yet implemented")
    }

}