package com.lettytrain.notesapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
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
        //记住密码
        val isRemember = SharedPreferenceUtil.readBoolean("remember_pass")
        if (isRemember) {
            val user_name = SharedPreferenceUtil.readString("userName")
            val pass_word = SharedPreferenceUtil.readStringEncypted("passWord")
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
                    "http://161.97.110.236:8080/portal/user/login.do?username=${username.text}&password=${password.text}",
                    object : OKHttpCallback() {
                        override fun onFinish(status1: String, result: String) {
                            super.onFinish(status1, result)
                            //解析数据
                            if (status1.equals("failure")){
                                //离线操作
                                val user =
                                    SharedPreferenceUtil.readObject("user", UserVo::class.java)
                                var failure_time=SharedPreferenceUtil.readInt("login_failure_time")
                             if (failure_time<3){
                                 SharedPreferenceUtil.putInt("login_failure_time",failure_time+1)
                                 Looper.prepare()
                                 Toast.makeText(
                                     MyApplication.context,
                                     "登录超时，请重试",
                                     Toast.LENGTH_SHORT
                                 ).show()
                                 val intent = Intent(MyApplication.context, LoginActivity::class.java)
                                 startActivity(intent)
                                 Looper.loop()
                             }else{
                                 //连续登录失败超三次
                                 if (user.userId != -1) {
                                     Log.d("LoginActivity", "连续登录3次失败,用户${user.userName}将进行离线操作")
                                 } else {
                                     Log.d("LoginActivity", "连续登录3次失败,将进入离线模式，您之后的操作将不会被保存")
                                 }
                                 val intent = Intent(MyApplication.context, MainActivity::class.java)
                                 startActivity(intent)
                             }
                            }else{
                                //登录成功
                                SharedPreferenceUtil.putInt("login_failure_time",0)
                                val turnsType = object : TypeToken<ServerResponse<UserVo>>() {}.type
                                val jsobj = Gson().fromJson<ServerResponse<UserVo>>(result, turnsType)

                                val status = jsobj.status
                                if (status == ResponseCode.USERNAME_NOT_EXISTS.num) {
                                    Looper.prepare()
                                    Toast.makeText(
                                        MyApplication.context,
                                        "The UserName doesn't exist!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    username.setText("")
                                    password.setText("")
                                    Looper.loop()
                                } else  if (status == ResponseCode.PASSWORD_NOT_Right.num) {
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
//                                    //存储token
//                                    val  token=jsobj.token
//                                    SharedPreferenceUtil.putString("token",token!!)
                                    Looper.prepare()
                                    if (rememberPass.isChecked) {
                                        SharedPreferenceUtil.putBoolean("remember_pass", true)
                                        SharedPreferenceUtil.putString(
                                            "userName",
                                            username.text.toString()
                                        )
                                        SharedPreferenceUtil.putStringEncrypted(
                                            "passWord",
                                            password.text.toString()
                                        )
                                    }
                                    //判断是否是初次登录，
                                    val user = SharedPreferenceUtil.readObject(
                                        "user",
                                        UserVo::class.java
                                    )
                                    if (user.userId == -1) {
                                        SharedPreferenceUtil.putBoolean("isLoginFirst", true)

                                    } else {
                                        Log.d("LoginActivity", "${user.userName}非初次登录")
                                        SharedPreferenceUtil.putBoolean("isLoginFirst", false)
                                    }
                                    SharedPreferenceUtil.putBoolean("isLogin", true)
                                    SharedPreferenceUtil.putString("user", Gson().toJson(jsobj.data))
                                    val intent = Intent(MyApplication.context, MainActivity::class.java)
                                    startActivity(intent)
                                    Looper.loop()
                                } else {
                                    //离线操作
                                    val user =
                                        SharedPreferenceUtil.readObject("user", UserVo::class.java)
                                    if (user.userId != -1) {
                                        Log.d("LoginActivity", "登录失败,用户${user.userName}将进行离线操作")
                                    } else {
                                        Log.d("LoginActivity", "登录失败,您之后的操作将不会被保存")
                                    }
                                    val intent = Intent(MyApplication.context, MainActivity::class.java)
                                    startActivity(intent)

                                }
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
        to_operete_offline.setOnClickListener {
            val user=SharedPreferenceUtil.readObject("user",UserVo::class.java)
            if (user.userId!=-1){
                Toast.makeText(MyApplication.context,"${user.userName}将进行离线操作",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(MyApplication.context,"访客模式，将进行离线操作，所有操作将不会被同步到服务端",Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
