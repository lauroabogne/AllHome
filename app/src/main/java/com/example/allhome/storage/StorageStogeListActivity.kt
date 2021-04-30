package com.example.allhome.storage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemWithExpirations

class StorageStogeListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_stoge_list)
        title = "Select storage"

        val storageEntity = intent.getParcelableExtra<StorageEntity>(StorageFragment.STORAGE_ENTITY_TAG)
        val storageItemWithExpiration =  intent.getParcelableExtra<StorageItemWithExpirations>(StorageFragment.STORAGE_ITEM_ENTITY_TAG)



        val bundle = Bundle()
        bundle.putInt(StorageFragment.ACTION_TAG,StorageFragment.STORAGE_TRASFERING_ITEM_ACTION)
        bundle.putParcelable(StorageFragment.STORAGE_ITEM_ENTITY_TAG,storageItemWithExpiration)
        bundle.putParcelable(StorageFragment.STORAGE_ENTITY_TAG,storageEntity)

        val storageFragment = StorageFragment()
        storageFragment.arguments = bundle

        fragmentProcessor(storageFragment)

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