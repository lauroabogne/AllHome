package com.example.allhome.storage.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class StorageViewModel: ViewModel() {

    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("PantryStorageViewModel"))
    lateinit var storageItemWithExpirations:ArrayList<StorageItemWithExpirations>
    var storageEntitiesWithExtraInformation:ArrayList<StorageEntityWithExtraInformation> = arrayListOf()


    var storageItemEntity:StorageItemEntity? = null

    suspend fun getPatryItemWithExpirations(context: Context,storage:String):ArrayList<StorageItemWithExpirations>{

        storageItemWithExpirations = arrayListOf()

        val pantryItemEntities = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getPantryItemsByStorage(storage,StorageItemEntityValues.NOT_DELETED_STATUS)
        pantryItemEntities.forEach {
            val itemName = it.name
            val dateModified = it.modified

            val pantryItemExpirationEntities =AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getPantryItemsByStorage(itemName,dateModified)
            storageItemWithExpirations.add(StorageItemWithExpirations(it,pantryItemExpirationEntities))

        }
        return storageItemWithExpirations
    }

    suspend fun updateItemAsDeleted(context:Context,currentDatetime:String, storageItemEntity:StorageItemEntity):Int{
      return AllHomeDatabase.getDatabase(context).getStorageItemDAO().updateItemAsDeleted(StorageItemEntityValues.DELETED_STATUS,currentDatetime,storageItemEntity.uniqueId)
    }
    suspend fun updateStorageExpirationDateAsDeleted(context:Context,currentDatetime:String, storageItemEntity:StorageItemEntity):Int{
        return AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().updateItemAsDeleted(StorageItemEntityValues.DELETED_STATUS,currentDatetime,storageItemEntity.uniqueId,storageItemEntity.storage)

    }
    suspend fun addStorage(context:Context,storageEntity:StorageEntity):Long{
        return AllHomeDatabase.getDatabase(context).getStorageDAO().addItem(storageEntity)
    }
    suspend fun getAllStorage(context:Context):ArrayList<StorageEntityWithExtraInformation>{
        val storageItemDAO = AllHomeDatabase.getDatabase(context).getStorageItemDAO()

        val storageEntities = AllHomeDatabase.getDatabase(context).getStorageDAO().getStorages() as ArrayList<StorageEntity>
        storageEntities.forEach {

            val storageItemCount = storageItemDAO.getStorageItemCount(StorageItemEntityValues.NOT_DELETED_STATUS,it.name)
            Log.e("DATA",it.name+" "+storageItemCount)

            val storageEntityWithExtraInformation = StorageEntityWithExtraInformation(
                storageEntity = it,
                itemCount = storageItemCount,
                noStockItemCount = 0,
                lowStockItemCount = 0,
                highStockItemCount = 0,
                expiredItemCount = 0,
                itemToExpireDayCount = 0
            )

            storageEntitiesWithExtraInformation.add(storageEntityWithExtraInformation)
        }

        Log.e("COUNT",storageEntitiesWithExtraInformation.size.toString())
        return storageEntitiesWithExtraInformation

    }
}