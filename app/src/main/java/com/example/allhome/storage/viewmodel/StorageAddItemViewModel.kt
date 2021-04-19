package com.example.allhome.storage.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemExpirationEntity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class StorageAddItemViewModel: ViewModel() {
    val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("PantryAddItemViewModel"))
    var storageItemEntity:StorageItemEntity? = null
    var storageItemExpirationsEntity:ArrayList<StorageItemExpirationEntity> = arrayListOf()

    suspend fun setPantryItemEntity(storageItemEntity:StorageItemEntity){
        this.storageItemEntity = storageItemEntity
    }
    suspend fun addExpiration(storageItemExpirationEntity:StorageItemExpirationEntity){
        storageItemExpirationsEntity.add(storageItemExpirationEntity)
    }

    suspend fun setStorageItemAndExpirations(context:Context, uniqueId:String, name:String){
        storageItemEntity = AllHomeDatabase.getDatabase(context).getStorageItemDAO().getItem(uniqueId,name)

        val storageItemDateTimeModified = storageItemEntity!!.modified
        val itemName = storageItemEntity!!.name
        val storage = storageItemEntity!!.storage

        storageItemExpirationsEntity = AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().getByItemNameStorageAndCreated(itemName,storage,storageItemDateTimeModified) as ArrayList<StorageItemExpirationEntity>


    }
    suspend fun getExpirations(){

    }
    suspend fun saveStorageItemEntity(context: Context, storageItemEntity: StorageItemEntity):Long{
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().addItem(storageItemEntity)
    }
    suspend fun saveStorageItemExpirationEntity(context: Context, storageItemExpirationEntity: StorageItemExpirationEntity):Long{
        return AllHomeDatabase.getDatabase(context).getStorageItemExpirationDAO().addItem(storageItemExpirationEntity)
    }
    suspend fun updateStorageItemEntity(context:Context,name:String,quantity:Double,unit:String,stockWeight:Int,storage:String,notes:String,imageName:String,modifiedDatetime:String,uniqueId: String):Int{
        return AllHomeDatabase.getDatabase(context).getStorageItemDAO().updateItem(name,quantity,unit,stockWeight,storage,notes,imageName,modifiedDatetime,uniqueId)
    }

}