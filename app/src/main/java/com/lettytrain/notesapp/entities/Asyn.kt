package com.lettytrain.notesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*


@Entity(tableName = "asyn")
class Asyn : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    @ColumnInfo(name = "userId")
    var userId: Int? = null
    @ColumnInfo(name = "offlineId")
    var offlineId: Int? = null
    @ColumnInfo(name = "onlineId")
    var onlineId: Int? = null
    @ColumnInfo(name = "operation")
    var operation: String? = null
    @ColumnInfo(name = "time")
    var time: String? = null
}