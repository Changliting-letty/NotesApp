package com.lettytrain.notesapp.model

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.Notes
import kotlinx.coroutines.Job

/**
 * author: Chang Liting
 * created on: 2021/11/29 18:43
 * description:
 */

class NotesViewModel(): ViewModel() {
    var  loalLivedata:LiveData<List<Notes>>


    init {

        loalLivedata=NotesDatabase.getDatabase(MyApplication.context).noteDao().getNotesList()
    }

    fun  updateLiveData(){

    }

}