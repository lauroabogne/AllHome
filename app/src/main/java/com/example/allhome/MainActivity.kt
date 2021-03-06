package com.example.allhome

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.example.allhome.grocerylist.GroceryListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var drawerLayout: DrawerLayout;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //show menu icon on action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        drawerLayout = findViewById(R.id.drawer_layout)
        // for drawerlayout
        val drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            override fun onDrawerClosed(drawerView: View) { super.onDrawerClosed(drawerView) }
        }
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_grocery_list->{
                    //val intent = Intent (applicationContext,GroceryListActivity::class.java)
                    //startActivity(intent)
                    fragmentProcessor()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }


            }
            true
        }

    }

    fun fragmentProcessor(){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_fragment_container,GroceryListFragment())
            commit()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        drawerLayout.openDrawer(GravityCompat.START)
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

}