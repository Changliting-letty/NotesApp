package com.lettytrain.notesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import com.google.gson.Gson
import com.lettytrain.notesapp.entities.ResponseCode
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import com.lettytrain.notesapp.vo.ServerResponse
import kotlinx.android.synthetic.main.activity_signup.*


//注册
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        newUserRegister()
    }
    private fun newUserRegister() {
        signup.setOnClickListener {
            //判空
            if (username.text.isNullOrEmpty()) {
                Toast.makeText(this, "UserName is Requeried", Toast.LENGTH_SHORT).show()
            }
            if (password.text.isNullOrEmpty()) {
                Toast.makeText(this, "Password is  Requeried", Toast.LENGTH_SHORT).show()
            }
            if (confirm.text.isNullOrEmpty()) {
                Toast.makeText(this, "Please input your password again.", Toast.LENGTH_SHORT).show()
            }
            //两次密码不一致
            if (!confirm.text.toString().equals(password.text.toString())) {
                Toast.makeText(this, "The two password are inconsistent", Toast.LENGTH_SHORT).show()
                password.setText("")
                confirm.setText("")
            } else {
                //发送网络请求，用onfinish拿到结果
                OKHttpUtils.get(
                    "http://10.236.11.105:8080/portal/user/signup.do?userName=${username.text.toString()}&password=${password.text.toString()}",
                    object : OKHttpCallback() {
                        override fun onFinish(status1: String, result: String) {
                            super.onFinish(status1, result)
                            //解析数据

                            var jsobj = Gson().fromJson(result, ServerResponse::class.java)
                            val status = jsobj.status
                            val msg = jsobj.msg
                            if (status == ResponseCode.USERNAME_EXITS.num) {
                                //如果用户名已经存在
                                Looper.prepare()
                                Toast.makeText(
                                    MyApplication.context,
                                    "The username already exists. ",
                                    Toast.LENGTH_SHORT
                                ).show()
                                username.setText("")
                                password.setText("")
                                confirm.setText("")
                                Looper.loop()

                            } else if (status ==ResponseCode.IS_SIGNUP_SUCCESS.num) {
                                Looper.prepare()
                                username.setText("")
                                password.setText("")
                                confirm.setText("")
                                Toast.makeText(
                                    MyApplication.context,
                                    "To Login......",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                                val intent =
                                    Intent(MyApplication.context, LoginActivity::class.java)
                                startActivity(intent)
                                Looper.loop()
                                // supportFragmentManager.popBackStack()
                                //设置有个交互按钮，让用户选择是去登录还是继续留在这里
                            } else {
                                //链接失败
                                Looper.prepare()
                                Toast.makeText(
                                    MyApplication.context,
                                    "The connection fails,please try again. ",
                                    Toast.LENGTH_SHORT
                                ).show()
                                username.setText("")
                                password.setText("")
                                confirm.setText("")
                                Looper.loop()
                            }
                        }
                    }
                )
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

