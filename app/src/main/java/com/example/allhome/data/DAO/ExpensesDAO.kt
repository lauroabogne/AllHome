package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillPaymentEntity
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType

@Dao
interface ExpensesDAO {
    @Insert
    fun saveExpense(expensesEntity: ExpensesEntity):Long

    @Query(
        "SELECT RANDOM() as unique_id, expense_date, TOTAL(amount) as amount , name as item_name,'other expenses' as expense_type FROM expenses " +
                " WHERE " +
                " DATE(expense_date) >=:fromDate AND DATE(expense_date) <=:toDate" +
                " GROUP BY name ORDER BY expense_date ASC"
    )
    fun getExpenses(fromDate: String, toDate: String): List<ExpensesEntityWithItemNameAndType>
    @Query(
        "SELECT TOTAL(amount) FROM expenses WHERE DATE(expense_date) >=:fromDate AND DATE(expense_date) <=:toDate"
    )
    fun getTotalPaymentAmount(fromDate: String, toDate: String): Double

}

