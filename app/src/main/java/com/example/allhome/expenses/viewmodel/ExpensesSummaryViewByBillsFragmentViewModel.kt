package com.example.allhome.expenses.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.allhome.data.DAO.BillDAO
import com.example.allhome.data.DAO.BillPaymentDAO
import com.example.allhome.data.DAO.ExpensesGroceryItemDAO
import com.example.allhome.data.DAO.ExpensesGroceryListDAO
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ExpensesSummaryViewByBillsFragmentViewModel(private val billPaymentDAO: BillPaymentDAO) : ViewModel() {
    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()
    val mLoadData: MutableLiveData<Boolean> = MutableLiveData(false)
    val mTotalBillExpenses: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }
    val mBillExpenses: MutableLiveData<List<ExpensesEntityWithItemNameAndType>> by lazy {
        MutableLiveData<List<ExpensesEntityWithItemNameAndType>>()
    }

    fun getBillExpenses() {

        val readableFromDate = SIMPLE_DATE_FORMAT.format(mDateFromFilter.time)
        val readableToDate = SIMPLE_DATE_FORMAT.format(mDateToFilter.time)

        viewModelScope.launch {
            mBillExpenses.value = withContext(IO) {
                billPaymentDAO.getBillPaymentExpenses(readableFromDate, readableToDate)
            }
        }
    }
}

class ExpensesSummaryViewByBillsFragmentViewModelFactory(private val billPaymentDAO: BillPaymentDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesSummaryViewByBillsFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesSummaryViewByBillsFragmentViewModel(billPaymentDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

