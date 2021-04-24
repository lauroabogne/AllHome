package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.StorageItemExpirationEntity

@Dao
interface StorageItemExpirationDAO {
    @Insert
    suspend fun addItem(storageItemExpirationEntity: StorageItemExpirationEntity):Long
    @Query("SELECT * FROM storage_item_expirations WHERE item_name =:storageItemMame AND created=:created")
    suspend fun getPantryItemsByStorage(storageItemMame:String,created:String):List<StorageItemExpirationEntity>

    @Query("SELECT * FROM storage_item_expirations WHERE item_name =:itemName AND storage=:storage AND created=:created")
    suspend fun getByItemNameStorageAndCreated(itemName:String,storage:String,created:String):List<StorageItemExpirationEntity>
    @Query("UPDATE storage_item_expirations SET deleted=:deletedStatus,modified =:modifiedDateTime WHERE storage_item_unique_id=:uniqueId AND storage =:storage")
    suspend fun updateItemAsDeleted(deletedStatus:Int,modifiedDateTime:String,uniqueId:String,storage:String):Int
}