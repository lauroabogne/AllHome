package com.example.allhome

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.allhome.network.RetrofitInstance
import com.example.allhome.network.Sync
import com.example.allhome.network.uploads.BillsUpload
import com.example.allhome.recipes.RecipesFragment
import com.example.allhome.storage.StorageFragment
import com.example.allhome.todo.CreateEditTodoFragment
import com.example.allhome.todo.TodoFragment
import com.example.allhome.todo.TodoFragmentContainerActivity
import com.example.allhome.todo.ViewTodoFragment
import com.example.allhome.todo.calendar.TodoCalendarViewFragment
import com.example.allhome.utils.CustomAlarmManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var drawerLayout: DrawerLayout
    var selectedDrawerItem = R.id.nav_todo

    companion object {
        const val REQUEST_POST_PERMISSIONS_CODE = 1161986
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //setTheme(R.style.ThemeBlue)
        //setTheme(R.style.ThemeYellow)

        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)


        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = this.findViewById(R.id.toolbar)

        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        //show menu icon on action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerLayout = findViewById(R.id.drawer_layout)
        // for drawer layout
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
                        fragmentProcessor(com.example.allhome.meal_planner_v2.MealPlannerFragment.newInstance("", ""))
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_bills -> {
                        fragmentProcessor(BillsFragment.newInstance("", ""))

                    }
                    R.id.nav_expenses_summary -> {
                        fragmentProcessor(ExpensesFragment.newInstance("", ""))
                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_todo -> {
//                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                        val stringDate = dateFormat.format(Date().time)
//                        fragmentProcessor(TodoFragment.newInstance(TodoFragment.MAIN_ACTIVITY, stringDate))

                        fragmentProcessor(TodoCalendarViewFragment())

                        //drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    R.id.nav_setting -> {

                        Toast.makeText(this@MainActivity,"Setting",Toast.LENGTH_SHORT).show()

                    }
                    R.id.nav_sync -> {
                        Toast.makeText(this@MainActivity, "Sync", Toast.LENGTH_SHORT).show()
                        testUpload()
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

                    fragmentProcessor(TodoFragment.newInstance(TodoFragment.MAIN_ACTIVITY, ""))

                    intent.getStringExtra(CreateEditTodoFragment.TODO_UNIQUE_ID_TAG)?.let {todoUniqueId->
                        Toast.makeText(this,"Todo must open $todoUniqueId",Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, TodoFragmentContainerActivity::class.java)
                        intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG, TodoFragmentContainerActivity.VIEW_TODO_FRAGMENT)
                        intent.putExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
                        startActivity(intent)
                    }
                }
                else -> {
                    fragmentProcessor(TodoCalendarViewFragment())
                }
            }
        }
        requestPostNotificationsPermission()

        if(!isAlarmActive()){
            createAlarm()
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
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onNewIntent(intent: Intent) {
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
        val alarmDateTimeMilli = DateTime.now().plusSeconds(1).millis
        val intent = Intent(applicationContext, NotificationReceiver::class.java)
        intent.apply {
            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }
        CustomAlarmManager.createAlarm(this,NotificationReceiver.NOTIFICATION_REQUEST_CODE,intent,alarmDateTimeMilli)

    }


    private val billsUpload: BillsUpload by lazy {
        // Initialize ExpensesUpload with your ApiService and ExpensesDAO
        val billDAO = (applicationContext as AllHomeBaseApplication).billDAO
        BillsUpload(RetrofitInstance.api, billDAO)
    }


    private fun testUpload_(){

        val syncNotification = SyncNotificationProgress(this)
        CoroutineScope(Dispatchers.IO).launch {
            val totalItemsToSync = 30 //Set this based on the number of items you need to sync
            for (i in 1..totalItemsToSync) {
                // Upload data...
                val perItemTotalItemToSync = 10;
                for(x in 1 ..perItemTotalItemToSync){
                    // Update notification progress
                    delay(1000)
                    //syncNotification.showProgressNotification(i, totalItemsToSync, x, perItemTotalItemToSync)
                }

            }

            // Once sync is complete
            syncNotification.completeSync()
        }


    }

    private fun testUpload(){
        Sync.getInstance(applicationContext).startSync()


//        lifecycleScope.launch {
//            Log.e("Test", "Test network call")
//            billsUpload.uploadBills()
//        }

    }
}