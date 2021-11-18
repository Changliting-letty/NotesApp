package com.lettytrain.notesapp.dao

import androidx.room.*
import com.lettytrain.notesapp.entities.IdMap
import com.lettytrain.notesapp.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * author: Chang Liting
 * created on: 2021/11/12 15:29
 * description:
 */
@Dao
interface IdMapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMap(idmap: IdMap): Long

    @Query("SELECT * FROM  idmap where userId=:userId")
     fun selectAll(userId: Int): List<IdMap>

    @Query("DELETE FROM idmap WHERE  onlineId =:onlineId")
     fun deleteSpecificmap(onlineId: Int)

    @Query("SELECT onlineId from idmap where offlineId=:offlineid")
     fun selectOnlineId(offlineid: Int): Int

    @Query("SELECT offlineId from idmap where onlineId=:onlineid")
    fun selectOfflineId(onlineid: Int): Int
    @Query("SELECT * FROM IDMAP where offlineId=:note_id")
     fun selectIdmap(note_id:Int):IdMap

    @Delete
     fun delete(idMap:IdMap)

    @Query("UPDATE  idmap SET onlineId=:online_Id where offlineId=:offline_id ")
    fun updateOnlineID(online_Id:Int,offline_id:Int)
}