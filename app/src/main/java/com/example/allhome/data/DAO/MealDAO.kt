package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.*

@Dao
interface MealDAO {
    @Insert
     fun addMealPlan(mealEntity: MealEntity):Long

    @Query("SELECT ${MealEntity.COLUMN_TYPE} FROM meals WHERE ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED} GROUP BY ${MealEntity.COLUMN_TYPE}")
     fun getMealPlanType(date:String):List<MealTypes>
    @Query("SELECT * FROM meals WHERE ${MealEntity.COLUMN_TYPE} =:type AND  ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED}")
     fun getMealByTypeAndDate(type:Int,date:String):List<MealEntity>
    @Query("SELECT * FROM meals WHERE  ${MealEntity.COLUMN_UNIQUE_ID} = :uniqueId AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED} LIMIT 1")
     fun getQuickRecipeMeal(uniqueId:String):MealEntity

    @Query("UPDATE meals SET ${MealEntity.COLUMN_DELETED}=${MealEntity.DELETED} WHERE ${MealEntity.COLUMN_UNIQUE_ID}=:uniqueId ")
     fun updateMealAsDeleted(uniqueId:String):Int
    @Query("SELECT SUM(${MealEntity.COLUMN_COST}) FROM ${MealEntity.TABLE_NAME} WHERE ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED} = ${MealEntity.NOT_DELETED}")
     fun getTotalCostInTheDay(date:String):String?

    @Query("SELECT SUM(${MealEntity.COLUMN_COST}) FROM ${MealEntity.TABLE_NAME} WHERE ${MealEntity.COLUMN_DATE} >= :startDateOfTheMonth AND ${MealEntity.COLUMN_DATE} <= :endDateOfTheMonth  AND ${MealEntity.COLUMN_DELETED} = ${MealEntity.NOT_DELETED}")
     fun getTotalCostInTheMonth(startDateOfTheMonth:String,endDateOfTheMonth:String):String?


    @Query("SELECT ${MealEntity.COLUMN_RECIPE_UNIQUE_ID} FROM ${MealEntity.TABLE_NAME} WHERE ${MealEntity.COLUMN_KIND} = ${MealEntity.RECIPE_KIND} AND ${MealEntity.COLUMN_DATE} >= :startDateOfTheMonth AND ${MealEntity.COLUMN_DATE} <= :endDateOfTheMonth  AND ${MealEntity.COLUMN_DELETED} = ${MealEntity.NOT_DELETED}")
     fun getRecipeUniqueIDs(startDateOfTheMonth:String,endDateOfTheMonth:String):List<String>
}