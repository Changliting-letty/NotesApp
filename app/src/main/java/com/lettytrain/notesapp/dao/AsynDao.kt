package com.lettytrain.notesapp.dao

import androidx.room.*
import com.lettytrain.notesapp.entities.Asyn


@Dao
interface AsynDao {
    @Query("SELECT * FROM asyn where userId=:userId")
    fun selectAll(userId:Int): List<Asyn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOne(asyn: Asyn):Long

    @Delete
    fun deleteOne(asyn: Asyn)

    @Query("DELETE FROM asyn where offlineId=:offline_id ")
    fun deleteByOfflineId(offline_id: Int)

    @Query("DELETE FROM asyn where onlineId=:online_id ")
    fun deleteOneByOnlineId(online_id: Int)

    @Query("SELECT COUNT(*) FROM asyn  WHERE  offlineId=:offline_id")
    fun isExist(offline_id: Int): Int

    @Query("UPDATE asyn SET time=:newTime WHERE offlineId=:offline_id")
    fun updateTime(offline_id: Int, newTime: String)

    @Query("DELETE FROM asyn")
    fun  clearTable()

}