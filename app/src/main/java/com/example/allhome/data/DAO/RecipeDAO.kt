package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.StorageEntity

@Dao
interface RecipeDAO {
    @Insert
    suspend fun addItem(recipeEntity: RecipeEntity):Long

    @Query("SELECT * FROM recipes where status = ${RecipeEntity.NOT_DELETED_STATUS}")
    suspend fun getRecipes():List<RecipeEntity>
}