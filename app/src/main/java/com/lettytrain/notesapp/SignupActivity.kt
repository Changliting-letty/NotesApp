package com.lettytrain.notesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.lettytrain.notesapp.R
import com.lettytrain.notesapp.database.NotesDatabase
import com.lettytrain.notesapp.database.UserDatabase
import com.lettytrain.notesapp.entities.Notes
import com.lettytrain.notesapp.entities.User
import com.lettytrain.notesapp.util.OKHttpCallback
import com.lettytrain.notesapp.util.OKHttpUtils
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

//注册
    class SignupActivity : AppCompatActivity() ,CoroutineScope{
        private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        setContentView(R.layout.activity_signup)
        newUserRegister()
    }
    private  fun newUserRegister(){
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
            if (!confirm.text.toString().equals(password.text.toString())){
                    Toast.makeText(this,"The two password are inconsistent",Toast.LENGTH_SHORT).show()
                     password.setText("")
                        confirm.setText("")
            } else {
                launch {
                    if(UserDatabase.getDatabase(MyApplication.context).userDao().getUsers(username.text.toString())!=0){
                        //如果用户名已经存在
                        Toast.makeText(MyApplication.context,"The username already exists. ",Toast.LENGTH_SHORT).show()
                        username.setText("")
                        password.setText("")
                        confirm.setText("")

                }else{
                        //注册新用户
                        //同步到后端数据库
                        synchronousWithBackend(username.text.toString(),password.text.toString())
                        var user = User()
                        user.user_name=username.text.toString()
                        user.password=password.text.toString()
                        UserDatabase.getDatabase(MyApplication.context).userDao().insertUsers(user)
                        username.setText("")
                        password.setText("")
                        confirm.setText("")
                        Toast.makeText(MyApplication.context,"To Login......",Toast.LENGTH_SHORT).show()
                        val intent = Intent(MyApplication.context,LoginActivity::class.java)
                        startActivity(intent)
                       // supportFragmentManager.popBackStack()
                        //设置有个交互按钮，让用户选择是去登录还是继续留在这里

                    }
                }
                    }

                    }
                }
    fun synchronousWithBackend(username:String,password:String) {
        OKHttpUtils.get(
            "http://161.97.110.236:8080/portal/user/signup.do?userName=${username}&password=${password}",
            OKHttpCallback()
        )

    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
    }

