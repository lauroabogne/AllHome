package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.allhome.data.entities.ExpensesCategoriesEntity

@Dao
interface ExpensesCategoriesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertAndReplaceOnConflict(expenseCategory: ExpensesCategoriesEntity):Long
     @Insert
     fun insert(expenseCategory: ExpensesCategoriesEntity):Long

    @Update
     fun update(expenseCategory: ExpensesCategoriesEntity)

    @Delete
     fun delete(expenseCategory: ExpensesCategoriesEntity)

    @Query("SELECT * FROM expenses_categories WHERE unique_id = :uniqueId")
     fun getExpenseCategoryById(uniqueId: String): ExpensesCategoriesEntity?

    @Query("SELECT * FROM expenses_categories")
    fun getAllExpenseCategories(): LiveData<List<ExpensesCategoriesEntity>>

    @Query("DELETE FROM expenses_categories")
     fun deleteAllExpenseCategories(): Int
     @Query("SELECT * FROM expenses_categories WHERE ${ExpensesCategoriesEntity.COLUMN_NAME} LIKE '%' || :searchQuery || '%'")
     fun searchCategories(searchQuery:String):List<ExpensesCategoriesEntity>

}