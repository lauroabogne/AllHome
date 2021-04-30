package com.example.allhome.data.DAO

import android.database.Cursor
import androidx.room.*
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemEntityValues

@Dao
interface StorageItemDAO {

    @Insert
    suspend fun addItem(storageItemEntity: StorageItemEntity):Long
    @Update
    suspend fun update(storageItemEntity: StorageItemEntity):Int
    @Query("SELECT * FROM storage_items WHERE storage =:storage AND deleted =:deletedStatus")
    suspend fun getPantryItemsByStorage(storage:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE unique_id=:uniqueId AND name=:storageItemName AND deleted =0 LIMIT 1")
    suspend fun getItem(uniqueId:String, storageItemName:String):StorageItemEntity
    @Query("SELECT * FROM storage_items WHERE name=:name AND unit=:unit AND storage =:storage  AND deleted =0 LIMIT 1")
    suspend fun getItemByNameAndUnitAndStorage(name:String,unit:String,storage:String):StorageItemEntity?

    @Query("UPDATE storage_items SET name=:name,quantity=:quantity,unit=:unit,category=:category,stock_weight=:stockWeight,storage=:storage,notes=:notes,image_name=:imageName,modified=:modified WHERE unique_id=:uniqueId")
    suspend fun updateItem(name:String,quantity:Double,unit:String,category:String,stockWeight:Int,storage:String,notes:String,imageName:String,modified:String,uniqueId: String):Int
    @Query("UPDATE storage_items SET deleted=:deletedStatus,modified=:dateTimeModified WHERE unique_id=:uniqueId")
    suspend fun updateItemAsDeleted(deletedStatus:Int,dateTimeModified:String,uniqueId: String):Int

    @Query("SELECT COUNT(*) FROM storage_items WHERE deleted =:deletedStatus AND storage =:storage")
    suspend fun getStorageItemCount(deletedStatus:Int,storage:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE deleted =:deletedStatus AND storage =:storage AND stock_weight=0")
    suspend fun getNoStockStorageItemCount(deletedStatus:Int,storage:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE deleted =:deletedStatus AND storage =:storage AND stock_weight=1")
    suspend fun getLowStockStorageItemCount(deletedStatus:Int,storage:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE deleted =:deletedStatus AND storage =:storage AND stock_weight=2")
    suspend fun getHighStockStorageItemCount(deletedStatus:Int,storage:String):Int
    @Query("SELECT julianday(DATE(storage_item_expirations.expiration_date)) - julianday(:currentDate)  FROM storage_items " +
            " LEFT JOIN storage_item_expirations" +
            " ON storage_items.unique_id = storage_item_expirations.storage_item_unique_id AND storage_items.modified = storage_item_expirations.created\n" +
            " WHERE storage_items.storage = :storageName" +
            " AND storage_items.deleted = 0 " +
            " AND DATE(storage_item_expirations.expiration_date) >DATE('now') "+
            " AND julianday(DATE(storage_item_expirations.expiration_date)) - julianday(:currentDate) <=31 " +
            " ORDER BY storage_item_expirations.expiration_date ASC")
    suspend fun getItemThatExpireSoon(storageName:String,currentDate:String):String

    @Query("SELECT COUNT(DISTINCT(storage_item_unique_id))  FROM storage_items " +
            " LEFT JOIN storage_item_expirations" +
            " ON storage_items.unique_id = storage_item_expirations.storage_item_unique_id " +
            " AND storage_items.modified = storage_item_expirations.created" +
            " AND  storage_items.storage = storage_item_expirations.storage" +
            " WHERE storage_items.storage = :storageName" +
            " AND storage_items.deleted = 0 " +
            " AND DATE(storage_item_expirations.expiration_date) <=DATE(:currentDate) "+
            " ORDER BY storage_item_expirations.expiration_date ASC")
    suspend fun getItemCountThatExpired(storageName:String,currentDate:String):Int
    @Query("SELECT name as itemName,unit FROM storage_items " +
            " WHERE storage_items.storage = :storage" +
            " AND deleted = 0 "+
            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage = storage_items.storage " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            ")")
    suspend fun getExpiredItems(storage:String,currentDate:String):List<SimpleGroceryLisItem>
    @Query("SELECT name as itemName,unit FROM storage_items " +
            " WHERE " +
            "(stock_weight IN (:stockWeight) AND storage_items.storage = :storage AND deleted= 0)"+
            " OR " +
            " (storage_items.storage = :storage" +
            " AND deleted = 0 "+

            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage = storage_items.storage " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            "))")
    suspend fun getExpiredItemsWithStockWeight(stockWeight:List<Int>,storage:String,currentDate:String):List<SimpleGroceryLisItem>

    @Query("SELECT name as itemName,unit FROM storage_items " +
            " WHERE storage_items.storage = :storage" +
            " AND deleted = 0 "+
            " AND stock_weight IN (:stockWeight) "+
            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage = storage_items.storage " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            ")")
    suspend fun getExpiredItemsWithStockWeightTest(stockWeight:List<Int>,storage:String,currentDate:String):List<SimpleGroceryLisItem>

    data class SimpleGroceryLisItem(val itemName:String,val unit:String)


}