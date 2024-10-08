package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.*

@Dao
interface ExpensesGroceryItemDAO {
    @Insert
    fun addItems(expensesGroceryListEntity: List<ExpensesGroceryItemEntity>): List<Long>

    @Query(
        "SELECT RANDOM() as unique_id,datetime_created as expense_date, TOTAL(quantity * price_per_unit) as amount , item_name,'grocery_item' as expense_type FROM expenses_grocery_items " +
                " WHERE " +
                " DATE(datetime_modified) >=:fromDate AND DATE(datetime_modified) <=:toDate" +
                " GROUP BY item_name"
    )
    fun getExpenses(fromDate: String, toDate: String): List<ExpensesEntityWithItemNameAndType>

    @Query(
        "SELECT TOTAL(quantity * price_per_unit) FROM expenses_grocery_items WHERE DATE(datetime_modified) >=:fromDate AND DATE(datetime_modified) <=:toDate"
    )
    fun getTotalPaymentAmount(fromDate: String, toDate: String): Double
}