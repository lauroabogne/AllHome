package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.PantryItemEntity
import com.example.allhome.data.entities.PantryItemWithExpirations

@Dao
interface PantryItemDAO {

    @Insert
    suspend fun addItem(pantryItemEntity: PantryItemEntity):Long
    @Query("SELECT * FROM pantry_items WHERE storage =:storage")
    suspend fun getPantryItemsByStorage(storage:String):List<PantryItemEntity>
}