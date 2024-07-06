package com.example.allhome.expenses.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.allhome.data.DAO.ExpensesCategoriesDAO
import com.example.allhome.data.entities.ExpensesCategoriesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpensesCategoriesViewModel(private val expensesCategoriesDAO: ExpensesCategoriesDAO): ViewModel() {

    private val insertSuccessMutableLiveData = MutableLiveData<Boolean>()
    val insertSuccessLiveData: LiveData<Boolean> get() = insertSuccessMutableLiveData

    fun insert(expenseCategory: ExpensesCategoriesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = expensesCategoriesDAO.insert(expenseCategory)
                insertSuccessMutableLiveData.postValue(id != -1L)
            } catch (e: Exception) {
                insertSuccessMutableLiveData.postValue(false)
            }
        }
    }
    fun getCategory(searchQuery:String){
        expensesCategoriesDAO.searchCategories(searchQuery)
    }

}

class ExpensesCategoriesViewModelFactory(private val expensesCategoriesDAO: ExpensesCategoriesDAO) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesCategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesCategoriesViewModel(expensesCategoriesDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}