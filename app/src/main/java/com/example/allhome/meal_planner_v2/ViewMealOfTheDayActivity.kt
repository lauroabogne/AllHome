package com.example.allhome.meal_planner_v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.allhome.R
import com.example.allhome.recipes.ViewRecipeFragment

class ViewMealOfTheDayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ThemeYellow)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_meal_of_the_day)
        intent.getStringExtra(ViewMealOfTheDayFragment.DATE_SELECTED_PARAM)?.let {
            val viewMealOfTheDayFragment = ViewMealOfTheDayFragment.newInstance(it)
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,viewMealOfTheDayFragment).commit()
        }


    }
}