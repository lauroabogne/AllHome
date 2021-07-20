package com.example.allhome.meal_planner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.allhome.R
import com.example.allhome.data.entities.MealEntity
import com.example.allhome.recipes.RecipesFragment

class ViewerActivity : AppCompatActivity() {
    companion object{
        const val TITLE_TAG = "title"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_viewer)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        intent.getStringExtra(TITLE_TAG)?.let{titleParam->
            toolbar.title = titleParam
        }

        setSupportActionBar(toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener(toolbarNavigationClickListener)

        val quickRecipeFragment = QuickRecipeFragment.newInstance("test",0.0,QuickRecipeFragment.VIEW_ACTION)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container,quickRecipeFragment)
            .commit()
    }

    val toolbarNavigationClickListener= object: View.OnClickListener{
        override fun onClick(v: View?) {
            Toast.makeText(this@ViewerActivity,"Test",Toast.LENGTH_SHORT).show()
            this@ViewerActivity.finish()
        }

    }
}