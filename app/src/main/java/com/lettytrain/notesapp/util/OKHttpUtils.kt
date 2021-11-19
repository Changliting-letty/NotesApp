package com.lettytrain.notesapp.util


import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody


object OKHttpUtils {
    //实现cookie持久化
    val client = OkHttpClient().newBuilder().cookieJar(PersistenceCookieJar()).build()

    fun get(url: String, callback: OKHttpCallback) {
        callback.url = url
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }

    private val JSONFormat get() = String.format("application/json; charset=utf-8").toMediaType()

    fun post(url: String, json: String, callback: OKHttpCallback) {
        callback.url = url
        val requestBody = json.toRequestBody(JSONFormat)
        val request = Request.Builder().url(url).post(requestBody).build()
        client.newCall(request).enqueue(callback)
    }
}

