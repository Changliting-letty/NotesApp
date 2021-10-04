package com.lettytrain.notesapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

//获取全局Context

class MyApplication:Application(){

    companion object{
        @SuppressLint("staticFieldLeak")
        lateinit var  context:Context
    }

    override fun onCreate() {
        super.onCreate()
        context=applicationContext
    }
}