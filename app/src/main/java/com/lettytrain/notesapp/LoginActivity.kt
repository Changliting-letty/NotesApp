package com.lettytrain.notesapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lettytrain.notesapp.database.UserDatabase
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.password
import kotlinx.android.synthetic.main.activity_login.username
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LoginActivity : AppCompatActivity(),CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_login)
        //记住密码功能
        val prefs=getPreferences(Context.MODE_PRIVATE)
        val editor=prefs.edit()
        val isRemember=prefs.getBoolean("remember_pass",false)
        if(isRemember){
            //将用户名和密码都设置到文本框中
            val  user_name=prefs.getString("userName","")
            val pass_word=prefs.getString("passWord","")
            username.setText(user_name)
            password.setText(pass_word)
            rememberPass.isChecked=true
        }

        login.setOnClickListener {
            //判空
            if (username.text.isNullOrEmpty()) {
                Toast.makeText(this, "UserName is Requeried", Toast.LENGTH_SHORT).show()
            }
            if (password.text.isNullOrEmpty()) {
                Toast.makeText(this, "Password is  Requeried", Toast.LENGTH_SHORT).show()
            }else{
                launch {
                    //用户不存在
                    if(UserDatabase.getDatabase(MyApplication.context).userDao().getUsers(username.text.toString())==0){
                        Toast.makeText(MyApplication.context, "The UserName doesn't exist!", Toast.LENGTH_SHORT).show()
                        username.setText("")
                        password.setText("")
                    }
                    //密码错误
                    var psd=UserDatabase.getDatabase(MyApplication.context).userDao().getPassword(username.text.toString())
                    if(!password.text.toString().equals(psd)){
                        Toast.makeText(MyApplication.context, "The Password is wrong!", Toast.LENGTH_SHORT).show()
                        password.setText("")
                    }else{
                        //如果记住密码复选框被选中
                        if (rememberPass.isChecked){
                            editor.putBoolean("remember_pass",true)
                            editor.putString("userName",username.text.toString())
                            editor.putString("passWord",password.text.toString())
                        }else{
                            editor.clear()
                        }
                        editor.apply()
                        //同步到后端数据库
                        synchronousWithBackend(username.text.toString(),password.text.toString())
                        //用户SessionID
                        var userID=UserDatabase.getDatabase(MyApplication.context).userDao().getUserID(username.text.toString())
                        val prefall=MyApplication.context.getSharedPreferences("session",Context.MODE_PRIVATE)
                        val editor1=prefall.edit()
                        editor1.putInt("userId",userID)
                        editor1.apply()
                        val intent = Intent(MyApplication.context,MainActivity::class.java)
                   //     intent.putExtra("userId",userID)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
        to_sign_up.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun synchronousWithBackend(username:String,password:String) {
        OKHttpUtils.get(
            "http://10.236.35.203:8080/portal/user/login.do?username=${username}&password=${password}",
            OKHttpCallback()
        )

    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
