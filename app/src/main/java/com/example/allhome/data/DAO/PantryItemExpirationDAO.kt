package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.PantryItemEntity
import com.example.allhome.data.entities.PantryItemExpirationEntity

@Dao
interface PantryItemExpirationDAO {
    @Insert
    suspend fun addItem(pantryItemExpirationEntity: PantryItemExpirationEntity):Long
    @Query("SELECT * FROM pantry_item_expirations WHERE pantry_item_name =:pantryItemMame AND created=:created")
    suspend fun getPantryItemsByStorage(pantryItemMame:String,created:String):List<PantryItemExpirationEntity>
}