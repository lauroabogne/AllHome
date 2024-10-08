package com.example.allhome.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.databinding.ExpensesItemSummaryActivityBinding
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class ExpensesItemSummaryActivity : AppCompatActivity() {

    lateinit var mExpensesItemSummaryActivityBinding: ExpensesItemSummaryActivityBinding
    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()
    var mCurrentActiveFragment: Fragment? = null

    companion object {
        const val DATE_FROM_TAG = "DATE_FROM_TAG"
        const val DATE_TO_TAG = "DATE_TO_TAG"
        const val FROM_DATE_REQUEST = 0
        const val TO_DATE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)

        super.onCreate(savedInstanceState)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        title = "Expenses items breakdown"
       // setContentView(R.layout.expenses_item_summary_activity)
        mExpensesItemSummaryActivityBinding = DataBindingUtil.setContentView<ExpensesItemSummaryActivityBinding>(this, R.layout.expenses_item_summary_activity)

        mExpensesItemSummaryActivityBinding.toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        setSupportActionBar(mExpensesItemSummaryActivityBinding.toolbar)

        mExpensesItemSummaryActivityBinding.viewingTabLayout.addOnTabSelectedListener(onTabSelectedListener)

        mDateFromFilter = intent.getSerializableExtra(DATE_FROM_TAG) as Calendar
        mDateToFilter = intent.getSerializableExtra(DATE_TO_TAG) as Calendar


        mExpensesItemSummaryActivityBinding.fromTextInputEditText.setOnClickListener { showCalendar(FROM_DATE_REQUEST) }
        mExpensesItemSummaryActivityBinding.toDateTextInputEditText.setOnClickListener { showCalendar(TO_DATE_REQUEST) }
        mExpensesItemSummaryActivityBinding.filterButton.setOnClickListener {
            displayDate()
            mCurrentActiveFragment?.let { activeFragment ->
                when (activeFragment) {
                    is ExpensesSummaryViewByItemsFragment -> {
                        mCurrentActiveFragment = ExpensesSummaryViewByItemsFragment.newInstance(mDateFromFilter, mDateToFilter)
                        fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryViewByItemsFragment)
                    }
                    is ExpensesSummaryByGroceryItemsFragment -> {

                        mCurrentActiveFragment = ExpensesSummaryByGroceryItemsFragment.newInstance(mDateFromFilter, mDateToFilter)
                        fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryByGroceryItemsFragment)

                    }
                    else -> {
                        mCurrentActiveFragment = ExpensesSummaryViewByBillsFragment.newInstance(mDateFromFilter, mDateToFilter)
                        fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryViewByBillsFragment)

                    }
                }
            }

        }

        displayDate()

        mCurrentActiveFragment = ExpensesSummaryViewByItemsFragment.newInstance(mDateFromFilter, mDateToFilter)
        fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryViewByItemsFragment)


    }


    private fun displayDate() {
        val readableFromDate = SimpleDateFormat("MMM d,yyyy").format(mDateFromFilter.time)
        val readableToDate = SimpleDateFormat("MMM d,yyyy").format(mDateToFilter.time)
        mExpensesItemSummaryActivityBinding.fromTextInputEditText.setText(readableFromDate)
        mExpensesItemSummaryActivityBinding.toDateTextInputEditText.setText(readableToDate)


    }
    fun fragmentProcessor(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            commit()
        }
    }
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab!!.position == 0) {
                mCurrentActiveFragment = ExpensesSummaryViewByItemsFragment.newInstance(mDateFromFilter, mDateToFilter)
                fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryViewByItemsFragment)

            }else if(tab!!.position == 1){
                mCurrentActiveFragment = ExpensesSummaryByGroceryItemsFragment.newInstance(mDateFromFilter, mDateToFilter)
                fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryByGroceryItemsFragment)

            } else if (tab!!.position == 2) {
                mCurrentActiveFragment = ExpensesSummaryViewByBillsFragment.newInstance(mDateFromFilter, mDateToFilter)
                fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryViewByBillsFragment)

            } else {
                mCurrentActiveFragment = ExpensesSummaryViewByOtherExpensesFragment.newInstance(mDateFromFilter,mDateToFilter)
                fragmentProcessor(mCurrentActiveFragment as ExpensesSummaryViewByOtherExpensesFragment)

            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }
    fun showCalendar(dateTypeRequest: Int) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)

            if (dateTypeRequest == FROM_DATE_REQUEST) {


                mDateFromFilter.time = date
                displayDate()


            } else if (dateTypeRequest == BillCustomDateRangeDialogFragment.END_DATE_REQUEST) {


                mDateToFilter.time = date
                displayDate()


            }
        }

        val datePickerDialog = DatePickerDialog(this, dateSetListener, year, month, day)
        datePickerDialog.show()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}