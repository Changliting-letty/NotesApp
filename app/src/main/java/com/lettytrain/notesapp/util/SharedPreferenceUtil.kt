package com.lettytrain.notesapp.util


import android.content.Context
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.lettytrain.notesapp.MyApplication
import com.lettytrain.notesapp.vo.UserVo
import java.lang.NullPointerException
import java.util.*
import kotlin.reflect.KClass

object SharedPreferenceUtil {
    val default =
        "{\"createTime\":\"2021-00-00 15:00:00\",\"updateTime\":\"2021-00-00 00:00:00\",\"userId\":-1,\"userName\":\"none\"}"
    val sp = MyApplication.context.getSharedPreferences(
        "session",
        Context.MODE_PRIVATE
    )
    val edit = sp.edit()
    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
    val sp_encry = EncryptedSharedPreferences.create(
        "encrypted_share",
        masterKeyAlias,
        MyApplication.context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    val edit_encry = sp_encry.edit()

    fun putBoolean(key: String, value: Boolean) {
        edit.putBoolean(key, value)
        edit.apply()
    }

    fun putString(key: String, value: String) {
        edit.putString(key, value)
        edit.apply()
    }

    fun putInt(key: String, value: Int) {
        edit.putInt(key, value)
        edit.apply()
    }

    fun readInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    fun readBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)

    }

    fun readString(key: String): String? {
        return sp.getString(key, "")
    }

    fun <T> readObject(key: String, clazz: Class<T>): T {
        val str = sp.getString(key, default)
        val userVo = Gson().fromJson(str, clazz)
        return userVo
    }

    fun delete(key: String) {
        edit.remove(key).apply()
    }

    fun clear() {
        edit.clear()
        edit.apply()
        edit_encry.clear()
        edit_encry.apply()
    }

    //设置加密存储
    fun putStringEncrypted(key: String, value: String) {
        edit_encry.putString(key, value)
        edit_encry.apply()
    }

    fun readStringEncypted(key: String): String? {
        return sp_encry.getString(key, "somethind encrypted")
    }

}