package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.StorageEntity

@Dao
interface RecipeDAO {
    @Insert
    suspend fun addItem(recipeEntity: RecipeEntity):Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateItem(recipeEntity: RecipeEntity):Long

    @Query("SELECT * FROM recipes where status = ${RecipeEntity.NOT_DELETED_STATUS}")
    suspend fun getRecipes():List<RecipeEntity>
    @Query("UPDATE recipes SET status=${RecipeEntity.DELETED_STATUS} WHERE unique_id=:uniqueId ")
    suspend fun updateRecipeByUniqueIdAsDeleted(uniqueId:String):Int
    /*@Query("SELECT * FROM recipes where estimated_cost=:coststatus = ${RecipeEntity.NOT_DELETED_STATUS}")
    suspend fun getRecipes(cost:Double,serving:Int,preparationPlusCookTime:Int):List<RecipeEntity>*/

}