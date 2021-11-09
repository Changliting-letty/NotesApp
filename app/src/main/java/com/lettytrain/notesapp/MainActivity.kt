package com.lettytrain.notesapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.lettytrain.notesapp.util.SharedPreferencesUtil
import com.lettytrain.notesapp.vo.UserVo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.nav_header.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userId = intent.getIntExtra("userId", -1)
        var fragment: Fragment
        var bundle = Bundle()  //用于传递数据
        bundle.putInt("userId", userId)
        fragment = NotesHomeFragment.newInstance()
        fragment.arguments = bundle
        replaceFragment(fragment)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        val headerview = navView.getHeaderView(0)
        val isLogin = SharedPreferencesUtil.readBoolean("isLogin")
        if (isLogin) {
            //获取用户信息
            val userVo = SharedPreferencesUtil.readObject("user", UserVo::class.java) as UserVo
            val nvHeaderName = headerview.findViewById<View>(R.id.nvHeaderName) as TextView
            nvHeaderName.text = userVo.userName
            val btn = headerview.findViewById<View>(R.id.gotoLogin) as Button
            btn.visibility = View.GONE
        }else{
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
                    SharedPreferencesUtil.putBoolean("isLogin",false)
                    val intent = Intent(MyApplication.context, LoginActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    drawerLayout.closeDrawers()
                }
            }
            true
        }


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
