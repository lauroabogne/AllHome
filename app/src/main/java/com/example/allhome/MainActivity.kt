package com.example.allhome

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.allhome.bill.BillsFragment
import com.example.allhome.expenses.ExpensesFragment
import com.example.allhome.grocerylist.AddGroceryListItemFragment
import com.example.allhome.grocerylist.GroceryListFragment
import com.example.allhome.grocerylist.SingleGroceryListActivity
import com.example.allhome.grocerylist.trash_grocery_list.TrashGroceryListFragment
import com.example.allhome.meal_planner.MealPlannerFragment
import com.example.allhome.recipes.RecipesFragment
import com.example.allhome.storage.StorageFragment
import com.example.allhome.todo.TodoFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var drawerLayout: DrawerLayout
    var selectedDrawerItem = R.id.nav_grocery_list


    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.ThemeBlue)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        //show menu icon on action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout = findViewById(R.id.drawer_layout)
        // for drawerlayout
        val drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                when (selectedDrawerItem) {
                    R.id.nav_grocery_list -> {
                        fragmentProcessor(GroceryListFragment())
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_storage -> {

                        val bundle = Bundle()
                        bundle.putInt(StorageFragment.ACTION_TAG, StorageFragment.STORAGE_VIEWING_ACTION)

                        val storageFragment = StorageFragment()
                        storageFragment.arguments = bundle

                        fragmentProcessor(storageFragment)
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }

                    R.id.nav_recipes -> {
                        fragmentProcessor(RecipesFragment())
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }

                    R.id.nav_meal_planner -> {


                        //fragmentProcessor(MealPlannerFragment.newInstance("", ""))
                        fragmentProcessor(com.example.allhome.meal_planner_v2.MealPlannerFragment.newInstance("", ""))
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_bills -> {
                        fragmentProcessor(BillsFragment.newInstance("", ""))
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_expenses_summary -> {
                        fragmentProcessor(ExpensesFragment.newInstance("", ""))
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_todo -> {

                        fragmentProcessor(TodoFragment.newInstance("", ""))
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }

            }
        }

        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            selectedDrawerItem = it.itemId
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        fragmentProcessor(GroceryListFragment())


        intent?.action?.let{
            intent?.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
                val intent = Intent(this, SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, it)
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
                fragmentProcessor(GroceryListFragment())
            }
            R.id.grocery_list_trash_menu->{

                fragmentProcessor(TrashGroceryListFragment())


            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.action?.let{
            intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
                val intent = Intent(this, SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, it)
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