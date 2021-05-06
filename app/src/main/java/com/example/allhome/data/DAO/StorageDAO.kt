package com.example.allhome.data.DAO

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageEntityValues
import com.example.allhome.data.entities.StorageItemEntity

@Dao
interface StorageDAO {

    @Insert
    suspend fun addItem(storageEntity: StorageEntity):Long

    @Query("SELECT * FROM storage where item_status = 0")
    suspend fun getStorages():List<StorageEntity>
    @Query("SELECT * FROM storage WHERE unique_id NOT IN (:storageUniqueId) AND item_status = 0 ")
    suspend fun getAllStorageExceptSome(storageUniqueId: List<String>):List<StorageEntity>
    @Query("SELECT * FROM storage WHERE unique_id =:storageUniqueId AND item_status = 0 LIMIT 1")
    suspend fun getStorage(storageUniqueId:String):StorageEntity
    @Query("UPDATE storage SET name=:name,description=:description,image_name=:imageName,modified=:modified WHERE unique_id=:storageUniqueId AND item_status = 0")
    suspend fun updateStorage(storageUniqueId:String,name:String,description:String,imageName:String,modified:String):Int
    @Query("UPDATE storage SET item_status =:deleteStatus,modified=:currentDateTime WHERE unique_id=:storageUniqueId")
    suspend fun updateStorageAsDeleted(storageUniqueId:String,currentDateTime:String,deleteStatus:Int):Int
    @Query("SELECT * from storage " +
            "WHERE unique_id IN ( " +
            " SELECT storage_unique_id FROM storage_items " +
            " WHERE name = :itemName AND unit=:unit AND item_status = 0" +
            " )" +
            " AND item_status = 0")
    suspend fun getStorages(itemName:String,unit:String):List<StorageEntity>
}