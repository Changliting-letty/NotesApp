package com.lettytrain.notesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
   // val fruits = mutableListOf(Fruit("Apple", R.drawable.apple), Fruit("Banana", R.drawable.banana), Fruit("Orange", R.drawable.orange), Fruit("Watermelon", R.drawable.watermelon), Fruit("Pear", R.drawable.pear), Fruit("Grape", R.drawable.grape), Fruit("Pineapple", R.drawable.pineapple), Fruit("Strawberry", R.drawable.strawberry), Fruit("Cherry", R.drawable.cherry), Fruit("Mango", R.drawable.mango))

  //  val fruitList = ArrayList<Fruit>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment(NotesHomeFragment())

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu)
        }
        navView.setCheckedItem(R.id.navNote)
        navView.setNavigationItemSelectedListener {item->
            when(item.itemId){
                R.id.navNote->
                    replaceFragment(NotesHomeFragment())
                else->{
                    drawerLayout.closeDrawers()
                }
            }
            true

        }

//        initFruits()
////        val layoutManager = GridLayoutManager(this, 2)
////        recyclerView.layoutManager = layoutManager
////        val adapter = FruitAdapter(this, fruitList)
////        recyclerView.adapter = adapter
////        swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
////        swipeRefresh.setOnRefreshListener {
////            refreshFruits(adapter)
////        }
    }

//   private fun refreshFruits(adapter: FruitAdapter) {
//        thread {
//            Thread.sleep(2000)
//            runOnUiThread {
//                initFruits()

//                adapter.notifyDataSetChanged()
//                swipeRefresh.isRefreshing = false
//            }
//        }
//    }

//    private fun initFruits() {
//        fruitList.clear()
//        repeat(50) {
//            val index = (0 until fruits.size).random()
//            fruitList.add(fruits[index])
//        }
//    }

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

    fun replaceFragment(fragment: Fragment){
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.drawerLayout,fragment)
        fragmentTransaction.commit()
    }
}
