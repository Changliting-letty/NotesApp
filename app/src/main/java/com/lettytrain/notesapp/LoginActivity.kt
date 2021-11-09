package com.lettytrain.notesapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lettytrain.notesapp.entities.ResponseCode
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import com.lettytrain.notesapp.util.SharedPreferencesUtil
import com.lettytrain.notesapp.vo.ServerResponse
import com.lettytrain.notesapp.vo.UserVo
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.password
import kotlinx.android.synthetic.main.activity_login.username


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //记住密码功能
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val isRemember = prefs.getBoolean("remember_pass", false)
        if (isRemember) {
            //将用户名和密码都设置到文本框中
            val user_name = prefs.getString("userName", "")
            val pass_word = prefs.getString("passWord", "")
            username.setText(user_name)
            password.setText(pass_word)
            rememberPass.isChecked = true
        }
        login.setOnClickListener {
            //判空
            if (username.text.isNullOrEmpty()) {
                Toast.makeText(this, "UserName is Requeried", Toast.LENGTH_SHORT).show()
            }
            if (password.text.isNullOrEmpty()) {
                Toast.makeText(this, "Password is  Requeried", Toast.LENGTH_SHORT).show()
            } else {
                OKHttpUtils.get(
                    "http://10.236.11.105:8080/portal/user/login.do?username=${username.text}&password=${password.text}",
                    object : OKHttpCallback() {
                        override fun onFinish(status1: String, result: String) {
                            super.onFinish(status1, result)
                            //解析数据
                            val turnsType = object : TypeToken<ServerResponse<UserVo>>() {}.type
                            val jsobj = Gson().fromJson<ServerResponse<UserVo>>(result, turnsType)
                            val status = jsobj.status
                            if (status ==ResponseCode.USERNAME_NOT_EXISTS.num) {
                                Looper.prepare()
                                Toast.makeText(
                                    MyApplication.context,
                                    "The UserName doesn't exist!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                username.setText("")
                                password.setText("")
                                Looper.loop()
                            }
                            //密码错误
                            if (status == ResponseCode.PASSWORD_NOT_Right.num) {
                                Looper.prepare()
                                Toast.makeText(
                                    MyApplication.context,
                                    "The Password is wrong!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                password.setText("")
                                Looper.loop()
                            } else if (status == ResponseCode.IS_LOGIN_SUCCESS.num) {
                                //如果记住密码复选框被选中
                                Looper.prepare()
                                if (rememberPass.isChecked) {
                                    editor.putBoolean("remember_pass", true)
                                    editor.putString("userName", username.text.toString())
                                    editor.putString("passWord", password.text.toString())
                                } else {
                                    editor.clear()
                                }
                                editor.apply()
                                SharedPreferencesUtil.putBoolean("isLogin",true)
                                println(Gson().toJson(jsobj.data))
                                SharedPreferencesUtil.putString("user",Gson().toJson(jsobj.data))
                                val intent = Intent(MyApplication.context, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                                Looper.loop()
                            }
                            else{
                                //离线操作
                                val intent = Intent(MyApplication.context, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    })
            }
        }
        to_sign_up.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}
