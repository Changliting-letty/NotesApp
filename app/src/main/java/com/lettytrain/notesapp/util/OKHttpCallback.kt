package com.lettytrain.notesapp.util

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

open class OKHttpCallback : Callback {
    var url: String = ""
    var result: String = ""

    //成功
    override fun onResponse(call: Call, response: Response) {
        result = response.body?.string().toString()
        Log.d("OKHttpCallback", "url:${url}")
        onFinish("success", result)
    }

    //失败
    override fun onFailure(call: Call, e: IOException) {
        Log.d("OKHttpCallback", "url:${url}")
        Log.d("OKHttpCallback", "请求失败${e.toString()}")
        onFinish("failure", e.toString())
    }

    open fun onFinish(status: String, msg: String) {
        Log.d("OKHttpCallback", "url:${url},status:${status},message:${msg}")
    }


}