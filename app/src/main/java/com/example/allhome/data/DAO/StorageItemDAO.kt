package com.example.allhome.data.DAO

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemEntityValues

@Dao
interface StorageItemDAO {

    @Insert
    suspend fun addItem(storageItemEntity: StorageItemEntity):Long
    @Query("SELECT * FROM storage_items WHERE storage =:storage AND deleted =:deletedStatus")
    suspend fun getPantryItemsByStorage(storage:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE unique_id=:uniqueId AND name=:storageItemName")
    suspend fun getItem(uniqueId:String, storageItemName:String):StorageItemEntity
    @Query("UPDATE storage_items SET name=:name,quantity=:quantity,unit=:unit,category=:category,stock_weight=:stockWeight,storage=:storage,notes=:notes,image_name=:imageName,modified=:modified WHERE unique_id=:uniqueId")
    suspend fun updateItem(name:String,quantity:Double,unit:String,category:String,stockWeight:Int,storage:String,notes:String,imageName:String,modified:String,uniqueId: String):Int
    @Query("UPDATE storage_items SET deleted=:deletedStatus,modified=:dateTimeModified WHERE unique_id=:uniqueId")
    suspend fun updateItemAsDeleted(deletedStatus:Int,dateTimeModified:String,uniqueId: String):Int

    @Query("SELECT COUNT(*) FROM storage_items WHERE deleted =:deletedStatus AND storage =:storage")
    suspend fun getStorageItemCount(deletedStatus:Int,storage:String):Int
}