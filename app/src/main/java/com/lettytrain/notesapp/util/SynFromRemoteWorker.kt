package com.lettytrain.notesapp.util

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * author: Chang Liting
 * created on: 2021/11/18 15:10
 * description:
 */
class SynFromRemoteWorker (context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        //1.从服务端拉取所有的noteId

        //2.读取本地idmap键值对，判断本地是否有
            //2.1本地没有
        return Result.success()
    }
}