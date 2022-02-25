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

//        val itemName = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_NAME_TAG)!!
//        val price = intent.getDoubleExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_PRICE_TAG,0.0)
//        val unit = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_UNIT_TAG)!!
//        val imageName = intent.getStringExtra(AddGroceryListItemFragment.IMAGE_TEMP_NAME)!!




        val itemName = intent.getStringExtra( AddGroceryListItemFragment.GROCERY_LIST_ITEM_NAME_TAG)!!
        val unit = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_UNIT_TAG)!!
        val price = intent.getDoubleExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_PRICE_TAG,0.0)
        val quantity = intent.getDoubleExtra(AddGroceryListItemFragment.ITEM_QUANTITY_TAG,0.0)
        val category = intent.getStringExtra(AddGroceryListItemFragment.ITEM_CATEGORY)!!
        val note = intent.getStringExtra(AddGroceryListItemFragment.ITEM_NOTES)!!
        val imageName = intent.getStringExtra(AddGroceryListItemFragment.IMAGE_TEMP_NAME)!!
        val groceryListUniqueId = intent.getStringExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG)!!



//        val browseItemActivity = Intent(this,BrowserItemImageActivity::class.java)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_NAME_TAG,itemName)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_PRICE_TAG,price)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.GROCERY_LIST_ITEM_UNIT_TAG,itemUnit)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.IMAGE_TEMP_NAME,tempImageName)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.ITEM_QUANTITY_TAG,quantity)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.ITEM_CATEGORY,category)
//        browseItemActivity.putExtra(AddGroceryListItemFragment.ITEM_NOTES,note)






        val browseItemImageFragment = BrowseItemImageFragment. newInstance(groceryListUniqueId,itemName,unit,price,quantity,category,note,imageName)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer,browseItemImageFragment)
                .commit()


    }


}