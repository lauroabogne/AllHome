package com.example.allhome.data.DAO

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.StorageEntity
import com.example.allhome.data.entities.StorageItemEntity

@Dao
interface StorageDAO {

    @Insert
    suspend fun addItem(storageEntity: StorageEntity):Long

    @Query("SELECT * FROM storage")
    suspend fun getStorages():List<StorageEntity>
    @Query("SELECT * FROM storage WHERE unique_id NOT IN (:storageUniqueId)")
    suspend fun getAllStorageExceptSome(storageUniqueId: List<String>):List<StorageEntity>
    @Query("SELECT * FROM storage WHERE unique_id =:storageUniqueId LIMIT 1")
    suspend fun getStorage(storageUniqueId:String):StorageEntity
    @Query("UPDATE storage SET name=:name,description=:description,image_name=:imageName,modified=:modified WHERE unique_id=:storageUniqueId")
    suspend fun updateStorage(storageUniqueId:String,name:String,description:String,imageName:String,modified:String):Int

}