package com.example.allhome.recipes.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.RecipeCategoryEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RecipeCategoryViewModel: ViewModel() {
    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("RecipeCategoryViewModel"))

    suspend fun add(context: Context, recipeCategory:RecipeCategoryEntity):Long{
        return AllHomeDatabase.getDatabase(context).getRecipeCategoryDAO().addItem(recipeCategory)
    }
    suspend fun getRecipeCategories(context:Context):List<RecipeCategoryEntity>{
        return AllHomeDatabase.getDatabase(context).getRecipeCategoryDAO().getRecipeCategories()
    }

}