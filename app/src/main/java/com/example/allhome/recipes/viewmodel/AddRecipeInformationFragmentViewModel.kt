package com.example.allhome.recipes.viewmodel

import androidx.lifecycle.ViewModel
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity

class AddRecipeInformationFragmentViewModel:ViewModel() {
    var mRecipeEntity:RecipeEntity? = null

    var mTempPrepaTimeHour = 0
    var mTempPrepaTimeMinutes =0
    var mTempCookTimeHour = 0
    var mTempCookTimeMinutes = 0


}