package com.lettytrain.notesapp.vo


class ServerResponse<T> {
    var status: Int = -1
    var data: T? = null  //status为0时，将返回的数据封装到data
    var msg: String? = null  //提示信息
    var userId:Int=-1
    var timeNow:String?=null
}