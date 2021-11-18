package com.lettytrain.notesapp.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * author: Chang Liting
 * created on: 2021/11/16 20:58
 * description:
 */

fun Date.getNowDateTime():String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(this)
}