package com.example.allhome.expenses.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.BillDAO
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.DAO.ExpensesGroceryItemDAO
import com.example.allhome.data.DAO.ExpensesGroceryListDAO
import com.example.allhome.data.entities.ExpensesEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.*
import kotlin.collections.ArrayList

class ExpensesFragmentViewModel(private val expensesDAO: ExpensesDAO,private val billDAO: BillDAO) : ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("ExpensesFragmentViewModel"))

    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()
    var mFilterTotalExpenses: Double = 0.0
    var mCurrentYearTotalExpenses: Double = 0.0
    var mExpensesPerMonth = arrayListOf<ExpensesEntity>()
    val mSaveSuccessfully:MutableLiveData<Boolean> = MutableLiveData()

    suspend fun getExpenses(context: Context, fromDate: String, toDate: String): Double {
        mFilterTotalExpenses = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpenses(fromDate, toDate).amount
        return mFilterTotalExpenses
    }
    suspend fun getExpensesByMonth(context: Context, month: String): ExpensesEntity {

        return AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpensesInMonth(month)
    }
    suspend fun getCurrentYearExpenses(context: Context, fromDate: String, toDate: String): Double {

        mCurrentYearTotalExpenses = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpenses(fromDate, toDate).amount
        return mCurrentYearTotalExpenses
    }

    suspend fun getExpensesPerMonth(context: Context, fromDate: String, toDate: String): List<ExpensesEntity> {
        mExpensesPerMonth = AllHomeDatabase.getDatabase(context).getBillItemDAO().getExpensesPerMonth(fromDate, toDate) as ArrayList<ExpensesEntity>
        return mExpensesPerMonth
    }

    fun addExpense(expensesEntity: ExpensesEntity){
        viewModelScope.launch {
           val id =  withContext(IO){
                expensesDAO.saveExpense(expensesEntity)
            }
            if(expensesEntity.name.equals("failed")){
                mSaveSuccessfully.postValue(false)
            }else{
                mSaveSuccessfully.postValue(id  > 0)
            }
        }
    }
}

class ExpensesFragmentViewModelViewModelFactory(private val expensesDAO: ExpensesDAO, private val billDAO: BillDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesFragmentViewModel(expensesDAO,billDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}