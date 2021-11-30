package com.example.allhome.data.DAO

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.allhome.data.entities.*

@Dao
interface RecipeDAO {
    @Insert
     fun addItem(recipeEntity: RecipeEntity):Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun addOrUpdateItem(recipeEntity: RecipeEntity):Long

    @Query("SELECT *,0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT},0 AS ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT} FROM ${RecipeEntity.TABLE_NAME} WHERE ${RecipeEntity.COLUMN_NAME} LIKE '%'||:searchTerm||'%' AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS} ORDER BY ${RecipeEntity.COLUMN_NAME}")
     fun getRecipes(searchTerm:String):List<RecipeEntityWithTotalIngredient>

    @Query("SELECT *,0 as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT},0 AS ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT} FROM ${RecipeEntity.TABLE_NAME} " +
            " WHERE ${RecipeEntity.COLUMN_UNIQUE_ID} =:uniqueId AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}")
     fun getRecipe(uniqueId:String):RecipeEntityWithTotalIngredient

    @Query("UPDATE recipes SET status=${RecipeEntity.DELETED_STATUS} WHERE unique_id=:uniqueId ")
     fun updateRecipeByUniqueIdAsDeleted(uniqueId:String):Int

    @Query("SELECT ${RecipeEntity.TABLE_NAME}.*," +
            " (" +
                " SELECT COUNT(*) FROM ${IngredientEntity.TABLE_NAME} " +
                " WHERE ${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID} = ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID}" +
                " AND ${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS}" +
                " ) as ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT}," +
            " (" +
                " SELECT COUNT(*) FROM ${IngredientEntity.TABLE_NAME} " +
                " WHERE ${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID} = ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID}" +
                " AND ${IngredientEntity.COLUMN_NAME} IN (:ingredients)" +
                " AND ${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS}" +
            " ) AS ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_MATCH_COUNT}," +
            " ((" +
            " SELECT COUNT(*) FROM ${IngredientEntity.TABLE_NAME} " +
            " WHERE ${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID} = ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID}" +
            " AND ${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS})" +
            " - " +
            " (SELECT COUNT(*) FROM ${IngredientEntity.TABLE_NAME} " +
            " WHERE ${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID} = ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID}" +
            " AND ${IngredientEntity.COLUMN_NAME} IN (:ingredients)" +
            " AND ${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS}" +
            " )) AS ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT_LESS_TOTAL_INGREDIENT_MATCH_COUNT} " +
            " FROM ${RecipeEntity.TABLE_NAME} " +
            " LEFT JOIN ${IngredientEntity.TABLE_NAME} " +
            " ON ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID} = ${IngredientEntity.TABLE_NAME}.${IngredientEntity.COLUMN_RECIPE_UNIQUE_ID}" +
            " WHERE ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_NAME} LIKE '%'||:searchTerm||'%' " +
            " AND ${IngredientEntity.TABLE_NAME}.${IngredientEntity.COLUMN_NAME} IN (:ingredients)" +
            " AND ${IngredientEntity.TABLE_NAME}.${IngredientEntity.COLUMN_STATUS} = ${IngredientEntity.NOT_DELETED_STATUS} " +
            " AND ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS}" +
            " GROUP BY ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_UNIQUE_ID}" +
            " ORDER BY ${RecipeEntityWithTotalIngredient.TOTAL_INGREDIENT_COUNT_LESS_TOTAL_INGREDIENT_MATCH_COUNT} ASC," +
            " ${RecipeEntity.TABLE_NAME}.${RecipeEntity.COLUMN_NAME}")
     fun getRecipesByIngredients(searchTerm:String,ingredients:List<String>):List<RecipeEntityWithTotalIngredient>
    /*@Query("SELECT * FROM recipes where estimated_cost :costCondition :cost  AND status = ${RecipeEntity.NOT_DELETED_STATUS}")
    suspend fun getRecipes(costCondition:String,cost:Double,servingCondition:String,serving:Int,totalPrepAndCookTimeInMinutesCondtion:String,
                           preparationPlusCookTime:Int):List<RecipeEntity>*/

    @RawQuery
     fun getRecipes(query:SupportSQLiteQuery):List<RecipeEntityWithTotalIngredient>

    @Query("SELECT *  FROM  ${RecipeEntity.TABLE_NAME} WHERE unique_id=:uniqueId  AND ${RecipeEntity.COLUMN_STATUS} = ${RecipeEntity.NOT_DELETED_STATUS} LIMIT 1")
     fun getRecipeByUniqueId(uniqueId:String):RecipeEntity
}