package com.example.allhome.storage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.data.entities.StorageItemEntity

class StorageStogeListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_stoge_list)
        title = "Select storage"

        val storageItemEntity = intent.getParcelableExtra<StorageItemEntity>(StorageFragment.STORAGE_ITEM_ENTITY_TAG)
        val bundle = Bundle()
        bundle.putInt(StorageFragment.ACTION_TAG,StorageFragment.STORAGE_TRASFERING_ITEM_ACTION)
        bundle.putParcelable(StorageFragment.STORAGE_ITEM_ENTITY_TAG,storageItemEntity)

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