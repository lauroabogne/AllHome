package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import com.example.allhome.data.entities.StorageEntity
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

    suspend fun getSteps(context:Context,recipeUniqueId:String):List<RecipeStepEntity>{
        return AllHomeDatabase.getDatabase(context).getRecipeStepDAO().getStepsByRecipeUniqueId(recipeUniqueId)
    }
}

