package com.example.allhome.data.DAO

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
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
    /*@Query("SELECT * FROM recipes where estimated_cost :costCondition :cost  AND status = ${RecipeEntity.NOT_DELETED_STATUS}")
    suspend fun getRecipes(costCondition:String,cost:Double,servingCondition:String,serving:Int,totalPrepAndCookTimeInMinutesCondtion:String,
                           preparationPlusCookTime:Int):List<RecipeEntity>*/

    @RawQuery
    suspend fun getRecipes(query:SupportSQLiteQuery):List<RecipeEntity>




}