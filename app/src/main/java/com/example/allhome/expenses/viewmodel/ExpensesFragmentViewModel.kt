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

class ExpensesFragmentViewModel:ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("ExpensesFragmentViewModel"))

    var mDateFromFilter:Calendar = Calendar.getInstance()
    var mDateToFilter:Calendar = Calendar.getInstance()
    var mFilterTotalExpenses:Double = 0.0


    suspend fun  getExpenses(context: Context,fromDate:String,toDate:String): Double {

        mFilterTotalExpenses = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpenses(fromDate,toDate).totalAmount
        return mFilterTotalExpenses
    }




}