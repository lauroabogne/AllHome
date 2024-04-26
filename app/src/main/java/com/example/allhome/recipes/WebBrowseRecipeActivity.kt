package com.example.allhome.recipes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R

class WebBrowseRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = (applicationContext as AllHomeBaseApplication).theme
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_browse_recipe)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer,BrowseRecipeFragment.newInstance("",""))
            commit()
        }
    }
}