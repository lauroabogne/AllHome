package com.example.allhome.expenses.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.allhome.data.DAO.ExpensesGroceryItemDAO
import com.example.allhome.data.DAO.ExpensesGroceryListDAO
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.text.SimpleDateFormat
import java.util.*

val READABLE_SIMPLE_DATE_FORMAT = SimpleDateFormat("MMM d,yyyy")
val SIMPLE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
class ExpensesSummaryByGroceryItemsViewModel(private val expensesGroceryListDAO: ExpensesGroceryListDAO, private val expensesGroceryItemDAO: ExpensesGroceryItemDAO) : ViewModel() {

    var mDateFromFilter:Calendar = Calendar.getInstance()
    var mDateToFilter:Calendar = Calendar.getInstance()
    val mLoadData:MutableLiveData<Boolean> = MutableLiveData(false)
    val mTotalGroceryItemExpenses:MutableLiveData<Double> by lazy{
        MutableLiveData<Double>()
    }
    val mGroceryItemExpense: MutableLiveData<List<ExpensesEntityWithItemNameAndType>> by lazy {
        MutableLiveData<List<ExpensesEntityWithItemNameAndType>>()
    }

    fun getTotalExpensesByDateRange(){
        val readableFromDate = SIMPLE_DATE_FORMAT.format(mDateFromFilter.time)
        val readableToDate = SIMPLE_DATE_FORMAT.format(mDateToFilter.time)

        viewModelScope.launch {
            mTotalGroceryItemExpenses.value = withContext(IO){
                expensesGroceryItemDAO.getTotalPaymentAmount(readableFromDate, readableToDate)
            }

        }
    }
    fun getExpensesGroceryItems() {
        val readableFromDate = SIMPLE_DATE_FORMAT.format(mDateFromFilter.time)
        val readableToDate = SIMPLE_DATE_FORMAT.format(mDateToFilter.time)
        viewModelScope.launch {
            mGroceryItemExpense.value = withContext(IO){
                expensesGroceryItemDAO.getExpenses(readableFromDate,readableToDate)
             }
        }
    }
}

class ExpensesSummaryByGroceryItemsViewModelFactory(private val expensesGroceryListDAO: ExpensesGroceryListDAO, private val expensesGroceryItemDAO: ExpensesGroceryItemDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesSummaryByGroceryItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesSummaryByGroceryItemsViewModel(expensesGroceryListDAO,expensesGroceryItemDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}