package com.lettytrain.notesapp

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.se.omapi.Session
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lettytrain.notesapp.adapter.NotesAdapter
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.Asyn
import com.lettytrain.notesapp.entities.IdMap
import com.lettytrain.notesapp.entities.Notes
import com.lettytrain.notesapp.entities.User
import com.lettytrain.notesapp.util.NoteBottemSheetFragment.Companion.noteId
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import com.lettytrain.notesapp.util.SharedPreferenceUtil
import com.lettytrain.notesapp.util.isRemoteMoreLast
import com.lettytrain.notesapp.vo.NotesVo
import com.lettytrain.notesapp.vo.ServerResponse
import com.lettytrain.notesapp.vo.UserVo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.coroutines.*
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.support.v4.progressDialog
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.uiThread
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/**
主界面
 */
class NotesHomeFragment : BaseFragment() {

    var arrNotes = ArrayList<Notes>()
    var notesAdapter: NotesAdapter = NotesAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            NotesHomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("onViewCreated", "执行一次")
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        if (SharedPreferenceUtil.readBoolean("isLogin") && SharedPreferenceUtil.readBoolean("isLoginFirst")) {
            SharedPreferenceUtil.putBoolean("isLoginFirst", false)
            initData()

        }
        launch {

            val user = SharedPreferenceUtil.readObject("user", UserVo::class.java) as UserVo
            if (user.userId != -1) {
                var notes = withContext(Dispatchers.IO) {
                    NotesDatabase.getDatabase(MyApplication.context).noteDao()
                        .getAllNotesByUserId(user.userId!!)
                }
                notesAdapter!!.setData(notes)
                arrNotes = notes as ArrayList<Notes>
                recycler_view.adapter = notesAdapter
            }

        }

        notesAdapter.setOnClickListener(onClicked)
        fabBtnCreateNote.setOnClickListener {
            replaceFragment(CreateNoteFragment.newInstance())
        }
        //搜索笔记

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                var tempArr = ArrayList<Notes>()
                for (arr in arrNotes) {
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(newText.toString())) {
                        tempArr.add(arr)
                    }
                }
                notesAdapter.setData(tempArr)
                notesAdapter.notifyDataSetChanged()
                return true
            }
        })

    }

    private val onClicked = object : NotesAdapter.OnItemClickListener {
        override fun onClicked(noteId: Int) {
            var fragment: Fragment
            var bundle = Bundle()  //用于传递数据
            bundle.putInt("noteId", noteId)
            Log.d("createonclick", "notesId${noteId}")
            fragment = CreateNoteFragment.newInstance()
            fragment.arguments = bundle
            replaceFragment(fragment)
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.drawerLayout, fragment)
        fragmentTransaction.commit()
    }

    //    private fun  savedToLocalDb(list:List<NotesVo>) {
    //版本1
//        for (notesVo in list) {
//            var note = Notes()
//            note.userId = notesVo.userId
//            note.title = notesVo.title
//            note.subTitle = notesVo.subTitle
//            note.noteText = notesVo.noteText
//            note.imgPath = notesVo.imgPath
//            note.webLink = notesVo.webLink
//            note.color = notesVo.color
//            launch {
//                var id= withContext(Dispatchers.IO ){
//                    NotesDatabase.getDatabase(MyApplication.context).noteDao().insertNotes(note)
//                }
//                var idmap = IdMap()
//                idmap.offlineId = id.toInt()
//                idmap.onlineId = notesVo.id
//                val idmap_id=withContext(Dispatchers.IO){
//                    NotesDatabase.getDatabase(MyApplication.context).idmapDao().insertMap(idmap)
//                }
//            }
//
//
//        }
//    }
    private fun savedToLocalDb(list: List<NotesVo>) {
        launch {
            //2.已经获取了服务端的所有notes，与本地已经有的note进行比较
            val user = SharedPreferenceUtil.readObject("user", UserVo::class.java)
            var idmaps = withContext(Dispatchers.IO) {
                NotesDatabase.getDatabase(MyApplication.context).idmapDao().selectAll(user.userId!!)
            }
            if (idmaps.size == 0) {
                //本地没有任何notes的情况，直接将服务端的所有notes写入本地
                localNoteExits(list)
                // Log.d("从服务端更新","本地没有idmap")
            } else {
//            //本地存在notes的情况
//            //收集本地存在的notes的id,与服务端的notes进行比较
                var localNoteExitList = ArrayList<NotesVo>()
                var local_onlineId_list = ArrayList<Int>()
                for (idmap in idmaps) {
                    //online_id为-1即本地存在，服务端不存在的情况不用在此处理，后期会定期更新到服务端
                    if (idmap.onlineId != -1) {
                        local_onlineId_list.add(idmap.onlineId!!)
                    }
                }
//            //比较
                for (noteVo in list) {
                    if (local_onlineId_list.contains(noteVo.id)) {
                        //2.1 本地和服务端都有，比较谁更新
                        //这里加上withcontex就出错
                        var noteLocalId =
                            NotesDatabase.getDatabase(MyApplication.context).idmapDao()
                                .selectOfflineId(noteVo.id!!)
                        var noteInLocal = withContext(Dispatchers.IO) {
                            NotesDatabase.getDatabase(MyApplication.context).noteDao()
                                .getSpecificNote(noteLocalId)
                        }
                        var remoteTime = noteVo.dateTime
                        var localTime = noteInLocal.updateTime
                        val compareRes = isRemoteMoreLast(remoteTime!!, localTime!!)
                        if (compareRes > 0) {
                            //服务端更新，覆盖本地的
                            Log.d("从服务端更新", "服务端的时间最新，覆盖本地的note")
                            noteInLocal.title = noteVo.title
                            noteInLocal.subTitle = noteVo.subTitle
                            noteInLocal.noteText = noteVo.noteText
                            noteInLocal.createTime = noteVo.createTime
                            noteInLocal.updateTime = noteVo.dateTime
                            noteInLocal.imgPath = noteVo.imgPath
                            noteInLocal.webLink = noteVo.webLink
                            noteInLocal.color = noteVo.color
                            withContext(Dispatchers.IO) {
                                NotesDatabase.getDatabase(MyApplication.context).noteDao()
                                    .updateNote(noteInLocal)
                            }
                        } else if (compareRes < 0) {
                            //本地更新，追加update记录到asyn表
                            Log.d("从服务端更新", "本地的时间最新，追加asyn记录用于同步到服务端")
                            var asyn = Asyn()
                            asyn.userId = user.userId
                            asyn.offlineId = noteId
                            asyn.onlineId = noteVo.id
                            asyn.operation = "update"
                            asyn.time = noteInLocal.updateTime
                            withContext(Dispatchers.IO) {
                                NotesDatabase.getDatabase(MyApplication.context).asynDao()
                                    .insertOne(asyn)
                            }
                        } else {
                            Log.d("从服务端更新", "时间一致,do nothing.")
                        }
                    } else {
                        //2.2本地没有这个notes，直接保存到本地
                        localNoteExitList.add(noteVo)
                    }
                }
                if (localNoteExitList.size > 0) {
                    localNoteExits(localNoteExitList)
                }
            }
            val intent = Intent(MyApplication.context, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun localNoteExits(list: List<NotesVo>) {
        for (notesVo in list) {
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
            launch {
                var id = withContext(Dispatchers.IO) {
                    NotesDatabase.getDatabase(MyApplication.context).noteDao().insertNotes(note)
                }
                var idmap = IdMap()
                idmap.userId = notesVo.userId
                idmap.offlineId = id.toInt()
                idmap.onlineId = notesVo.id
                val idmap_id = withContext(Dispatchers.IO) {
                    NotesDatabase.getDatabase(MyApplication.context).idmapDao().insertMap(idmap)
                }
            }
        }
    }

    fun uiUpdate(notesVoList: List<NotesVo>) {
        launch {
            savedToLocalDb(notesVoList)
        }
        //适用于第一次用本设备进行登录的情况
        launch {
            val notes=ArrayList<Notes>()
            for (notesVo in notesVoList) {
                var note = Notes()
                note.userId = notesVo.userId
                note.title = notesVo.title
                note.subTitle = notesVo.subTitle
                note.noteText = notesVo.noteText
                note.imgPath = notesVo.imgPath
                note.webLink = notesVo.webLink
                note.color = notesVo.color
                notes.add(note)
            }
            notesAdapter!!.setData(notes)
            arrNotes = notes
            recycler_view.adapter = notesAdapter
            recycler_view.adapter = notesAdapter
        }
    }

    fun initData() {
        launch(Dispatchers.IO) {
            OKHttpUtils.get(
                "http://10.236.11.105:8080/portal/notes/searchAllNotes.do",
                object : OKHttpCallback() {
                    override fun onFinish(status1: String, result: String) {
                        super.onFinish(status1, result)
                        //执行成功才执行一下逻辑
                        if (status1.equals("success")) {
                            val turnsType =
                                object : TypeToken<ServerResponse<List<NotesVo>>>() {}.type
                            val jsobj = Gson().fromJson<ServerResponse<List<NotesVo>>>(
                                result,
                                turnsType
                            )
                            val noteslist = jsobj.data as List<NotesVo>
                            savedToLocalDb(noteslist)
//                            runOnUiThread {
//                                uiUpdate(noteslist)
//                            }
                        }
                    }
                }
            )
        }

    }

}



