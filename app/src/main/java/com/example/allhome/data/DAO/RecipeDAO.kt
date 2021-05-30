package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.RecipeEntity

@Dao
interface RecipeDAO {
    @Insert
    suspend fun addItem(recipeEntity: RecipeEntity):Long
}