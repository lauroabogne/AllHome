package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.ExpensesGroceryItemEntity
import com.example.allhome.data.entities.ExpensesGroceryListEntity
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityForAutoSuggest

@Dao
interface ExpensesGroceryItemDAO {
    @Insert
    fun addItems(expensesGroceryListEntity: List<ExpensesGroceryItemEntity>):List<Long>
}