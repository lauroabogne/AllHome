package com.example.allhome.expenses.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlin.collections.ArrayList

class ExpensesSummaryViewByItemsFragmentViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("ExpensesSummaryViewByItemsFragmentViewModel"))

    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()

    var mFilterTotalExpenses:Double = 0.0
    var  mExpensesEntityWithItemNameAndType:List<ExpensesEntityWithItemNameAndType> = arrayListOf()

    suspend fun getExpenses(context: Context,fromDate:String,toDate:String): Double {

        mFilterTotalExpenses = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpenses(fromDate,toDate).totalAmount
        return mFilterTotalExpenses
    }

    suspend fun  getExpensesWithItemNameAndType(context: Context, fromDate:String, toDate:String) :List<ExpensesEntityWithItemNameAndType>{

        mExpensesEntityWithItemNameAndType = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpensesWithItemNameAndType(fromDate,toDate)
        return mExpensesEntityWithItemNameAndType
    }
}