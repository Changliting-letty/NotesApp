package com.lettytrain.notesapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.work.*
import com.lettytrain.notesapp.util.SharedPreferenceUtil
import com.lettytrain.notesapp.util.SynToRemoteWorker
import com.lettytrain.notesapp.util.getNowDateTime
import com.lettytrain.notesapp.vo.UserVo
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val user = SharedPreferenceUtil.readObject("user",UserVo::class.java) as UserVo
        val userId = user.userId
        var fragment: Fragment
        var bundle = Bundle()  //用于传递数据
        bundle.putInt("userId", userId!!)
        fragment = NotesHomeFragment.newInstance()
        fragment.arguments = bundle
        replaceFragment(fragment)
        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener {
            item ->
            when(item.itemId){
                R.id.backup ->
                {
                    //触发后台同步操作,立即运行
                    Log.d("MainActivity", "用户点击backup,时间：${Date().getNowDateTime()}")
                    val request = OneTimeWorkRequest.Builder(SynToRemoteWorker::class.java).build()
                    WorkManager.getInstance(this).enqueue(request)
                   // 监听运行结果
                    WorkManager.getInstance(this).getWorkInfoByIdLiveData(request.id)
                        .observe(this , Observer{ t ->
                            if (t.state==WorkInfo.State.SUCCEEDED){
                                Log.d("MainActivity", "成功同步至服务端一次,时间：${Date().getNowDateTime()}")
                            } else if (t.state == WorkInfo.State.FAILED) {
                                Log.d("MainActivity", "同步至服务端失败一次,时间：${Date().getNowDateTime()}")
                            }
                        })
                }
            }
            true
        }
        supportActionBar?.let {

            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        val headerview = navView.getHeaderView(0)
        val isLogin = SharedPreferenceUtil.readBoolean("isLogin")
        if (isLogin) {
            //获取用户信息
            val nvHeaderName = headerview.findViewById<View>(R.id.nvHeaderName) as TextView
            nvHeaderName.text = user.userName
            val btn = headerview.findViewById<View>(R.id.gotoLogin) as Button
            btn.visibility = View.GONE
        }else{
            val logOffItem=navView.menu.findItem(R.id.navLogOff)
            val unlogItem=navView.menu.findItem(R.id.navUnlogin)
            logOffItem.isVisible=false
            unlogItem.isVisible=false
            val btn = headerview.findViewById<View>(R.id.gotoLogin) as Button
            btn.setOnClickListener {
                val intent = Intent(MyApplication.context, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        navView.setCheckedItem(R.id.navHome)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navNote ->
                    replaceFragment(NotesHomeFragment())
                R.id.navUnlogin ->
                {
                    SharedPreferenceUtil.putBoolean("isLogin",false)   //退出登录
                    SharedPreferenceUtil.putBoolean("isLoginFirst",false)
                    val intent = Intent(MyApplication.context, LoginActivity::class.java)
                    startActivity(intent)
                }
                R.id.navLogOff ->{
                    SharedPreferenceUtil.clear()  //注销登录
                    val intent = Intent(MyApplication.context, LoginActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    drawerLayout.closeDrawers()
                }
            }
            true
        }
        //执行同步操作
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
            R.id.backup -> Toast.makeText(this, "You clicked Backup", Toast.LENGTH_SHORT).show()
            R.id.delete -> Toast.makeText(this, "You clicked Delete", Toast.LENGTH_SHORT).show()
            R.id.settings -> Toast.makeText(this, "You clicked Settings", Toast.LENGTH_SHORT).show()
        }
        return true
    }
    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.placeNotesFragment, fragment)
        fragmentTransaction.commit()
    }

}
