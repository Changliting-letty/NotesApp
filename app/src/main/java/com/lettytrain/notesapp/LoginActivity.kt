package com.lettytrain.notesapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lettytrain.notesapp.entities.ResponseCode
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import com.lettytrain.notesapp.util.SharedPreferenceUtil
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

        val isRemember = SharedPreferenceUtil.readBoolean("remember_pass")
        if (isRemember) {
            //将用户名和密码都设置到文本框中
            val user_name = SharedPreferenceUtil.readString("userName")
            val pass_word = SharedPreferenceUtil.readString("passWord")
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
                            } else if (status == ResponseCode.IS_SUCCESS.num) {
                                //登录成功
                                //如果记住密码复选框被选中

                                Looper.prepare()
                                println("登录成功")
                                if (rememberPass.isChecked) {
                                    SharedPreferenceUtil.putBoolean("remember_pass", true)
                                    SharedPreferenceUtil.putString("userName", username.text.toString())
                                    SharedPreferenceUtil.putString("passWord", password.text.toString())
                                }
                                //判断是否是初次登录，
                                val user = SharedPreferenceUtil.readObject("user",UserVo::class.java) as UserVo
                                if (user.userId==-1){
                                    //初次登录，拉取数据库上所有该用户的数据到本地
                                    println("初次登录")
                                    SharedPreferenceUtil.putBoolean("isLoginFirst",true)

                                }else{
                                    //非初次登录，执行同步
                                    println("非初次登录")
                                    SharedPreferenceUtil.putBoolean("isLoginFirst",false)
                                }
                                SharedPreferenceUtil.putBoolean("isLogin",true)
                                println(Gson().toJson(jsobj.data))
                                SharedPreferenceUtil.putString("user",Gson().toJson(jsobj.data))
                                val intent = Intent(MyApplication.context, MainActivity::class.java)
                                startActivity(intent)
                            }
                            else{
                                //离线操作
                                val user=SharedPreferenceUtil.readObject("user",UserVo::class.java)
                                if(user.userId!=-1){
                                    Log.d("LoginActivity","登录失败,用户${user.userName}将进行离线操作")
                                }else{
                                    Log.d("LoginActivity","登录失败,您之后的操作将不会被保存")
                                }
                                val intent = Intent(MyApplication.context, MainActivity::class.java)
                                startActivity(intent)

                           }
                            Looper.loop()
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
