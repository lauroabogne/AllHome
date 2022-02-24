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

        val itemName = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_NAME_TAG)!!
        val price = intent.getDoubleExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_PRICE_TAG,0.0)
        val unit = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_UNIT_TAG)!!
        val imageName = intent.getStringExtra(AddGroceryListItemFragment.IMAGE_TEMP_NAME)!!
        val groceryListUniqueId = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)!!


            val browseItemImageFragment = BrowseItemImageFragment.newInstance(groceryListUniqueId,itemName,unit,price,imageName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,browseItemImageFragment)
                .commit()


    }


}