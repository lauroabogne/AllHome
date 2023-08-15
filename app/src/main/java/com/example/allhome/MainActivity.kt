package com.example.allhome

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.allhome.bill.BillsFragment
import com.example.allhome.expenses.ExpensesFragment
import com.example.allhome.grocerylist.AddGroceryListItemFragment
import com.example.allhome.grocerylist.GroceryListFragment
import com.example.allhome.grocerylist.SingleGroceryListActivity
import com.example.allhome.grocerylist.trash_grocery_list.TrashGroceryListFragment
import com.example.allhome.recipes.RecipesFragment
import com.example.allhome.storage.StorageFragment
import com.example.allhome.todo.CreateEditTodoFragment
import com.example.allhome.todo.TodoFragment
import com.example.allhome.todo.TodoFragmentContainerActivity
import com.example.allhome.todo.ViewTodoFragment
import com.example.allhome.utils.CustomAlarmManager
import org.joda.time.DateTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var drawerLayout: DrawerLayout
    var selectedDrawerItem = R.id.nav_grocery_list

    companion object {
        const val REQUEST_POST_PERMISSIONS_CODE = 1161986
    }

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
//                        fragmentProcessor(BillsFragment.newInstance("", ""))
//                        drawerLayout.closeDrawer(GravityCompat.START)
                        if(!isAlarmActive()){
                            createAlarm()
                            Toast.makeText(this@MainActivity,"Alarm is not active", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this@MainActivity,"Alarm is already active", Toast.LENGTH_SHORT).show()
                        }

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

        /**
         * @see NotificationReceiver
         */
        intent?.action?.let{ action->
            when (action) {
                NotificationReceiver.GROCERY_NOTIFICATION_ACTION -> {

                    fragmentProcessor(GroceryListFragment())

                    intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
                        val intent = Intent(this, SingleGroceryListActivity::class.java)
                        intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, it)
                        startActivity(intent)
                    }
                }
                NotificationReceiver.TODO_NOTIFICATION_ACTION -> {

                    fragmentProcessor(TodoFragment.newInstance("", ""))

                    intent.getStringExtra(CreateEditTodoFragment.TODO_UNIQUE_ID_TAG)?.let {todoUniqueId->
                        Toast.makeText(this,"Todo must open $todoUniqueId",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, TodoFragmentContainerActivity::class.java)
                        intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG, TodoFragmentContainerActivity.VIEW_TODO_FRAGMENT)
                        intent.putExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
                        startActivity(intent)
                    }
                }
                else -> {
                    fragmentProcessor(GroceryListFragment())
                }
            }
        }?:run{
        }

        requestPostNotificationsPermission()

        if(!isAlarmActive()){
            createAlarm()
            Toast.makeText(this,"Alarm is not active", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"Alarm is already active", Toast.LENGTH_SHORT).show()
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


        /**
         * @see NotificationReceiver
         */
        intent?.action?.let{ action->
            when (action) {
                NotificationReceiver.GROCERY_NOTIFICATION_ACTION -> {
                    intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
                        val intent = Intent(this, SingleGroceryListActivity::class.java)
                        intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, it)
                        startActivity(intent)
                    }
                }
                NotificationReceiver.TODO_NOTIFICATION_ACTION -> {

                    intent.getStringExtra(CreateEditTodoFragment.TODO_UNIQUE_ID_TAG)?.let {todoUniqueId->

                        val intent = Intent(this, TodoFragmentContainerActivity::class.java)
                        intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG, TodoFragmentContainerActivity.VIEW_TODO_FRAGMENT)
                        intent.putExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
                        startActivity(intent)
                    }
                }
                else -> {

                }
            }

        }



    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_POST_PERMISSIONS_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,"Permissions granted",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Permissions not granted",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPostNotificationsPermission() {
        val permission = POST_NOTIFICATIONS

        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }else{
            Toast.makeText(this,"NOt granted here",Toast.LENGTH_SHORT).show()
        }


        // Request the permission
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_POST_PERMISSIONS_CODE)
    }

    private fun isAlarmActive(): Boolean {
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        intent.apply {
            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
        }
        return CustomAlarmManager.isAlarmActive(this,NotificationReceiver.NOTIFICATION_REQUEST_CODE,intent)
    }

    private fun createAlarm(){
        val alarmDateTimeMilli = DateTime.now().plusSeconds(10).millis
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        intent.apply {
            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        CustomAlarmManager.createAlarm(this,NotificationReceiver.NOTIFICATION_REQUEST_CODE,intent,alarmDateTimeMilli)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e("RESULT","RECEIVED")

    }



}