package com.example.allhome.grocerylist

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.allhome.R

class BrowserItemImageActivity : AppCompatActivity() {
    private val TAG:String by lazy {
        this@BrowserItemImageActivity::class.java.name

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser_item_image)
        Log.e(TAG,"no data available")
        intent.getStringExtra(BrowseItemImageFragment.ARG_ITEM_NAME)?.let {
            val browseItemImageFragment = BrowseItemImageFragment.newInstance(it)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,browseItemImageFragment)
                .commit()
        }?:run{
            Log.e(TAG,"no data available")
        }


    }


}