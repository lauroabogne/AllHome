package com.example.allhome.storage.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageItemWithExpirations
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class StorageViewModel: ViewModel() {

    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("PantryStorageViewModel"))
    lateinit var storageItemWithExpirations:ArrayList<StorageItemWithExpirations>

    suspend fun getPatryItemWithExpirations(context: Context,storage:String):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorage(storage)
        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }
}