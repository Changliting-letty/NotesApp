package com.lettytrain.notesapp.entities

/**
 * author: Chang Liting
 * created on: 2021/11/12 15:20
 * description:
 */
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "idmap")
class IdMap:Serializable{
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null
    @ColumnInfo(name="userId")
    var userId:Int?= null
    @ColumnInfo(name="offlineId")
    var offlineId:Int?= null
    @ColumnInfo(name="onlineId")
    var onlineId:Int?= null
}