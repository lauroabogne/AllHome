package com.example.allhome.meal_planner.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.MealTypes
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MealPlannerViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("MealPlannerViewModel"))

    suspend fun saveMealPlan(context: Context,mealEntity:MealEntity):Long{
        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
       return mealDAO.addMealPlan(mealEntity)
    }
    suspend fun getMealPlanForDay(context: Context,date:String):List<MealTypes>{
        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        return mealDAO.getMealPlanType(date)
    }
    suspend fun getMealByTypeAndDate(context: Context,mealType:Int,date:String):List<MealEntity>{
        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        return mealDAO.getMealByTypeAndDate(mealType,date)
    }
}