package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillEntity
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
    @Query("SELECT ${ExpensesEntity.COLUMN_UNIQUE_ID} FROM ${ExpensesEntity.TABLE_NAME} WHERE ${ExpensesEntity.COLUMN_UPLOADED} = :notUpload ORDER BY ${ExpensesEntity.COLUMN_CREATED} ASC")
    suspend fun getUniqueIdsToUpload(notUpload: Int): List<String>
    @Query("SELECT * FROM ${ExpensesEntity.TABLE_NAME} WHERE ${ExpensesEntity.COLUMN_UNIQUE_ID} = :uniqueId")
    suspend fun getExpensesByUniqueId(uniqueId: String): ExpensesEntity?
    @Query("UPDATE ${ExpensesEntity.TABLE_NAME} SET ${ExpensesEntity.COLUMN_UPLOADED} = :uploaded WHERE ${ExpensesEntity.COLUMN_UNIQUE_ID} = :uniqueId")
    suspend fun updateExpensesAsUploaded(uniqueId: String, uploaded: Int):Int
}

