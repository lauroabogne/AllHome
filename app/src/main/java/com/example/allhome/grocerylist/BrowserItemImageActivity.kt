package com.example.allhome.grocerylist

import android.content.Intent
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

        val itemName = intent.getStringExtra(BrowseItemImageFragment.ITEM_NAME_TAG)!!
        val price = intent.getDoubleExtra(BrowseItemImageFragment.ITEM_PRICE_TAG,0.0)
        val unit = intent.getStringExtra(BrowseItemImageFragment.ITEM_UNIT_TAG)!!
        val imageName = intent.getStringExtra(BrowseItemImageFragment.ITEM_IMAGE_NAME_TAG)!!


            val browseItemImageFragment = BrowseItemImageFragment.newInstance(itemName,unit,price,imageName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,browseItemImageFragment)
                .commit()


    }


}