package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeStepEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AddRecipeStepsFragmentViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("AddRecipeIngredientsFragmentModel"))
    var mRecipeStepEntities = arrayListOf<RecipeStepEntity>()
    suspend fun getSteps(context: Context,recipeUniqueId:String):List<RecipeStepEntity>{
        return AllHomeDatabase.getDatabase(context).getRecipeStepDAO().getStepsByRecipeUniqueId(recipeUniqueId)
    }
}