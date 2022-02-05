package com.example.allhome.grocerylist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.example.allhome.R
import com.example.allhome.recipes.ViewRecipeFragment

class BrowserItemImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser_item_image)


        val browseItemImageFragment = BrowseItemImageFragment.newInstance("","")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer,browseItemImageFragment)
            .commit()

    }


}