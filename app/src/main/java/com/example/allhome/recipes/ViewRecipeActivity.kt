package com.example.allhome.recipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.storage.StorageActivity
import com.example.allhome.utils.IngredientEvaluator

class ViewRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        val recipeEntity  = intent.getParcelableExtra<RecipeEntity>(ViewRecipeFragment.RECIPE_INTENT_TAG)!!

        val viewRecipeFragment = ViewRecipeFragment.newInstance(recipeEntity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.recipeFragmentContainer,viewRecipeFragment)
            .commit()
    }
}