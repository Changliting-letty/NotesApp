package com.lettytrain.notesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notes")
class Notes: Serializable {

    @PrimaryKey(autoGenerate = true)
    var id:Int? = null
    @ColumnInfo(name="userId")
    var userId:Int?= null

    @ColumnInfo(name = "title")
    var title:String? = null

    @ColumnInfo(name = "sub_title")
    var subTitle:String? = null

    @ColumnInfo(name = "create_time")
    var createTime:String? = null

    @ColumnInfo(name="update_time")
    var updateTime:String?=null

    @ColumnInfo(name = "note_text")
    var noteText:String? = null

    @ColumnInfo(name = "img_path")
    var imgPath:String? = null

    @ColumnInfo(name = "web_link")
    var webLink:String? = null

    @ColumnInfo(name = "color")
    var color:String? = null

}