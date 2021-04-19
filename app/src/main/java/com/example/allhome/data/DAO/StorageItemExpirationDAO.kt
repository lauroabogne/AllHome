package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.StorageItemExpirationEntity

@Dao
interface StorageItemExpirationDAO {
    @Insert
    suspend fun addItem(storageItemExpirationEntity: StorageItemExpirationEntity):Long
    @Query("SELECT * FROM storage_item_expirations WHERE pantry_item_name =:storageItemMame AND created=:created")
    suspend fun getPantryItemsByStorage(storageItemMame:String,created:String):List<StorageItemExpirationEntity>

    @Query("SELECT * FROM storage_item_expirations WHERE pantry_item_name =:itemName AND storage=:storage AND created=:created")
    suspend fun getByItemNameStorageAndCreated(itemName:String,storage:String,created:String):List<StorageItemExpirationEntity>
}