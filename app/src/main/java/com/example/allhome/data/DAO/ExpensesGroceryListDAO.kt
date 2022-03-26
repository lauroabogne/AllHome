package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.allhome.data.entities.*
import com.example.allhome.data.relations.GroceryListAndGroceryItems
import com.example.allhome.data.relations.GroceryListAndGroceryItemsName
import com.example.allhome.data.relations.GroceryListAndGroceryItemsName1

@Dao
interface ExpensesGroceryListDAO {
    @Insert
    fun addItem(expensesGroceryListEntity: ExpensesGroceryListEntity):Long
}

