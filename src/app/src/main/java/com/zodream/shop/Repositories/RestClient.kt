package com.zodream.shop.repositories

import com.zodream.shop.models.HttpException
import com.zodream.shop.models.IJsonFormatter
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class RestClient(var uri: String, var method: String = "GET") {

    val headers: Map<String, String> = mutableMapOf()

    var body: String = ""

    fun addQueries(key: String, value: String): RestClient {

        return this
    }

    fun addHeader(key: String, value: String): RestClient {
        headers.put(key, value)
        return this
    }

    fun addBody(key: String, value: String): RestClient {

        return this
    }

    fun <T> fetch(uri: String, key: String, value: String?, failure: (HttpException) -> Unit): T? {

    }

    fun fetch(uri: String, data: Map<String, String>) {

    }

    fun <T> post(uri: String, body: String) {

    }

    fun <T>execute(type: Class<T>, failure: (HttpException) -> Unit): T? {
        val text = execute(failure)
        if (text == null || text.isEmpty()) {
            return null;
        }
        val data = type.newInstance()
        if (data is IJsonFormatter) {
            data.fromJSON(JSONObject(text))
            return data
        }
        return null
    }

    fun execute(failure: (HttpException) -> Unit): String? {
        val text: String
        val connection = URL(uri).openConnection() as HttpURLConnection
        connection.requestMethod = method
        if (headers.isNotEmpty()) {
            for (key in headers.keys) {
                connection.setRequestProperty(key, headers[key])
            }
        }
        if (body.isNotEmpty()) {
            val out = DataOutputStream(connection.outputStream)
            out.writeBytes(body)
            out.flush()
            out.close()
        }
        try{
            connection.connect()
            text = connection.inputStream.use {it.reader().use{reader -> reader.readText()}}
            if (connection.responseCode != 200) {
                val error = HttpException(connection.responseCode)
                error.fromJSON(JSONObject(text))
                failure(error)
                return null
            }
        }finally {
            connection.disconnect()
        }
        return text
    }
}