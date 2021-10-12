package com.lettytrain.notesapp

import android.content.Context
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Bundle
import android.se.omapi.Session
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lettytrain.notesapp.adapter.NotesAdapter
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.Notes
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 主界面
 */
class NotesHomeFragment :BaseFragment() {

    var arrNotes=ArrayList<Notes>()
    var notesAdapter:NotesAdapter=NotesAdapter()


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

        recycler_view.setHasFixedSize(true)

        recycler_view.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        launch {
            context?.let{
//                val bundle=arguments
//                var userId=bundle?.getInt("userId",-1)
                val prefs=MyApplication.context.getSharedPreferences("session",Context.MODE_PRIVATE)
                val userId=prefs.getInt("userId",-1)
                var notes=NotesDatabase.getDatabase(it).noteDao().getAllNotesByUserId(userId!!)
                //var notes=NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                notesAdapter!!.setData(notes)

                arrNotes=notes as ArrayList<Notes>
                recycler_view.adapter=notesAdapter
            }
        }
        notesAdapter.setOnClickListener(onClicked)
        fabBtnCreateNote.setOnClickListener{
//            val userId=savedInstanceState?.getInt("userId",-1)
//            var fragment:Fragment
//            var bundle=Bundle()  //用于传递数据
//            if (userId != null) {
//                bundle.putInt("userId",userId)
//            }
//            fragment=CreateNoteFragment.newInstance()
//            fragment.arguments=bundle
//            replaceFragment(fragment)
            replaceFragment(CreateNoteFragment.newInstance())
        }
        //搜索笔记

        search_view.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                var tempArr=ArrayList<Notes>()
                for (arr in arrNotes){
                    if (arr.title!!.toLowerCase(Locale.getDefault()).contains(newText.toString())){
                        tempArr.add(arr)
                    }
                }
                notesAdapter.setData(tempArr)
                notesAdapter.notifyDataSetChanged()
                return true
            }
        })

    }
    fun replaceFragment(fragment: Fragment){
        val fragmentTransaction=activity!!.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.drawerLayout,fragment).addToBackStack(fragment.javaClass.simpleName)
        fragmentTransaction.commit()
    }
    private  val  onClicked=object :NotesAdapter.OnItemClickListener{
        override fun onClicked(noteId: Int) {
            var fragment:Fragment
            var bundle=Bundle()  //用于传递数据
            bundle.putInt("noteId",noteId)
            fragment=CreateNoteFragment.newInstance()
            fragment.arguments=bundle
            replaceFragment(fragment)
        }
    }
}
