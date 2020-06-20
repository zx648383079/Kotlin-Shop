package com.zodream.shop.models

import com.zodream.shop.repositories.RestClient
import com.zodream.shop.utils.Signature
import java.text.SimpleDateFormat
import java.util.*

class ApiSignature(private val appId: String, private val secret: String) {


    fun append(request: RestClient): RestClient {
        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date())
        request.addQueries("appid", appId)
            .addQueries("timestamp", timestamp)
            .addQueries("sign", Signature.md5("$appId$timestamp$secret"))
        return request
    }
}