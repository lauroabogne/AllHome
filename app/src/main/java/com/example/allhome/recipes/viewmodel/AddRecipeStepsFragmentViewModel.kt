package com.example.allhome.recipes.viewmodel

import androidx.lifecycle.ViewModel
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeStepEntity

class AddRecipeStepsFragmentViewModel: ViewModel() {

    val mRecipeStepEntities = arrayListOf<RecipeStepEntity>()
}