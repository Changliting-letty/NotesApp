package com.lettytrain.notesapp.vo

import com.lettytrain.notesapp.entities.Notes

class RequestData {
    var online_id = -1
    var offline_id = -1
    var operation = ""
    var data: NotesVo? = null
    var update_time: String = ""
    var desc = ""
}