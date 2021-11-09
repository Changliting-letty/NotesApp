package com.lettytrain.notesapp.vo

/**
 * author: Chang Liting
 * created on: 2021/11/3 14:27
 * description:
 */
class ServerResponse <T>{
     var status: Int = -1
     var data: T? = null  //status为0时，将返回的数据封装到data
     var msg: String? = null  //提示信息
}