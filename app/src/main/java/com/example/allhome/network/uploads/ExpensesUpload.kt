package com.example.allhome.network.uploads

import android.util.Log
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.network.UploadApi

class ExpensesUpload(
    private val apiService: UploadApi,
    private val expensesDao:  ExpensesDAO
    ) {

    suspend fun uploadExpenses(expenses: List<ExpensesEntity>) {
        try {
            val response = apiService.uploadExpenses(expenses)
            if (response.isSuccessful) {
               // expensesDao.insertExpenses(expenses)
                Log.e("Upload", "Expenses uploaded successfully")
            } else {
                // Handle error

                val errorCode = response.code()
                val errorMessage = response.errorBody()?.string() ?: "No error body"
                Log.e("Upload", "Expenses uploaded failed with code $errorCode. Error: $errorMessage")
            }
        } catch (e: Exception) {
            // Handle exception
            Log.e("Upload", "Expenses upload failed: ${e.message}")
        }
    }

}