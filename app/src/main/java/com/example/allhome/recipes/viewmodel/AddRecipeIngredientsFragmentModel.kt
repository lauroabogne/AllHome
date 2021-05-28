package com.example.allhome.recipes.viewmodel

import androidx.lifecycle.ViewModel
import com.example.allhome.data.entities.IngredientEntity

class AddRecipeIngredientsFragmentModel:ViewModel() {

    val mIngredients = arrayListOf<IngredientEntity>()

    fun test(){

    }
}