package com.example.allhome.meal_planner.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.data.entities.MealTypes
import com.example.allhome.data.entities.RecipeEntity
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
    suspend fun getRecipe(context:Context,recipeUniqueId:String):RecipeEntity{
        val recipeDAO = AllHomeDatabase.getDatabase(context).getRecipeDAO()
        return recipeDAO.getRecipeByUniqueId(recipeUniqueId)
    }
    suspend fun getRecipeUniqueIDs(context:Context,startDateOfTheMonth:String,endDateOfTheMonth:String):List<String>{
        val recipeDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        return recipeDAO.getRecipeUniqueIDs(startDateOfTheMonth,endDateOfTheMonth)
    }
    suspend fun getQuickRecipe(context:Context,recipeUniqueId:String):MealEntity{
        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        return mealDAO.getQuickRecipeMeal(recipeUniqueId)
    }
    suspend fun updateMealAsDeleted(context:Context,mealEntityUniqueId:String):Int{
        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        return mealDAO.updateMealAsDeleted(mealEntityUniqueId)
    }
    suspend fun getTotalCostInTheDay(context:Context,date:String):Double{
        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        val totalCost = mealDAO.getTotalCostInTheDay(date)
        return totalCost?.toDouble() ?: 0.0

    }
    suspend fun getTotalCostInTheMonth(context: Context,startDateOfTheMonth:String,endDateOfTheMonth:String):Double{

        val mealDAO = AllHomeDatabase.getDatabase(context).getMealDAO()
        val totalCost = mealDAO.getTotalCostInTheMonth(startDateOfTheMonth,endDateOfTheMonth)
        return totalCost?.toDouble() ?: 0.0

    }

}