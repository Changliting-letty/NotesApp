package com.lettytrain.notesapp.vo

import com.lettytrain.notesapp.entities.Notes

/**
 * author: Chang Liting
 * created on: 2021/11/26 16:22
 * description:
 */
class RequestData{
    var online_id=-1
    var offline_id=-1
    var operation=""
    var data: NotesVo?=null
    var update_time:String=""
    var desc=""
}