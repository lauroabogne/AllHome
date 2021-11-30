package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeEntityWithTotalIngredient

@Dao
interface RecipeCategoryDAO {
    @Insert
    fun addItem(recipeCategoryEntity: RecipeCategoryEntity):Long

    @Query("SELECT * FROM recipe_categories where status=${RecipeCategoryEntity.NOT_DELETED_STATUS} ORDER BY name")
    fun getRecipeCategories():List<RecipeCategoryEntity>
}