package com.example.allhome.recipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.allhome.R

class ViewRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        val viewRecipeFragment = ViewRecipeFragment.newInstance("Test","Test")
        supportFragmentManager.beginTransaction()
            .replace(R.id.recipeFragmentContainer,viewRecipeFragment)
            .commit()
    }
}