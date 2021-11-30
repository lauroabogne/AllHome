package com.example.allhome.recipes.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeCategoryEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.data.entities.RecipeStepEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AddRecipeInformationFragmentViewModel:ViewModel() {

    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("AddRecipeInformationFragmentViewModel"))


    var mRecipeEntity:RecipeEntity? = null

    var mTempPrepaTimeHour = 0
    var mTempPrepaTimeMinutes =0
    var mTempCookTimeHour = 0
    var mTempCookTimeMinutes = 0

    var previousImageUri: Uri? = null
    var newImageUri: Uri? = null

    var mRecipeCategoryEntities = arrayListOf<RecipeCategoryEntity>()



    suspend fun getRecipeCategories(context: Context,recipeUniqueId:String): List<RecipeCategoryEntity> {
        val allHomeDatabase = AllHomeDatabase.getDatabase(context)

        val recipeCategoryAssignmentDAO = allHomeDatabase.getRecipeCategoryAssignmentDAO()
        return recipeCategoryAssignmentDAO.getRecipeCategories(recipeUniqueId)

    }



}