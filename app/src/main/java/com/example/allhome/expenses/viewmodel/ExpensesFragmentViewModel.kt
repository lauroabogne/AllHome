package com.example.allhome.expenses.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.ExpensesEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.collections.ArrayList

class ExpensesFragmentViewModel : ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("ExpensesFragmentViewModel"))

    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()
    var mFilterTotalExpenses: Double = 0.0
    var mCurrentYearTotalExpenses: Double = 0.0
    var mExpensesPerMonth = arrayListOf<ExpensesEntity>()


    suspend fun getExpenses(context: Context, fromDate: String, toDate: String): Double {

        mFilterTotalExpenses = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpenses(fromDate, toDate).totalAmount
        return mFilterTotalExpenses
    }

    suspend fun getExpensesByMonth(context: Context, month: String): ExpensesEntity {

        return AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpensesInMonth(month)
    }

    suspend fun getCurrentYearExpenses(context: Context, fromDate: String, toDate: String): Double {

        mCurrentYearTotalExpenses = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpenses(fromDate, toDate).totalAmount
        return mCurrentYearTotalExpenses
    }

    suspend fun getExpensesPerMonth(context: Context, fromDate: String, toDate: String): List<ExpensesEntity> {

        mExpensesPerMonth = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpensesPerMonth(fromDate, toDate) as ArrayList<ExpensesEntity>
        return mExpensesPerMonth
    }


}