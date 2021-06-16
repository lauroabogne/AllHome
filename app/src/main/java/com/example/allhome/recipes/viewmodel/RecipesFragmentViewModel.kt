package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RecipesFragmentViewModel:ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RecipesFragmentViewModel"))

    suspend fun getRecipes(context: Context): List<RecipeEntity> {
        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes()
    }
    suspend fun getIngredients(context:Context,recipeUniqueId:String):List<IngredientEntity>{
        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun getIngredientsForTransferringInGroceryList(context:Context,recipeUniqueId:String):List<IngredientEntityTransferringToGroceryList>{
        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsForGroceryListByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun getSteps(context:Context,recipeUniqueId:String):List<RecipeStepEntity>{
        return AllHomeDatabase.getDatabase(context).getRecipeStepDAO().getStepsByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun deleteRecipe(context:Context,recipeUniqueId:String){
        val recipeDAO = AllHomeDatabase.getDatabase(context).getRecipeDAO()
        val recipeStepDAO = AllHomeDatabase.getDatabase(context).getRecipeStepDAO()
        val ingredientDAO = AllHomeDatabase.getDatabase(context).getIngredientDAO()

        recipeDAO.updateRecipeByUniqueIdAsDeleted(recipeUniqueId)
        recipeStepDAO.updateStepsByRecipeUniqueIdAsDeleted(recipeUniqueId)
        ingredientDAO.updateIngredientByRecipeUniqueIdAsDeleted(recipeUniqueId)
    }
}

