package com.lettytrain.notesapp.util

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Headers.Companion.toHeaders

object OKHttpUtils {
    val client = OkHttpClient()

    fun get(url: String, callback: OKHttpCallback) {
        callback.url = url
        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }
    val JSON = String.format("application/json; charset=utf-8").toMediaType()

    fun post(url:String,json:String,callback:OKHttpCallback){
        callback.url=url
        val requestBody=RequestBody.create(JSON,json)
        val request=Request.Builder().url(url).build()
        client.newCall(request).enqueue(callback)
    }
}

