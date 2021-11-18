package com.lettytrain.notesapp.util

import android.content.Context
import android.util.Log
//import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.database.NotesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import  com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lettytrain.notesapp.vo.ServerResponse
import java.util.*
import kotlin.collections.HashMap


/**
 * author: Chang Liting
 * created on: 2021/11/15 13:23
 * description:
 * 在这个方法中编写具体的后台任务逻辑
doWork()方法不会运行在主线程当中，因此可以放心地在这里执行耗时逻辑
 */
class SynToRemoteWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
//        Log.d(
//            "开始同步",
//            "执行add，操作时间：${Date().getNowDateTime()}"
//        )
        val asyns =
            NotesDatabase.getDatabase(MyApplication.context).asynDao().selectAll()

        if (asyns.size == 0) {
            Log.d("SynToRemoteWorker", "没有什么可同步的,时间：${Date().getNowDateTime()}")
        } else {
            for (asyn in asyns) {
                var operation = asyn.operation
                var offline_id = asyn.offlineId
                when (operation) {
                    "add" -> {
                        val notes = NotesDatabase.getDatabase(MyApplication.context).noteDao()
                            .getSpecificNote(offline_id!!)
//                            //将实体数据转化为json字符串
                        val jsonString = formatJson(Gson().toJson(notes))
                        Log.d("work", "${jsonString}")
                        val url = "http://10.236.11.105:8080/portal/notes/addNote.do"
                        OKHttpUtils.post(url, jsonString, object : OKHttpCallback() {
                            override fun onFinish(status: String, msg: String) {
                                super.onFinish(status, msg)
                                val turnsType = object :
                                    TypeToken<ServerResponse<HashMap<String, Int>>>() {}.type
                                val jsobj =
                                    Gson().fromJson<ServerResponse<HashMap<String, Int>>>(
                                        result,
                                        turnsType
                                    )
                                //接收返回的Notes的onlie_id写入idmap
                                val resMaP = jsobj.data
                                val status = jsobj.status
                                if (status != 0) {
                                    Log.d(
                                        "add同步失败",
                                        "notes:${Gson().toJson(notes)},时间：${Date().getNowDateTime()}"
                                    )
                                } else {
                                    val online_id = resMaP!!.get("online_id")
                                    val offline_id = resMaP!!.get("offline_id")
                                    NotesDatabase.getDatabase(MyApplication.context)
                                        .idmapDao()
                                        .updateOnlineID(online_id!!, offline_id!!)
                                    //删除asyn表中的这条记录
                                    NotesDatabase.getDatabase(MyApplication.context)
                                        .asynDao()
                                        .deleteByOfflineId(offline_id)
                                    val onlineid =
                                        NotesDatabase.getDatabase(MyApplication.context).idmapDao()
                                            .selectOnlineId(offline_id)
                                    Log.d(
                                        "已同步至服务器",
                                        "operation:add,offline_id:${asyn.offlineId},online_id:${online_id},时间：${Date().getNowDateTime()}"
                                    )
                                }
                            }
                        })
                    }

                    "update" -> {
                        //获取note表中对应的记录
                        val notes =
                            NotesDatabase.getDatabase(MyApplication.context).noteDao()
                                .getSpecificNote(offline_id!!)

                        //取对应的online_id
                        val online_id = asyn.onlineId;
                        //将实体数据转化为json字符串
                        var jsonString = formatJson(Gson().toJson(notes))
                        var url =
                            "http://10.236.11.105:8080/portal/notes/updateNote.do?onlineId=${online_id}"
                        OKHttpUtils.post(url, jsonString, object : OKHttpCallback() {
                            override fun onFinish(status: String, msg: String) {
                                super.onFinish(status, msg)
                                val turnsType = object :
                                    TypeToken<ServerResponse<Int>>() {}.type
                                val jsobj =
                                    Gson().fromJson<ServerResponse<Int>>(
                                        result,
                                        turnsType
                                    )
                                //接收返回的Notes的onlie_id写入idmap
                                val status = jsobj.status
                                if (status != 0) {
                                    Log.d(
                                        "update至服务器失败",
                                        "notes:${Gson().toJson(notes)},时间：${Date().getNowDateTime()}"
                                    )
                                } else {
                                    val online_Id = jsobj.data
                                    //删除asyn表中的这条记录
                                    NotesDatabase.getDatabase(MyApplication.context)
                                        .asynDao()
                                        .deleteOneByOnlineId(online_id!!)
                                    Log.d(
                                        "已同步至服务器",
                                        "operation:update,online_id:${asyn.onlineId}"
                                    )
                                }
                            }
                        })
                    }
                    "delete" -> {
                        val online_id = asyn.onlineId
                        //后端同步删除
                        OKHttpUtils.get(
                            "http://10.236.11.105:8080/portal/notes/deleteNote.do?onlineId=${online_id}",
                            object : OKHttpCallback() {
                                override fun onFinish(status: String, msg: String) {
                                    super.onFinish(status, msg)
                                    val turnsType =
                                        object : TypeToken<ServerResponse<Int>>() {}.type
                                    val jsobj =
                                        Gson().fromJson<ServerResponse<Int>>(result, turnsType)
                                    //接收返回的Notes的onlie_id写入idmap
                                    val status = jsobj.status
                                    if (status != 0) {
                                        Log.d("delete至服务器失败", "onineId:${jsobj.data}")
                                    } else {
                                        val online_Id = jsobj.data
                                        //删除asyn表中的这条记录
                                        NotesDatabase.getDatabase(MyApplication.context)
                                            .asynDao()
                                            .deleteOneByOnlineId(online_id!!)
                                        Log.d(
                                            "已同步至服务器",
                                            "operation:delete,online_id:${asyn.onlineId}"
                                        )
                                    }
                                }
                            })
                    }
                }
            }
        }
        return Result.success()
    }


//                                    }
//                                })
//            /            }
    //     }
    //  }
    //   }


    //  }
    //数据实体转化位
    fun formatJson(content: String): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonParser = JsonParser()
        val jsonElement = jsonParser.parse(content)
        return gson.toJson(jsonElement)
    }

}

