package com.lettytrain.notesapp.util


import android.content.Context
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.gson.Gson
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.vo.UserVo
import java.lang.NullPointerException
import java.util.*
import kotlin.reflect.KClass
/**
 * author: Chang Liting
 * created on: 2021/11/12 9:13
 * description:
 */
object SharedPreferenceUtil {

        val default="{\"createTime\":\"2021-00-00 15:00:00\",\"updateTime\":\"2021-00-00 00:00:00\",\"userId\":-1,\"userName\":\"none\"}"
        val sp = MyApplication.context.getSharedPreferences(
            "session",
            Context.MODE_PRIVATE
        )
        val edit = sp.edit()
        fun putBoolean(key: String, value: Boolean) {
            edit.putBoolean(key, value)
            edit.apply()
        }

        fun putString(key: String, value: String) {
            edit.putString(key, value)
            edit.apply()
        }

        fun readBoolean(key: String): Boolean {
            return sp.getBoolean(key, false)

        }
        fun  readString(key:String):String?{
            return sp.getString(key,"")
        }
        fun<T> readObject(key: String, clazz:Class<T>): T {
            val str = sp.getString(key, default)
            val userVo = Gson().fromJson(str, clazz)
            return userVo
        }

        fun delete(key: String) {
            edit.remove(key).apply()
        }

        fun clear() {
//            putBoolean("isLogin",false)
//            putString("user", default)
            edit.clear()
            edit.apply()
        }

}