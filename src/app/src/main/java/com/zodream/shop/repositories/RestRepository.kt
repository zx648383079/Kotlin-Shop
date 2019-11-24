package com.zodream.shop.repositories

class RestRepository(val uri: String, val appid: String, val secret: String, var token: String = "") {

    val request: RestClient =
        RestClient(uri)


}