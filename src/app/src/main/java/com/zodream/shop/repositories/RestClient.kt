package com.zodream.shop.repositories

import com.zodream.shop.models.HttpException
import com.zodream.shop.models.IJsonFormatter
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

typealias FailureFunc = (HttpException) -> Unit

class RestClient(private var baseUri: String, private var method: String = "GET") {

    private val headers: MutableMap<String, String> = mutableMapOf()
    private val queries: MutableMap<String, String> = mutableMapOf()

    fun addQueries(key: String, value: String): RestClient {
        queries[key] = value;
        return this
    }

    fun addHeader(key: String, value: String): RestClient {
        headers[key] = value
        return this
    }

    fun addHeader(data: Map<String, String>): RestClient {
        headers.putAll(data)
        return this
    }

    fun <T> fetch(type: Class<T>, uri: String, failure: FailureFunc?): T? {
        method = "GET"
        return execute<T>(type, uri, "", failure)
    }

    fun <T> fetch(type: Class<T>, uri: String, key: String, value: String?, failure: FailureFunc?): T? {
        method = "GET"
        return execute<T>(type, appendUriParam(uri, key, value), "", failure)
    }

    fun <T> fetch(type: Class<T>, uri: String, data: Map<String, String>, failure: FailureFunc?): T? {
        method = "GET"
        return execute<T>(type, appendUriParam(uri, data), "", failure)
    }

    fun <T> post(type: Class<T>, uri: String, body: String, failure: FailureFunc?): T? {
        method = "POST"
        return execute<T>(type, uri, body, failure)
    }

    fun <T> put(type: Class<T>, uri: String, body: String, failure: FailureFunc?): T? {
        method = "PUT"
        return execute<T>(type, uri, body, failure)
    }

    fun <T> delete(type: Class<T>, uri: String, key: String, value: String, failure: FailureFunc?): T? {
        method = "DELETE"
        return execute<T>(type,  appendUriParam(uri, key, value), "", failure)
    }

    fun <T> delete(type: Class<T>, uri: String, data: Map<String, String>, failure: FailureFunc?): T? {
        method = "GET"
        return execute<T>(type,  appendUriParam(uri, data), "", failure)
    }

    fun <T>execute(type: Class<T>, uri: String, body: String, failure: FailureFunc?): T? {
        val text = execute(uri, body, failure)
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

    fun execute(uri: String, data: String, failure: FailureFunc?): String? {
        val text: String
        var url = uri;
        if (uri.indexOf("://") < 0) {
            url = baseUri + uri
        }
        val connection = URL(appendUriParam(url, queries)).openConnection() as HttpURLConnection
        connection.requestMethod = method
        if (headers.isNotEmpty()) {
            for (key in headers.keys) {
                connection.setRequestProperty(key, headers[key])
            }
        }
        if (data.isNotEmpty()) {
            val out = DataOutputStream(connection.outputStream)
            out.writeBytes(data)
            out.flush()
            out.close()
        }
        try{
            connection.connect()
            text = connection.inputStream.use {it.reader().use{reader -> reader.readText()}}
            if (connection.responseCode != 200) {
                val error = HttpException(connection.responseCode)
                error.fromJSON(JSONObject(text))
                failure?.invoke(error)
                return null
            }
        }finally {
            connection.disconnect()
        }
        return text
    }

    private fun appendUriParam(uri: String, key: String, value: String?): String {
        var link = '?'
        if (uri.indexOf('?') >= 0) {
            link = '&'
        }
        return "$uri$link$key=$value"
    }

    private fun appendUriParam(uri: String, data: Map<String, String>): String {
        var link = '?'
        if (uri.indexOf('?') >= 0) {
            link = '&'
        }
        val param = StringBuilder()
        var first = true
        data.forEach { (t, u) ->
            param.append(if (first) {
                first = false;
                "$t=$u"
            } else "&$t=$u");
        };
        if (param.isEmpty()) {
            return uri
        }
        return "$uri$link$param";
    }
}