package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.GroceryListWithItemCount
import com.example.allhome.data.entities.IngredientEntity

@Dao
interface IngredientDAO{

    @Insert
    suspend fun saveIngredients(ingredients:List<IngredientEntity>):List<Long>
    @Query("SELECT * FROM ingredients WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
    suspend fun getIngredientsByRecipeUniqueId(recipeUniqueId:String):List<IngredientEntity>

}
