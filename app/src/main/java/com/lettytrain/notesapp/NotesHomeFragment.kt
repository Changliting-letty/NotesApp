package com.lettytrain.notesapp

import android.app.ProgressDialog
import android.content.Context
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lettytrain.notesapp.adapter.NotesAdapter
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.IdMap
import com.lettytrain.notesapp.entities.Notes
import com.lettytrain.notesapp.entities.User
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import com.lettytrain.notesapp.util.SharedPreferenceUtil
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
        Log.d("onViewCreated","执行一次")
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        if (SharedPreferenceUtil.readBoolean("isLogin") && SharedPreferenceUtil.readBoolean("isLoginFirst")){
                    SharedPreferenceUtil.putBoolean("isLoginFirst",false)
                    initData()

        }
        launch {

                    val user = SharedPreferenceUtil.readObject("user",UserVo::class.java) as UserVo
                    if(user.userId!=-1){
                        var notes= withContext(Dispatchers.IO){
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
            Log.d("createonclick","notesId${noteId}")
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
    private fun  savedToLocalDb(list:List<NotesVo>) {
        for (notesVo in list) {
            var note = Notes()
            note.userId = notesVo.userId
            note.title = notesVo.title
            note.subTitle = notesVo.subTitle
            note.noteText = notesVo.noteText
            note.imgPath = notesVo.imgPath
            note.webLink = notesVo.webLink
            note.color = notesVo.color
            launch {
                var id= withContext(Dispatchers.IO ){
                    NotesDatabase.getDatabase(MyApplication.context).noteDao().insertNotes(note)
                }
                var idmap = IdMap()
                idmap.offlineId = id.toInt()
                idmap.onlineId = notesVo.id
                val idmap_id=withContext(Dispatchers.IO){
                    NotesDatabase.getDatabase(MyApplication.context).idmapDao().insertMap(idmap)
                }
            }


        }
    }
    fun uiUpdate(notesVoList:List<NotesVo>){
        launch {
            savedToLocalDb(notesVoList)
        }
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
    fun  initData(){
        launch(Dispatchers.IO) {
            OKHttpUtils.get(
                "http://10.236.11.105:8080/portal/notes/searchAllNotes.do",
                object : OKHttpCallback() {
                    override fun onFinish(status1: String, result: String) {
                        super.onFinish(status1, result)
                        //执行成功才执行一下逻辑
                        if (status1.equals("success")){
                            val turnsType =
                                object : TypeToken<ServerResponse<List<NotesVo>>>() {}.type
                            val jsobj = Gson().fromJson<ServerResponse<List<NotesVo>>>(
                                result,
                                turnsType
                            )
                            if (jsobj.status==0){
                                val noteslist = jsobj.data as List<NotesVo>
                                runOnUiThread {
                                    uiUpdate(noteslist)
                                }
                            }
                        }
                    }
                }
            )
        }

        }

}



