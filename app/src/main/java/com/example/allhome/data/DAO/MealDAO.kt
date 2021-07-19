package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.*

@Dao
interface MealDAO {
    @Insert
    suspend fun addMealPlan(mealEntity: MealEntity):Long

    /*@Query("SELECT * FROM meals WHERE ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.DELETED}=${MealEntity.NOT_DELETED} GROUP BY ${MealEntity.COLUMN_TYPE}")
    suspend fun getMealPlanType( date:String):List<MealTypes>*/

    @Query("SELECT ${MealEntity.COLUMN_TYPE} FROM meals WHERE ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED} GROUP BY ${MealEntity.COLUMN_TYPE}")
    suspend fun getMealPlanType(date:String):List<MealTypes>
    @Query("SELECT * FROM meals WHERE ${MealEntity.COLUMN_TYPE} =:type AND  ${MealEntity.COLUMN_DATE} = :date AND ${MealEntity.COLUMN_DELETED}=${MealEntity.NOT_DELETED}")
    suspend fun getMealByTypeAndDate(type:Int,date:String):List<MealEntity>
}