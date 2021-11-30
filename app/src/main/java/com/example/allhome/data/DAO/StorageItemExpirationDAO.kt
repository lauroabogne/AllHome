package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.StorageItemExpirationEntity

@Dao
interface StorageItemExpirationDAO {
    @Insert
     fun addItem(storageItemExpirationEntity: StorageItemExpirationEntity):Long
    @Query("SELECT * FROM storage_item_expirations WHERE item_name =:storageItemMame AND created=:created ORDER BY expiration_date ASC")
     fun getPantryItemsByStorage(storageItemMame:String,created:String):List<StorageItemExpirationEntity>


    @Query("SELECT * FROM storage_item_expirations " +
            " WHERE created IN( " +
            " SELECT modified FROM storage_items WHERE name =:storageItemname AND unit=:unit AND item_status = 0)" +
            "  ORDER BY expiration_date ASC")
     fun getStorageItemsExpirationByStorage(storageItemname:String,unit:String):List<StorageItemExpirationEntity>

    @Query("SELECT * FROM storage_item_expirations WHERE storage_item_unique_id=:storageUniqueId AND  item_name =:storageItemMame AND created=:created ORDER BY expiration_date ASC")
     fun getStorageItemsExpiratinsByStorageUniquedIdItemNameAndCreated(storageUniqueId:String,storageItemMame:String,created:String):List<StorageItemExpirationEntity>

    @Query("SELECT * FROM storage_item_expirations WHERE storage_item_unique_id=:storageUniqueId AND  storage_item_unique_id =:storageItemUniqueId AND created=:created ORDER BY expiration_date ASC")
     fun getStorageItemsExpiratinsByStorageUniquedIdItemUniqueIdAndCreated(storageUniqueId:String,storageItemUniqueId:String,created:String):List<StorageItemExpirationEntity>

    @Query("SELECT * FROM storage_item_expirations WHERE item_name =:itemName AND storage=:storage AND created=:created")
     fun getByItemNameStorageAndCreated(itemName:String,storage:String,created:String):List<StorageItemExpirationEntity>
    @Query("UPDATE storage_item_expirations SET deleted=:deletedStatus,modified =:modifiedDateTime WHERE storage_item_unique_id=:uniqueId AND storage =:storage")
     fun updateItemAsDeleted(deletedStatus:Int,modifiedDateTime:String,uniqueId:String,storage:String):Int
}