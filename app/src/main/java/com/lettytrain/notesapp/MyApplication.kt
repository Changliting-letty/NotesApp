package com.lettytrain.notesapp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lettytrain.notesapp.util.SynToRemoteWorker
import com.lettytrain.notesapp.util.getNowDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.lifecycle.Observer


//获取全局Context

class MyApplication : Application() {

    companion object {
        @SuppressLint("staticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        //每间隔一小时执行同步
        val request = PeriodicWorkRequest.Builder(SynToRemoteWorker::class.java,15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(request)
    }
}