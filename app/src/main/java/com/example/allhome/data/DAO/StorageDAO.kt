package com.example.allhome.data.DAO

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

}