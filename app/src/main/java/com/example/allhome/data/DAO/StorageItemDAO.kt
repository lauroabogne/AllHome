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
    @Query("SELECT * FROM storage_items WHERE storage_unique_id =:storageUniqueId AND item_status =:deletedStatus")
    suspend fun getPantryItemsByStorage(storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemName||'%' AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus")
    suspend fun getPantryItemsByStorageFilterByName(itemName:String,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE storage_unique_id =:storageUniqueId AND stock_weight IN(:stockWeight) AND item_status =:deletedStatus")
    suspend fun getStorageItemsByStorageFilterByStockWeight(stockWeight:List<Int>,storageUniqueId: String,deletedStatus: Int):List<StorageItemEntity>
    @Query("SELECT * FROM storage_items WHERE  name  LIKE '%'||:itemName||'%' AND storage_unique_id =:storageUniqueId AND stock_weight IN(:stockWeight) AND item_status =:deletedStatus")
    suspend fun getStorageItemsByStorageFilterByStockWeightFilterByName(itemName: String,stockWeight:List<Int>,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "    SUM(CASE " +
            "    WHEN quantity < 0 THEN 0 ELSE quantity " +
            "    END) AS quantity " +
            "    FROM storage_items WHERE item_status = :deletedStatus " +
            "    GROUP BY name,unit")
    suspend fun getStorageItemsWithTotalQuantity(deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND quantity < :quantity AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus")
    suspend fun getStorageItemsByStorageLessThan(itemNameSearchTerm:String,storageUniqueId:String,quantity:Int,deletedStatus: Int):List<StorageItemEntity>
    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND quantity > :quantity AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus")
    suspend fun getStorageItemsByStorageGreaterThan(itemNameSearchTerm:String,storageUniqueId:String,quantity:Int,deletedStatus: Int):List<StorageItemEntity>


    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND DATE(modified) = DATE(:dateModified) AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus")
    suspend fun getStorageItemsByStorageFilterByModified(dateModified: String,itemNameSearchTerm:String,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>



    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND quantity BETWEEN :fromQuantity AND :toQuantity AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus")
    suspend fun getStorageItemsByStorageQuantityBetween(itemNameSearchTerm:String,storageUniqueId:String,fromQuantity:Int,toQuantity:Int,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE unique_id=:uniqueId AND name=:storageItemName AND item_status =0 LIMIT 1")
    suspend fun getItem(uniqueId:String, storageItemName:String):StorageItemEntity
    @Query("SELECT * FROM storage_items WHERE name=:name AND unit=:unit AND storage =:storage  AND item_status =0 LIMIT 1")
    suspend fun getItemByNameAndUnitAndStorage(name:String,unit:String,storage:String):StorageItemEntity?

    @Query("UPDATE storage_items SET name=:name,quantity=:quantity,unit=:unit,category=:category,stock_weight=:stockWeight,storage=:storage,notes=:notes,image_name=:imageName,modified=:modified WHERE unique_id=:uniqueId")
    suspend fun updateItem(name:String,quantity:Double,unit:String,category:String,stockWeight:Int,storage:String,notes:String,imageName:String,modified:String,uniqueId: String):Int
    @Query("UPDATE storage_items SET item_status=:deletedStatus,modified=:dateTimeModified WHERE unique_id=:uniqueId")
    suspend fun updateItemAsDeleted(deletedStatus:Int,dateTimeModified:String,uniqueId: String):Int

    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId")
    suspend fun getStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId AND stock_weight=0")
    suspend fun getNoStockStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId AND stock_weight=1")
    suspend fun getLowStockStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId AND stock_weight=2")
    suspend fun getHighStockStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT julianday(DATE(storage_item_expirations.expiration_date)) - julianday(:currentDate)  FROM storage_items " +
            " LEFT JOIN storage_item_expirations" +
            " ON storage_items.unique_id = storage_item_expirations.storage_item_unique_id AND storage_items.modified = storage_item_expirations.created\n" +
            " WHERE storage_items.storage_unique_id = :storageUniqueId" +
            " AND storage_items.item_status = 0 " +
            " AND DATE(storage_item_expirations.expiration_date) >DATE('now') "+
            " AND julianday(DATE(storage_item_expirations.expiration_date)) - julianday(:currentDate) <=31 " +
            " ORDER BY storage_item_expirations.expiration_date ASC")
    suspend fun getItemThatExpireSoon(storageUniqueId:String,currentDate:String):String
    @Query("SELECT COUNT(DISTINCT(storage_item_unique_id))  FROM storage_items " +
            " LEFT JOIN storage_item_expirations" +
            " ON storage_items.unique_id = storage_item_expirations.storage_item_unique_id " +
            " AND storage_items.modified = storage_item_expirations.created" +
            " AND  storage_items.storage = storage_item_expirations.storage" +
            " WHERE storage_items.storage_unique_id = :storageUniqueId" +
            " AND storage_items.item_status = 0 " +
            " AND DATE(storage_item_expirations.expiration_date) <=DATE(:currentDate) "+
            " ORDER BY storage_item_expirations.expiration_date ASC")
    suspend fun getItemCountThatExpired(storageUniqueId:String,currentDate:String):Int
    @Query("SELECT name as itemName,unit FROM storage_items " +
            " WHERE storage_items.storage_unique_id = :storageUniqueId" +
            " AND item_status = 0 "+
            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage = storage_items.storage " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            ")")
    suspend fun getExpiredItems(storageUniqueId:String,currentDate:String):List<SimpleGroceryLisItem>

    @Query("SELECT * FROM storage_items " +
            " WHERE " +
            " name  LIKE '%'||:itemNameSearchTerm||'%' "+
            " AND storage_items.storage_unique_id = :storageUniqueId" +
            " AND item_status = 0 "+
            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage = storage_items.storage " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            ")")
    suspend fun getStorageItemFilterByExpiredItems(itemNameSearchTerm:String,storageUniqueId:String,currentDate:String):List<StorageItemEntity>

    @Query("SELECT name as itemName,unit FROM storage_items" +
            " WHERE " +
            "(stock_weight IN (:stockWeight) AND storage_items.storage_unique_id = :storageUniqueId AND item_status= 0)"+
            " OR " +
            " (storage_items.storage_unique_id = :storageUniqueId" +
            " AND item_status = 0 "+

            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage_item_unique_id = storage_items.storage_unique_id " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            "))")
    suspend fun getExpiredItemsWithStockWeight(stockWeight:List<Int>,storageUniqueId:String,currentDate:String):List<SimpleGroceryLisItem>

    @Query("SELECT name as itemName,unit FROM storage_items " +
            " WHERE storage_items.storage = :storage" +
            " AND item_status = 0 "+
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