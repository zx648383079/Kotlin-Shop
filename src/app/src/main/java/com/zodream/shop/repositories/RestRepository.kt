package com.zodream.shop.repositories

import com.zodream.shop.models.ApiSignature

class RestRepository(val uri: String, appid: String, secret: String, var token: String = "") {

    private val signature = ApiSignature(appid, secret)

    val site: RestSiteRepository = RestSiteRepository(this)

    fun request(): RestClient {
        val api = RestClient(uri)
        api.addHeader(mapOf("Content-Type" to "application/json",
            "Accept" to "application/json"))
        if (token.isNotEmpty()) {
            api.addHeader("Authorization", "Bearer $token")
        }
        return signature.append(api)
    }
}