package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.allhome.data.entities.GroceryListWithItemCount
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.IngredientEntityTransferringToGroceryList
import com.example.allhome.data.entities.RecipeEntity

@Dao
interface IngredientDAO{

    @Insert
    suspend fun saveIngredients(ingredients:List<IngredientEntity>):List<Long>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOrUpdateIngredients(ingredients:List<IngredientEntity>):List<Long>

    @Query("SELECT * FROM ingredients WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
    suspend fun getIngredientsByRecipeUniqueId(recipeUniqueId:String):List<IngredientEntity>
    @Query("SELECT *,${IngredientEntityTransferringToGroceryList.SELECTED} as 'isSelected' FROM ingredients WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
    suspend fun getIngredientsForGroceryListByRecipeUniqueId(recipeUniqueId:String):List<IngredientEntityTransferringToGroceryList>

    @Query("UPDATE ingredients SET status=${IngredientEntity.DELETED_STATUS} WHERE recipe_unique_id=:recipeUniqueId")
    suspend fun updateIngredientByRecipeUniqueIdAsDeleted(recipeUniqueId:String):Int

}
