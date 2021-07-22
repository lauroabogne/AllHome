package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.*

@Dao
interface MealDAO {
    @Insert
    suspend fun addMealPlan(mealEntity: MealEntity):Long

    @Query("SELECT ${MealEntity.COLUMN_TYPE} FROM meals WHERE ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED} GROUP BY ${MealEntity.COLUMN_TYPE}")
    suspend fun getMealPlanType(date:String):List<MealTypes>
    @Query("SELECT * FROM meals WHERE ${MealEntity.COLUMN_TYPE} =:type AND  ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED}")
    suspend fun getMealByTypeAndDate(type:Int,date:String):List<MealEntity>
    @Query("SELECT * FROM meals WHERE  ${MealEntity.COLUMN_UNIQUE_ID} = :uniqueId AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED} LIMIT 1")
    suspend fun getQuickRecipeMeal(uniqueId:String):MealEntity

    @Query("UPDATE meals SET ${MealEntity.COLUMN_DELETED}=${MealEntity.DELETED} WHERE ${MealEntity.COLUMN_UNIQUE_ID}=:uniqueId ")
    suspend fun updateMealAsDeleted(uniqueId:String):Int
    @Query("SELECT SUM(${MealEntity.COLUMN_COST}) FROM ${MealEntity.TABLE_NAME} WHERE ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED} = ${MealEntity.NOT_DELETED}")
    suspend fun getTotalCostInTheDay(date:String):String?

    @Query("SELECT SUM(${MealEntity.COLUMN_COST}) FROM ${MealEntity.TABLE_NAME} WHERE ${MealEntity.COLUMN_DATE} >= :startDateOfTheMonth AND ${MealEntity.COLUMN_DATE} <= :endDateOfTheMonth  AND ${MealEntity.COLUMN_DELETED} = ${MealEntity.NOT_DELETED}")
    suspend fun getTotalCostInTheMonth(startDateOfTheMonth:String,endDateOfTheMonth:String):String?
}