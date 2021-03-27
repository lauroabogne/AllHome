package com.example.allhome

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.allhome.grocerylist.AddGroceryListItemActivity
import com.example.allhome.grocerylist.GroceryListFragment
import com.example.allhome.grocerylist.SingleGroceryListActivity

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
                    fragmentProcessor(GroceryListFragment())
                    drawerLayout.closeDrawer(GravityCompat.START)
                }

            }
            true
        }
        fragmentProcessor(GroceryListFragment())


        intent?.action?.let{
            intent?.getStringExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
                val intent = Intent(this, SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, it)
                startActivity(intent)
            }
        }
    }

    fun fragmentProcessor(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.home_fragment_container,fragment)
            commit()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        drawerLayout.openDrawer(GravityCompat.START)
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.action?.let{
            intent?.getStringExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
                val intent = Intent(this, SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, it)
                startActivity(intent)
            }
        }


        Toast.makeText(this,"NEW INTENT",Toast.LENGTH_SHORT).show()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e("RESULT","RECEIVED")

    }

}