package com.lettytrain.notesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "Users")
class User: Serializable {

    @PrimaryKey(autoGenerate = true)
    var id:Int? = null
    @ColumnInfo(name = "user_name")
    var user_name:String?=null

    @ColumnInfo(name = "password")
    var password:String?=null


}