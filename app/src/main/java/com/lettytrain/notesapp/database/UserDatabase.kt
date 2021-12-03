package com.lettytrain.notesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lettytrain.notesapp.dao.UserDao
import com.lettytrain.notesapp.entities.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    companion object {
        var userDatabase: com.lettytrain.notesapp.database.UserDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): com.lettytrain.notesapp.database.UserDatabase {
            if (userDatabase == null) {
                userDatabase = Room.databaseBuilder(
                    context
                    , com.lettytrain.notesapp.database.UserDatabase::class.java
                    , "users.db"
                ).build()
            }
            return userDatabase!!
        }
    }

    abstract fun userDao(): UserDao


}