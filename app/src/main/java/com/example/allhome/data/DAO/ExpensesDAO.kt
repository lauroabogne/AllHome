package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.BillPaymentEntity
import com.example.allhome.data.entities.ExpensesEntity

@Dao
interface ExpensesDAO {
    @Insert
    fun saveExpense(expensesEntity: ExpensesEntity):Long
}