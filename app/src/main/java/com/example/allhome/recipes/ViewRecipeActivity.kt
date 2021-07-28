package com.example.allhome.recipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.storage.StorageActivity

class ViewRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        val recipeEntity  = intent.getParcelableExtra<RecipeEntity>(ViewRecipeFragment.RECIPE_INTENT_TAG)!!

        //intent.putExtra(ViewRecipeFragment.RECIPE_INTENT_TAG,recipeEntity)
        val viewRecipeFragment = ViewRecipeFragment.newInstance(recipeEntity)
        supportFragmentManager.beginTransaction()
            .replace(R.id.recipeFragmentContainer,viewRecipeFragment)
            .commit()
    }
}