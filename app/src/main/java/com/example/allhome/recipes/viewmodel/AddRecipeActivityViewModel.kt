package com.example.allhome.recipes.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.room.withTransaction
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AddRecipeActivityViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("AddRecipeActivityViewModel"))
    suspend fun saveRecipe(context: Context,recipe:RecipeEntity,ingredient:List<IngredientEntity>,steps:List<RecipeStepEntity>){
        val allHomeDatabase = AllHomeDatabase.getDatabase(context)

        val recipeDAO = allHomeDatabase.getRecipeDAO()
        val ingredientDAO = allHomeDatabase.getIngredientDAO()
        val recipeStepDAO = allHomeDatabase.getRecipeStepDAO()

        allHomeDatabase.withTransaction {
            val recipeId = recipeDAO.addItem(recipe)
            val ingredientIDs = ingredientDAO.saveIngredients(ingredient)
            val recipeStepIDs = recipeStepDAO.saveSteps(steps)

            Log.e("recipeId",recipeId.toString());
            Log.e("ingredientIDs",ingredientIDs.toString());
            Log.e("recipeStepIDs",recipeStepIDs.toString());
        }

    }

    suspend fun updateRecipe(context: Context,recipe:RecipeEntity,ingredient:List<IngredientEntity>,steps:List<RecipeStepEntity>){
        val allHomeDatabase = AllHomeDatabase.getDatabase(context)

        val recipeDAO = allHomeDatabase.getRecipeDAO()
        val ingredientDAO = allHomeDatabase.getIngredientDAO()
        val recipeStepDAO = allHomeDatabase.getRecipeStepDAO()

        allHomeDatabase.withTransaction {
            // update as deleted first
            recipeDAO.updateRecipeByUniqueIdAsDeleted(recipe.uniqueId)
            ingredientDAO.updateIngredientByRecipeUniqueIdAsDeleted(recipe.uniqueId)
            recipeStepDAO.updateStepsByRecipeUniqueIdAsDeleted(recipe.uniqueId)

            val recipeId = recipeDAO.addOrUpdateItem(recipe)
            val ingredientIDs = ingredientDAO.saveOrUpdateIngredients(ingredient)
            val recipeStepIDs = recipeStepDAO.saveOrUpdateSteps(steps)

            Log.e("recipeId",recipeId.toString());
            Log.e("ingredientIDs",ingredientIDs.toString());
            Log.e("recipeStepIDs",recipeStepIDs.toString());
        }

    }

}