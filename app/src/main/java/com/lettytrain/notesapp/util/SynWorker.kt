package com.lettytrain.notesapp.util

import android.content.Context
import android.content.Intent
import android.icu.text.LocaleDisplayNames
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.core.content.contentValuesOf
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lettytrain.notesapp.MainActivity
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.IdMap
import com.lettytrain.notesapp.entities.Notes
import com.lettytrain.notesapp.util.NoteBottemSheetFragment.Companion.noteId
import com.lettytrain.notesapp.vo.NotesVo
import com.lettytrain.notesapp.vo.RequestData
import com.lettytrain.notesapp.vo.ServerResponse
import com.lettytrain.notesapp.vo.UserVo
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class SynWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        var request_list = getLocalChanges()
        val jsonString = formatJson(Gson().toJson(request_list))
        val lastSynTime = SharedPreferenceUtil.readString("lastSynTime")
        val url = "http://161.97.110.236:8080/portal/notes/syn.do?lastSynTime=${lastSynTime}"
        OKHttpUtils.post(url, jsonString, object : OKHttpCallback() {
            override fun onFinish(status: String, msg: String) {
                super.onFinish(status, msg)
                if (status.equals("success")) {
                    val turnsType =
                        object : TypeToken<ServerResponse<List<RequestData>>>() {}.type
                    val jsobj = Gson().fromJson<ServerResponse<List<RequestData>>>(
                        result,
                        turnsType
                    )
                    if (jsobj.status == 0) {
                        if (jsobj.data != null) {
                            val requestData_list = jsobj.data as List<RequestData>
                            updateToLocal(requestData_list)
                        }
                        SharedPreferenceUtil.putString("lastSynTime", jsobj.timeNow!!)
                        NotesDatabase.getDatabase(MyApplication.context).asynDao().clearTable()
                    } else {
                        Log.d("Synworker", "本次更新失败，累积到下次同步")
                    }
                } else {
                    Log.d("Synworker", "网络链接失败，本次更新失败，累积到下次同步")
                    val userVo = SharedPreferenceUtil.readObject("user", UserVo::class.java)
                    val asyn_list = NotesDatabase.getDatabase(MyApplication.context).asynDao()
                        .selectAll(userVo.userId!!)
                    Log.d("Synworker", "asyn长度 ${asyn_list.size}")
                }
            }
        })
        Log.d("Synworker", "自动与服务端同步一次,时间：${Date().getNowDateTime()}")
        return Result.success()
    }

    fun updateToLocal(requestDatas: List<RequestData>) {
        for (item in requestDatas) {
            val jstemp = formatJson(Gson().toJson(item))
            Log.d("SynWorker", "服务端需要更新到本地的部分 ${jstemp}")
            if (item.desc.equals("request")) {
                if (item.operation.equals("add")) {
                    val notesVo = item.data!!
                    val notes = converNotesVo(notesVo)
                    notes.id = null
                    var id =
                        NotesDatabase.getDatabase(MyApplication.context).noteDao()
                            .insertNotes(notes)
                    var idmap = IdMap()
                    idmap.userId = notes.userId
                    idmap.offlineId = id.toInt()
                    idmap.onlineId = notesVo.id
                    NotesDatabase.getDatabase(MyApplication.context).idmapDao().insertMap(idmap)
                } else if (item.operation.equals("update")) {
                    val notesVo = item.data!!
                    val offline_Id = NotesDatabase.getDatabase(MyApplication.context).idmapDao()
                        .selectOfflineId(notesVo.id!!)
                    var notes =
                        NotesDatabase.getDatabase(MyApplication.context).noteDao()
                            .getSpecificNote(offline_Id)
                    notes.title = notesVo.title
                    notes.subTitle = notesVo.subTitle
                    notes.noteText = notesVo.noteText
                    notes.updateTime = notesVo.dateTime
                    notes.color = notesVo.color
                    notes.imgPath = notesVo.imgPath
                    notes.webLink = notesVo.webLink
                    NotesDatabase.getDatabase(MyApplication.context).noteDao().updateNote(notes)

                } else {

                    val onlineId = item.online_id
                    val offline_id = NotesDatabase.getDatabase(MyApplication.context).idmapDao()
                        .selectOfflineId(onlineId)
                    NotesDatabase.getDatabase(MyApplication.context).noteDao()
                        .deleteSpecificNote(offline_id)
                    NotesDatabase.getDatabase(MyApplication.context).idmapDao()
                        .deleteSpecificmap(onlineId)
                }
            } else {
                //服务端针对客户端的更新给出的反馈
                if (item.operation.equals("add")) {
                    val online_id = item.online_id
                    val offline_id = item.offline_id
                    NotesDatabase.getDatabase(MyApplication.context).idmapDao()
                        .updateOnlineID(online_id, offline_id)
                }
            }

        }
    }

    fun converNotesVo(notesVo: NotesVo): Notes {
        var note = Notes()
        note.userId = notesVo.userId
        note.title = notesVo.title
        note.subTitle = notesVo.subTitle
        note.createTime = notesVo.createTime
        note.updateTime = notesVo.dateTime
        note.noteText = notesVo.noteText
        note.imgPath = notesVo.imgPath
        note.webLink = notesVo.webLink
        note.color = notesVo.color
        return note
    }

    fun reConverNotes(notes: Notes): NotesVo {
        var notesVo = NotesVo()
        notesVo.userId = notes.userId
        notesVo.title = notes.title
        notesVo.subTitle = notes.subTitle
        notesVo.createTime = notes.createTime
        notesVo.dateTime = notes.updateTime
        notesVo.noteText = notes.noteText
        notesVo.imgPath = notes.imgPath
        notesVo.webLink = notes.webLink
        notesVo.color = notes.color
        return notesVo
    }

    //收集client端的本地更新
    fun getLocalChanges(): ArrayList<RequestData> {
        var request_list = ArrayList<RequestData>()
        Log.d("SynWorker", "正在收集本地需要更改的内容")
        val userVo = SharedPreferenceUtil.readObject("user", UserVo::class.java)
        val asyns =
            NotesDatabase.getDatabase(MyApplication.context).asynDao().selectAll(userVo.userId!!)
        Log.d("SynWorker", "同步表记录数量：${asyns.size}")
        if (asyns.size == 0) {
            Log.d("SynWorker", "没有什么可同步的,时间：${Date().getNowDateTime()}")
        } else {
            for (asyn in asyns) {
                var operation = asyn.operation
                var offline_id = asyn.offlineId
                when (operation) {
                    "add" -> {
                        val notes = NotesDatabase.getDatabase(MyApplication.context).noteDao()
                            .getSpecificNote(offline_id!!)
                        var temp_data = RequestData()
                        temp_data.offline_id = offline_id
                        temp_data.data = reConverNotes(notes)
                        temp_data.operation = "add"
                        temp_data.update_time = asyn.time!!
                        temp_data.desc = "request"
                        request_list.add(temp_data)
                    }
                    "update" -> {
                        val notes =
                            NotesDatabase.getDatabase(MyApplication.context).noteDao()
                                .getSpecificNote(offline_id!!)
                        val online_id = asyn.onlineId
                        //针对还未上传到服务端的情况
                        var temp_data = RequestData()
                        temp_data.online_id = online_id!!
                        temp_data.offline_id = offline_id
                        temp_data.data = reConverNotes(notes)
                        temp_data.operation = "update"
                        temp_data.desc = "request"
                        temp_data.update_time = asyn.time!!
                        request_list.add(temp_data)
                    }
                    "delete" -> {
                        val online_id = asyn.onlineId
                        var temp_data = RequestData()
                        temp_data.offline_id = offline_id!!
                        temp_data.online_id = online_id!!
                        temp_data.operation = "delete"
                        temp_data.desc = "request"
                        temp_data.update_time = asyn.time!!
                        request_list.add(temp_data)
                    }
                }
            }

        }
        return request_list
    }

    fun formatJson(content: String): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonParser = JsonParser()
        val jsonElement = jsonParser.parse(content)
        return gson.toJson(jsonElement)
    }

}
