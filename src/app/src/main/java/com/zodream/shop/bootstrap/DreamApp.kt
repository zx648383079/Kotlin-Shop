package com.zodream.shop.bootstrap

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.zodream.shop.repositories.RestRepository
import android.content.pm.PackageManager

class DreamApp : Application() {

    companion object {
        const val USER_DATA_KEY = "user_data"
        const val API_ENDPOINT_KEY = "com.dream.shop.ApiEndpoint"
        const val APP_ID_KEY = "com.dream.shop.AppId"
        const val SECRET_KEY = "com.dream.shop.Secret"
        const val TOKEN_KEY = "token"
        lateinit var instance: DreamApp
            private set
        lateinit var rest: RestRepository
            private set
    }

    var token: String
        get() = getSharedPreferences().getString(TOKEN_KEY, "") as String
        set(value) {
            getSharedPreferences().edit().putString(TOKEN_KEY, value).apply()
            rest.token = value
        }

    var isLogin: Boolean
        get() = token.isNotEmpty()
        set(value) {
            if (value) {
                token = ""
            }
        }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createRest()
    }

    private fun createRest() {
        val host = getMetaData<String>(API_ENDPOINT_KEY) as String
        val appId = getMetaData<String>(APP_ID_KEY) as String
        val secret = getMetaData<String>(SECRET_KEY) as String
        rest = RestRepository(host, appId, secret, token)
    }

    fun getSharedPreferences(): SharedPreferences {
        return getSharedPreferences(USER_DATA_KEY, Context.MODE_PRIVATE)
    }

    fun <T> getMetaData(name: String): T? {
        return packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData?.get(name) as T
    }

}