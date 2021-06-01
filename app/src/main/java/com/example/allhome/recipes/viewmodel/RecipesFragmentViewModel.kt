package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.StorageEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RecipesFragmentViewModel:ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RecipesFragmentViewModel"))

    suspend fun getRecipes(context: Context): List<RecipeEntity> {

        return AllHomeDatabase.getDatabase(context).getRecipeDAO().getRecipes()

    }
}

