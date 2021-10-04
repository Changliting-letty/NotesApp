package com.lettytrain.notesapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lettytrain.notesapp.adapter.NotesAdapter
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.Notes
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 主界面
 */
class NotesHomeFragment :BaseFragment() {

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
                var notes=NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                recycler_view.adapter=NotesAdapter(notes)
            }
        }
        fabBtnCreateNote.setOnClickListener{
            replaceFragment(CreateNoteFragment())
        }
    }
    fun replaceFragment(fragment: Fragment){
        val fragmentTransaction=activity!!.supportFragmentManager.beginTransaction()
//        if (isTransaction){
//            fragmentTransaction.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
//        }
        //实现返回栈

        fragmentTransaction.replace(R.id.drawerLayout,fragment).addToBackStack(fragment.javaClass.simpleName)
        fragmentTransaction.commit()
    }
}
