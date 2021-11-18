package com.lettytrain.notesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lettytrain.notesapp.dao.AsynDao
import com.lettytrain.notesapp.dao.IdMapDao
import com.lettytrain.notesapp.dao.NoteDao
import com.lettytrain.notesapp.entities.Asyn
import com.lettytrain.notesapp.entities.IdMap
import com.lettytrain.notesapp.entities.Notes


@Database(entities = [Notes::class,IdMap::class, Asyn::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    companion object {
        var notesDatabase: NotesDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): NotesDatabase {
            if (notesDatabase == null) {
                notesDatabase = Room.databaseBuilder(
                    context
                    , NotesDatabase::class.java
                    , "notes.db"
                ).build()
            }
            return notesDatabase!!
        }
    }

    abstract fun noteDao():NoteDao

    abstract fun idmapDao():IdMapDao

    abstract  fun  asynDao():AsynDao

}