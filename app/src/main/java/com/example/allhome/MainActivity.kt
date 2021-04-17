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
import com.example.allhome.grocerylist.trash_grocery_list.TrashGroceryListFragment
import com.example.allhome.pantry.PantryAddItemActivity
import com.example.allhome.pantry.PantryStorageActivity

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
                R.id.nav_pantry->{
                    /*val addPantryItemActivity = Intent(this, PantryAddItemActivity::class.java)
                    startActivity(addPantryItemActivity)*/
                    val pantryStorageActivity = Intent(this, PantryStorageActivity::class.java)
                    startActivity(pantryStorageActivity)
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
        when(item.itemId){
            android.R.id.home->{
                drawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.grocery_list_menu->{
                Toast.makeText(this,"List",Toast.LENGTH_SHORT).show()
                fragmentProcessor(GroceryListFragment())
            }
            R.id.grocery_list_trash_menu->{
                Toast.makeText(this,"Trash",Toast.LENGTH_SHORT).show()
                fragmentProcessor(TrashGroceryListFragment())


            }
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e("RESULT","RECEIVED")

    }

}