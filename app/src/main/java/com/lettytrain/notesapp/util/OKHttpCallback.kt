package com.lettytrain.notesapp.util

import android.util.Log
import kotlinx.android.synthetic.main.activity_main.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.math.log

class OKHttpCallback :Callback {
    var url:String=""
    var result:String=""

   override fun onResponse(call: Call,response: Response){
        Log.d("OKHttpCallback","url:${url}")
        result=  response.body?.string().toString()
        onFinish("success",result)
    }

    override fun onFailure(call: Call, e: IOException) {
      Log.d("OKHttpCallback","url:${url}")
        Log.d("OKHttpCallback","请求失败${e.toString()}")
        onFinish("failure",e.toString())
    }
    fun onFinish(status:String,msg :String){
        Log.d("OKHttpCallback","url:${url},status:${status},message:${msg}")
    }


}