package com.lettytrain.notesapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.lettytrain.notesapp.database.UserDatabase
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
                        val editor=prefs.edit()
                        if (rememberPass.isChecked){
                            editor.putBoolean("remember_pass",true)
                            editor.putString("userName",username.text.toString())
                            editor.putString("passWord",password.text.toString())
                        }else{
                            editor.clear()
                        }
                        editor.apply()
                        val intent = Intent(MyApplication.context,MainActivity::class.java)
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
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
