package com.lettytrain.notesapp.util

import android.util.Log
import org.jetbrains.annotations.TestOnly
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

/**
 * author: Chang Liting
 * created on: 2021/11/16 20:58
 * description:
 */

fun Date.getNowDateTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    return sdf.format(this)
}

/**
 *  比较时间先后
 *  时间格式  2021-11-18  11:21:08
 *  return: >0 服务端更新
 *          <0  app端 更新
 *          =0  一致
 * */
fun isRemoteMoreLast(remoteTime: String, localTime: String): Int {
    val remote_date = remoteTime.split("-", " ", ":")
    val re_year = remote_date[0]
    val re_mon = remote_date[1]
    val re_day = remote_date[2]
    val re_h = remote_date[3]
    val re_min = remote_date[4]
    val re_sec = remote_date[5]
    val local_date = localTime.split("-", " ", ":")
    val local_year = local_date[0]
    val local_mon = local_date[1]
    val local_day = local_date[2]
    val local_h = local_date[3]
    val local_min = local_date[4]
    val local_sec = local_date[5]
    val remote = (re_year + re_mon + re_day + re_h + re_min + re_sec).toLong()
    val local = (local_year + local_mon + local_day + local_h + local_min + local_sec).toLong()
    return remote.compareTo(local)
}