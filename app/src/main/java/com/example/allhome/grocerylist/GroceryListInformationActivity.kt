package com.example.allhome.grocerylist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.databinding.ActivityGroceryListInformationBinding
import com.example.allhome.grocerylist.viewmodel.GroceryListInformationActivityViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GroceryListInformationActivity : AppCompatActivity() {
    private lateinit var mGroceryListInformationActivityViewModel: GroceryListInformationActivityViewModel
    private lateinit var mDataBindingUtil:ActivityGroceryListInformationBinding

    var groceryListUniqueId: String = ""
    companion object {

        val GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG = "GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG";
        val UPDATED = 1;

    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_list_information)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent.getStringExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)?.let {
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





        mDataBindingUtil.calendarImageView.setOnClickListener({

        })

        mDataBindingUtil.scheduleTextInputEditText.setOnClickListener({
            showCalendar()
        })
        mDataBindingUtil.locationImageView.setOnClickListener({
            Toast.makeText(this, "Location", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.grocery_list_information_activity_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
            }
            R.id.update_menu->{
                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.name = mDataBindingUtil.nameTextInputEditText.text.toString().trim()
                mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity.location = mDataBindingUtil.locationTextInputEditText.text.toString().trim()

                mGroceryListInformationActivityViewModel.coroutineScope.launch {
                    mGroceryListInformationActivityViewModel.updateGroceryList(this@GroceryListInformationActivity,mGroceryListInformationActivityViewModel.mGroceryListWithItemCount.groceryListEntity)

                    withContext(Main){
                        val intent = Intent()
                        intent.putExtra(GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG,groceryListUniqueId)
                        intent.putExtra(GroceryListFragment.ACTION_TAG, GroceryListFragment.UPDATED_ACTION)
                        setResult(RESULT_OK,intent)
                        this@GroceryListInformationActivity.finish()
                    }
                }
            }
            R.id.view_items_menu->{
                val intent = Intent(this, SingleGroceryListActivity::class.java)
                intent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueId)
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
    fun showTimePicker(date:String){
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            val scheduleDateWithTimeForSaving = date +" "+ SimpleDateFormat("HH:mm:00").format(calendar.time)

            mDataBindingUtil.groceryListInformationActivityViewModel!!.mGroceryListWithItemCount!!.groceryListEntity.shoppingDatetime = scheduleDateWithTimeForSaving
            mDataBindingUtil.invalidateAll()

        }
        val timePickerDialog = TimePickerDialog(this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",DialogInterface.OnClickListener{dialog,which->
            mDataBindingUtil.groceryListInformationActivityViewModel!!.mGroceryListWithItemCount!!.groceryListEntity.shoppingDatetime = date+" 00:00:00"
            mDataBindingUtil.invalidateAll()
        })
        timePickerDialog.show()

    }

}