package com.example.allhome.grocerylist

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.NotificationReceiver
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.databinding.ActivityGroceryListInformationBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListInformationActivityViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*


class GroceryListInformationActivity : AppCompatActivity() {
    private lateinit var mGroceryListInformationActivityViewModel: GroceryListInformationActivityViewModel
    private lateinit var mDataBindingUtil:ActivityGroceryListInformationBinding

    var groceryListUniqueId: String = ""
    companion object {

        val GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG = "GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG"
        val UPDATED = 1

    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_list_information)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
            groceryListUniqueId = it
        }
        mGroceryListInformationActivityViewModel = ViewModelProvider(this).get(GroceryListInformationActivityViewModel::class.java)


        mDataBindingUtil = DataBindingUtil.setContentView<ActivityGroceryListInformationBinding>(this, R.layout.activity_grocery_list_information).apply {
            this.lifecycleOwner = this@GroceryListInformationActivity
            mGroceryListInformationActivityViewModel.coroutineScope.launch {
                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount =  mGroceryListInformationActivityViewModel.getGroceryListItem(this@GroceryListInformationActivity, groceryListUniqueId)
                withContext(Main){
                    mDataBindingUtil.groceryListInformationActivityViewModel = mGroceryListInformationActivityViewModel
                }
            }
        }


        mDataBindingUtil.toolbar.inflateMenu((R.menu.grocery_list_information_activity_menu))
        mDataBindingUtil.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        mDataBindingUtil.toolbar.title = "Update Grocery List"
        mDataBindingUtil.toolbar.setNavigationOnClickListener {
            finish()
        }

        mDataBindingUtil.toolbar.setOnMenuItemClickListener {item->

            when(item.itemId){
                android.R.id.home -> {
                    finish()
                }
                R.id.update_menu -> {



                    val notifyType = mDataBindingUtil.notificationSpinner.selectedItem.toString()
                    val notifyText = mDataBindingUtil.notificationTextInputEditText.text.toString()
                    val notifyInt =  if(notifyText.trim().length <=0 ) 0 else notifyText.toInt()

                    mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.name = mDataBindingUtil.nameTextInputEditText.text.toString().trim()
                    mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.location = mDataBindingUtil.locationTextInputEditText.text.toString().trim()
                    mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.notify = notifyInt
                    mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.notifyType = notifyType
                    mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.uploaded = GroceryListEntity.NOT_YET_UPLOADED

                    mGroceryListInformationActivityViewModel.coroutineScope.launch {
                        mGroceryListInformationActivityViewModel.updateGroceryList(this@GroceryListInformationActivity, mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity)

                        withContext(Main) {

                            createAlarm(groceryListUniqueId)
                            val intent = Intent()
                            intent.putExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
                            intent.putExtra(GroceryListFragment.ACTION_TAG, GroceryListFragment.UPDATED_ACTION)
                            setResult(RESULT_OK, intent)
                            this@GroceryListInformationActivity.finish()
                        }
                    }
                }
                R.id.go_shopping_menu -> {
                    val intent = Intent(this, SingleGroceryListActivity::class.java)
                    intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
                    this.startActivity(intent)
                    this.finish()
                }
            }
            true
        }

        mDataBindingUtil.calendarImageView.setOnClickListener {
            showCalendar()
        }

        mDataBindingUtil.scheduleTextInputEditText.setOnClickListener {
            showCalendar()
        }
        mDataBindingUtil.locationImageView.setOnClickListener {

            val location = mDataBindingUtil.locationTextInputEditText.text.toString()

            val gmmIntentUri = Uri.parse("geo:0,0?q=$location")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.grocery_list_information_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
            R.id.update_menu -> {



                val notifyType = mDataBindingUtil.notificationSpinner.selectedItem.toString()
                val notifyText = mDataBindingUtil.notificationTextInputEditText.text.toString()
                val notifyInt =  if(notifyText.trim().length <=0 ) 0 else notifyText.toInt()

                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.name = mDataBindingUtil.nameTextInputEditText.text.toString().trim()
                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.location = mDataBindingUtil.locationTextInputEditText.text.toString().trim()
                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.notify = notifyInt
                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.notifyType = notifyType

                mGroceryListInformationActivityViewModel.coroutineScope.launch {
                    mGroceryListInformationActivityViewModel.updateGroceryList(this@GroceryListInformationActivity, mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity)

                    withContext(Main) {

                        createAlarm(groceryListUniqueId)
                        val intent = Intent()
                        intent.putExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
                        intent.putExtra(GroceryListFragment.ACTION_TAG, GroceryListFragment.UPDATED_ACTION)
                        setResult(RESULT_OK, intent)
                        this@GroceryListInformationActivity.finish()
                    }
                }
            }
            R.id.go_shopping_menu -> {
                val intent = Intent(this, SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
                this.startActivity(intent)
                this.finish()
            }
        }

        return true
    }
    fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val stringDateForSaving = SimpleDateFormat("yyyy-MM-dd").format(date)

            showTimePicker(stringDateForSaving)
        }

        val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    fun showTimePicker(date: String){
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            val scheduleDateWithTimeForSaving = date +" "+ SimpleDateFormat("HH:mm:00").format(calendar.time)

            mDataBindingUtil.groceryListInformationActivityViewModel!!.mGroceryListWithItemCount.groceryListEntity.shoppingDatetime = scheduleDateWithTimeForSaving
            mDataBindingUtil.invalidateAll()

        }
        val timePickerDialog = TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No time", DialogInterface.OnClickListener { dialog, which ->
            mDataBindingUtil.groceryListInformationActivityViewModel!!.mGroceryListWithItemCount.groceryListEntity.shoppingDatetime = date + " 00:00:00"
            mDataBindingUtil.invalidateAll()
        })
        timePickerDialog.show()

    }
    private fun createAlarm(groceryListUniqueId: String){


        if(mDataBindingUtil.notificationTextInputEditText.text.toString().trim().isEmpty()){
            return
        }

        val notificationStringData = mDataBindingUtil.notificationTextInputEditText.text.toString().toInt()
        val notificationTypeSelected =mDataBindingUtil.notificationSpinner.selectedItem.toString()

        val notificationDatetime = generatedAlarmDatetime(notificationStringData, notificationTypeSelected)


        // Get AlarmManager instance
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = createIntent(groceryListUniqueId)
        val pendingIntent = createPendingIntent(intent)

        //cancel if there is alarm set previously
        alarmManager.cancel(pendingIntent)

        if(notificationDatetime <=0){

            /**
             * do nothing
             */
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {


            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }else{

            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }
    }

    private fun generatedAlarmDatetime(notifyValue: Int, notifyType: String):Long{
        var shoppingDateTimeString:String = mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.shoppingDatetime
        if(shoppingDateTimeString.isEmpty() || shoppingDateTimeString.equals("0000-00-00 00:00:00")){

            return 0
        }
        if(shoppingDateTimeString.split(" ").size <=1){
            shoppingDateTimeString = "$shoppingDateTimeString 00:00:00"
        }
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val datetime:DateTime = formatter.parseDateTime(shoppingDateTimeString)

        when(notifyType){
            resources.getString(R.string.grocery_notification_none) -> {
                return 0
            }
            resources.getString(R.string.grocery_notification_same_day_and_time) -> {

                return datetime.millis
            }
            resources.getString(R.string.grocery_notification_minute_before) -> {

                return datetime.minusMinutes(notifyValue).millis
            }
            resources.getString(R.string.grocery_notification_hour_before) -> {

                return datetime.minusHours(notifyValue).millis
            }
            resources.getString(R.string.grocery_notification_day_before) -> {

                return datetime.minusDays(notifyValue).millis
            }
        }
        return 0
    }
    private fun createIntent(groceryListUniqueId: String):Intent{
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.action = NotificationReceiver.GROCERY_NOTIFICATION_ACTION
        intent.putExtra(NotificationReceiver.GROCERY_LIST_UNIQUE_ID, groceryListUniqueId)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        return intent
    }

    private fun createPendingIntent(intent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(this, NotificationReceiver.NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}