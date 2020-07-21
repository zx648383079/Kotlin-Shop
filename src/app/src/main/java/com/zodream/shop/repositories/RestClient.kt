package com.zodream.shop.repositories

import com.zodream.shop.models.HttpException
import com.zodream.shop.models.IJsonFormatter
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
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

    fun <T> fetch(type: Class<T>, uri: String, success: (T) -> Unit, failure: FailureFunc?) {
        method = "GET"
        return execute<T>(type, uri, "", success, failure)
    }

    fun <T> fetch(type: Class<T>, uri: String, key: String, value: String?, success: (T) -> Unit, failure: FailureFunc?) {
        method = "GET"
        return execute<T>(type, appendUriParam(uri, key, value), "", success, failure)
    }

    fun <T> fetch(type: Class<T>, uri: String, data: Map<String, String>, success: (T) -> Unit, failure: FailureFunc?) {
        method = "GET"
        return execute<T>(type, appendUriParam(uri, data), "", success, failure)
    }

    fun <T> post(type: Class<T>, uri: String, body: String, success: (T) -> Unit, failure: FailureFunc?) {
        method = "POST"
        return execute<T>(type, uri, body, success, failure)
    }

    fun <T> put(type: Class<T>, uri: String, body: String, success: (T) -> Unit, failure: FailureFunc?) {
        method = "PUT"
        return execute<T>(type, uri, body, success, failure)
    }

    fun <T> delete(type: Class<T>, uri: String, key: String, value: String, success: (T) -> Unit, failure: FailureFunc?) {
        method = "DELETE"
        return execute<T>(type,  appendUriParam(uri, key, value), "", success, failure)
    }

    fun <T> delete(type: Class<T>, uri: String, data: Map<String, String>, success: (T) -> Unit, failure: FailureFunc?) {
        method = "GET"
        return execute<T>(type,  appendUriParam(uri, data), "", success, failure)
    }

    fun <T>execute(type: Class<T>, uri: String, body: String, success: (T) -> Unit, failure: FailureFunc?) {
        execute(uri, body, fun(text: String) {
            if (text.isEmpty()) {
                return
            }
            val data = type.newInstance()
            if (data is IJsonFormatter) {
                data.fromJSON(JSONObject(text))
                success(data)
            }
        }, failure);
    }

    fun execute(uri: String, data: String, success: (String)-> Unit, failure: FailureFunc?) {
        var url = uri;
        if (uri.indexOf("://") < 0) {
            url = baseUri + uri
        }
        url = appendUriParam(url, queries);
        Thread(object: Runnable {
            override fun run() {
                var connection: HttpURLConnection? = null
                try{
                    connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = method
                    connection.connectTimeout = 8000
                    connection.readTimeout = 8000
                    // connection.doInput = true
                    // connection.doOutput = true
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

                    connection.connect()
                    val responseData = StringBuilder()
                    val allText = connection.inputStream.use {it.reader().use{reader -> reader.readText()}}
                    responseData.append(allText)
                    if(connection.responseCode == 200) {
                        success(responseData.toString())
                        return;
                    }
                    val error = HttpException(connection.responseCode)
                    error.fromJSON(JSONObject(responseData.toString()))
                    failure?.invoke(error)
                } catch (ex: Exception) {
                    failure?.invoke(HttpException(404, ex.toString()))
                } finally {
                    connection?.disconnect()
                }
            }
        }).start();
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