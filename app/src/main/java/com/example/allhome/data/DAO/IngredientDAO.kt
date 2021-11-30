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
     fun saveIngredients(ingredients:List<IngredientEntity>):List<Long>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun saveOrUpdateIngredients(ingredients:List<IngredientEntity>):List<Long>

    @Query("SELECT * FROM ingredients WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
     fun getIngredientsByRecipeUniqueId(recipeUniqueId:String):List<IngredientEntity>
    @Query("SELECT ${IngredientEntity.COLUMN_UNIQUE_ID},${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID}," +
            "${IngredientEntity.COLUMN_QUANTITY},${IngredientEntity.COLUMN_UNIT},${IngredientEntity.COLUMN_STATUS}," +
            "${IngredientEntity.COLUMN_UPLOADED},${IngredientEntity.COLUMN_CREATED},${IngredientEntity.COLUMN_MODIFIED}," +
            "${IngredientEntity.COLUMN_QUANTITY}||' '||${IngredientEntity.COLUMN_UNIT}||' '||${IngredientEntity.COLUMN_NAME} as ${IngredientEntity.COLUMN_NAME}" +
            " FROM ingredients WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
     fun getIngredientsByRecipeUniqueIdForEditing(recipeUniqueId:String):List<IngredientEntity>

    @Query("SELECT *,${IngredientEntityTransferringToGroceryList.SELECTED} as 'isSelected' FROM ingredients WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
     fun getIngredientsForGroceryListByRecipeUniqueId(recipeUniqueId:String):List<IngredientEntityTransferringToGroceryList>

    @Query("SELECT *,${IngredientEntityTransferringToGroceryList.SELECTED} as 'isSelected' FROM ingredients WHERE recipe_unique_id IN(:recipeUniqueIds) AND status  =  ${IngredientEntity.NOT_DELETED_STATUS}")
     fun getIngredientsForGroceryListByRecipeUniqueIds(recipeUniqueIds:List<String>):List<IngredientEntityTransferringToGroceryList>

    @Query("UPDATE ingredients SET status=${IngredientEntity.DELETED_STATUS} WHERE recipe_unique_id=:recipeUniqueId")
     fun updateIngredientByRecipeUniqueIdAsDeleted(recipeUniqueId:String):Int

    @Query(" SELECT ${IngredientEntity.COLUMN_NAME} " +
            " FROM  ${IngredientEntity.TABLE_NAME} "+
            " WHERE ${IngredientEntity.COLUMN_NAME} LIKE '%'||:searchTerm||'%' AND ${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS} " +
            " GROUP BY ${IngredientEntity.COLUMN_NAME} ORDER BY ${IngredientEntity.COLUMN_NAME} ")
     fun getIngredientForAutousuggest(searchTerm:String):List<String>

}
