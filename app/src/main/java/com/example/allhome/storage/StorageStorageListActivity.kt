package com.example.allhome.storage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.data.entities.*

class StorageStorageListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_stoge_list)

        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = "Select storage"

        val action = intent.getIntExtra(StorageFragment.ACTION_TAG,StorageFragment.STORAGE_TRASFERING_ITEM_ACTION)

        if(action == StorageFragment.STORAGE_TRASFERING_ITEM_ACTION){

            val storageEntity = intent.getParcelableExtra<StorageEntity>(StorageFragment.STORAGE_ENTITY_TAG)
            val storageItemWithExpiration =  intent.getParcelableExtra<StorageItemWithExpirations>(StorageFragment.STORAGE_ITEM_ENTITY_TAG)


            val bundle = Bundle()
            bundle.putInt(StorageFragment.ACTION_TAG,action)
            bundle.putParcelable(StorageFragment.STORAGE_ITEM_ENTITY_TAG,storageItemWithExpiration)
            bundle.putParcelable(StorageFragment.STORAGE_ENTITY_TAG,storageEntity)

            val storageFragment = StorageFragment()
            storageFragment.arguments = bundle

            fragmentProcessor(storageFragment)

        }else if(action == StorageFragment.STORAGE_ADD_ITEM_FROM_GROCERY_LIST_ACTION){
            val groceryItemEntity = intent.getParcelableExtra<GroceryItemEntity>(StorageFragment.GROCERY_ITEM_ENTITY_TAG)

            val bundle = Bundle()
            bundle.putInt(StorageFragment.ACTION_TAG,action)
            bundle.putParcelable(StorageFragment.GROCERY_ITEM_ENTITY_TAG,groceryItemEntity)

            val storageFragment = StorageFragment()
            storageFragment.arguments = bundle
            fragmentProcessor(storageFragment)

        }else if(action == StorageFragment.STORAGE_ADD_ALL_ITEM_FROM_GROCERY_LIST_ACTION){

            val groceryListEntity = intent.getParcelableExtra<GroceryListEntity>(StorageFragment.GROCERY_ENTITY_TAG)

            val bundle = Bundle()
            bundle.putInt(StorageFragment.ACTION_TAG,action)
            bundle.putParcelable(StorageFragment.GROCERY_ENTITY_TAG,groceryListEntity)

            val storageFragment = StorageFragment()
            storageFragment.arguments = bundle

            fragmentProcessor(storageFragment)

        }



    }
    fun fragmentProcessor(fragment: Fragment){

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.homeFragmentContainer,fragment)
            commit()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

}