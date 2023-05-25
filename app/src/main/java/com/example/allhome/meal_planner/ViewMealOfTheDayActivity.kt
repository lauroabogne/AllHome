package com.example.allhome.meal_planner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.allhome.R
import com.example.allhome.recipes.ViewRecipeFragment

class ViewMealOfTheDayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ThemeYellow)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_meal_of_the_day)
        intent.getStringExtra(ViewMealOfTheDayFragment.DATE_SELECTED_PARAM)?.let {
            Toast.makeText(this,"Here I am.", Toast.LENGTH_SHORT).show()
            val viewMealOfTheDayFragment = ViewMealOfTheDayFragment.newInstance(it)
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer,viewMealOfTheDayFragment).commit()
        }?:run{
            Toast.makeText(this,"Here I am another.", Toast.LENGTH_SHORT).show()
        }


    }
}