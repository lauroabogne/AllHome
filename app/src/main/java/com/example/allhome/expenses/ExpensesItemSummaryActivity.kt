package com.example.allhome.expenses

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.databinding.ActivityAddGroceryListItemBinding
import com.example.allhome.databinding.ExpensesItemSummaryActivityBinding
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class ExpensesItemSummaryActivity : AppCompatActivity() {

    lateinit var mExpensesItemSummaryActivityBinding:ExpensesItemSummaryActivityBinding
    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()

    companion object{
        const val DATE_FROM_TAG = "DATE_FROM_TAG"
        const val DATE_TO_TAG = "DATE_TO_TAG"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Expenses items breakdown"
        setContentView(R.layout.expenses_item_summary_activity)
        mExpensesItemSummaryActivityBinding = DataBindingUtil.setContentView<ExpensesItemSummaryActivityBinding>(this,R.layout.expenses_item_summary_activity)
        mExpensesItemSummaryActivityBinding.viewingTabLayout.addOnTabSelectedListener(onTabSelectedListener)

        mDateFromFilter = intent.getSerializableExtra(DATE_FROM_TAG) as Calendar
        mDateToFilter = intent.getSerializableExtra(DATE_TO_TAG) as Calendar
        displayDate()
        fragmentProcessor(ExpensesSummaryViewByItemsFragment.newInstance(mDateFromFilter,mDateToFilter))

    }
    fun displayDate(){
        val readableFromDate = SimpleDateFormat("MMMM d,yyyy").format(mDateFromFilter.time)
        val readableToDate = SimpleDateFormat("MMMM d,yyyy").format(mDateToFilter.time)
        mExpensesItemSummaryActivityBinding.fromTextInputEditText.setText(readableFromDate)
        mExpensesItemSummaryActivityBinding.toDateTextInputEditText.setText(readableToDate)


    }
    fun fragmentProcessor(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer,fragment)
            commit()
        }

    }

    val onTabSelectedListener = object: TabLayout.OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab?) {

            if(tab!!.position == 0){
                fragmentProcessor(ExpensesSummaryViewByItemsFragment.newInstance(mDateFromFilter,mDateToFilter))
            }else{
                fragmentProcessor(ExpensesSummaryViewByCategoryFragment.newInstance("",""))

            }
            Toast.makeText(this@ExpensesItemSummaryActivity,"Clicked",Toast.LENGTH_SHORT).show()

            //mFragmentViewRecipeBinding.viewPager.currentItem = tab!!.position
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

}