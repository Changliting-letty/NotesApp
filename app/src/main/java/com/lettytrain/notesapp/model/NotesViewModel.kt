package com.lettytrain.notesapp.model

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.entities.Notes
import kotlinx.coroutines.Job

class NotesViewModel() : ViewModel() {
    var loalLivedata: LiveData<List<Notes>>


    init {

        loalLivedata = NotesDatabase.getDatabase(MyApplication.context).noteDao().getNotesList()
    }

}