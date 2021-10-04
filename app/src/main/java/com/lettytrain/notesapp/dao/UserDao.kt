package com.lettytrain.notesapp.dao

import androidx.room.*
import com.lettytrain.notesapp.entities.User


@Dao
interface UserDao {


    @Query("SELECT count(*) FROM users where user_name=:username")
    suspend fun getUsers(username: String) : Int

    @Query("SELECT password  FROM users WHERE user_name=:user_name")
  suspend  fun getPassword(user_name:String) : String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(user: User)


    @Query("DELETE FROM users WHERE user_name =:user_name")
   suspend  fun deleteSpecificUser(user_name:String)

    @Update
    suspend  fun updateNote(user:User)
}