package com.lettytrain.notesapp.dao

import android.provider.ContactsContract
import androidx.room.*
import com.lettytrain.notesapp.entities.Notes

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    suspend fun getAllNotes(): List<Notes>

    @Query("SELECT * FROM notes where userId=:userId ORDER BY id DESC")
    suspend fun getAllNotesByUserId(userId: Int): List<Notes>

    @Query("SELECT * FROM notes WHERE id =:id")
    fun getSpecificNote(id: Int): Notes

    @Query("SELECT title FROM notes WHERE id=:id")
    suspend fun getNoteTitle(id: Int): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(note: Notes): Long

    @Delete
    suspend fun deleteNote(note: Notes)

    @Query("DELETE FROM notes WHERE id =:id")
    suspend fun deleteSpecificNote(id: Int)

    @Update
    suspend fun updateNote(note: Notes)
}