package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.IngredientEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AddRecipeIngredientsFragmentModel:ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("AddRecipeIngredientsFragmentModel"))
    var mIngredients = arrayListOf<IngredientEntity>()

    suspend fun getIngredients(context: Context,recipeUniqueId:String):List<IngredientEntity>{

        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsByRecipeUniqueId(recipeUniqueId)
    }

    suspend fun getIngredientsForEditing(context: Context,recipeUniqueId:String):List<IngredientEntity>{

        return AllHomeDatabase.getDatabase(context).getIngredientDAO().getIngredientsByRecipeUniqueIdForEditing(recipeUniqueId)
    }

}