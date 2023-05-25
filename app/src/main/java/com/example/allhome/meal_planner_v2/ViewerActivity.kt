package com.example.allhome.meal_planner_v2

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

        val name = intent.getStringExtra(QuickRecipeFragment.NAME)
        val cost = intent.getDoubleExtra(QuickRecipeFragment.COST,0.0)


        val quickRecipeFragment = QuickRecipeFragment.newInstance(name!!,cost,QuickRecipeFragment.VIEW_ACTION)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container,quickRecipeFragment)
            .commit()
    }

    val toolbarNavigationClickListener= object: View.OnClickListener{
        override fun onClick(v: View?) {
            this@ViewerActivity.finish()
        }

    }
}