package com.example.allhome.data.DAO

import android.database.Cursor
import androidx.room.*
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.StorageItemAutoSuggest
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemEntityValues

@Dao
interface StorageItemDAO {

    @Insert
     fun addItem(storageItemEntity: StorageItemEntity):Long
    @Update
     fun update(storageItemEntity: StorageItemEntity):Int
    @Query("SELECT * FROM storage_items WHERE storage_unique_id =:storageUniqueId AND item_status =:deletedStatus ORDER BY name")
     fun getPantryItemsByStorage(storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemName||'%' AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus  ORDER BY name")
     fun getPantryItemsByStorageFilterByName(itemName:String,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE name =:itemName AND unit = :itemUnit AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus  ORDER BY name LIMIT 1")
     fun getSingleStorageItemsByStorageFilterByName(itemName:String,itemUnit:String,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE storage_unique_id =:storageUniqueId AND stock_weight IN(:stockWeight) AND item_status =:deletedStatus  ORDER BY name")
     fun getStorageItemsByStorageFilterByStockWeight(stockWeight:List<Int>,storageUniqueId: String,deletedStatus: Int):List<StorageItemEntity>
    @Query("SELECT * FROM storage_items WHERE  name  LIKE '%'||:itemName||'%' AND storage_unique_id =:storageUniqueId AND stock_weight IN(:stockWeight) AND item_status =:deletedStatus  ORDER BY name")
     fun getStorageItemsByStorageFilterByStockWeightFilterByName(itemName: String,stockWeight:List<Int>,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "    SUM(CASE " +
            "    WHEN quantity < 0 THEN 0 ELSE quantity " +
            "    END) AS quantity " +
            "    FROM storage_items" +
            "   WHERE name  LIKE '%'||:itemNameSearchTerm||'%' " +
            "   AND  item_status = :deletedStatus " +
            "   GROUP BY name,unit  ORDER BY name")
     fun getStorageItemsWithTotalQuantity(itemNameSearchTerm:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND quantity < :quantity AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus  ORDER BY name")
     fun getStorageItemsByStorageLessThan(itemNameSearchTerm:String,storageUniqueId:String,quantity:Int,deletedStatus: Int):List<StorageItemEntity>
    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND quantity > :quantity AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus  ORDER BY name")
     fun getStorageItemsByStorageGreaterThan(itemNameSearchTerm:String,storageUniqueId:String,quantity:Int,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "              SUM(CASE " +
            "                WHEN quantity < 0 THEN 0 ELSE quantity " +
            "               END) AS quantity," +
            "               SUM(CASE " +
            "                 WHEN quantity < 0 THEN 0 ELSE quantity " +
            "                END) AS quantityForCondition " +
            "                FROM storage_items" +
            "               WHERE name  LIKE '%'||:itemNameSearchTerm||'%' " +
            "               AND  item_status = :deletedStatus" +
            "               GROUP BY name,unit " +
            "               HAVING quantityForCondition > :quantity" +
            "               ORDER BY name")
     fun getAllItemsByStorageGreaterThan(itemNameSearchTerm:String,quantity:Int,deletedStatus: Int):List<StorageItemEntity>
    @Query("SELECT storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "              SUM(CASE " +
            "                WHEN quantity < 0 THEN 0 ELSE quantity " +
            "               END) AS quantity," +
            "               SUM(CASE " +
            "                 WHEN quantity < 0 THEN 0 ELSE quantity " +
            "                END) AS quantityForCondition " +
            "                FROM storage_items" +
            "               WHERE name  LIKE '%'||:itemNameSearchTerm||'%' " +
            "               AND  item_status = :deletedStatus" +
            "               GROUP BY name,unit " +
            "               HAVING quantityForCondition < :quantity" +
            "               ORDER BY name")
     fun getAllItemsByStorageLessThan(itemNameSearchTerm:String,quantity:Int,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "              SUM(CASE " +
            "                WHEN quantity < 0 THEN 0 ELSE quantity " +
            "               END) AS quantity," +
            "               SUM(CASE " +
            "                 WHEN quantity < 0 THEN 0 ELSE quantity " +
            "                END) AS quantityForCondition " +
            "                FROM storage_items" +
            "               WHERE name  LIKE '%'||:itemNameSearchTerm||'%' " +
            "               AND  item_status = :deletedStatus" +
            "               GROUP BY name,unit " +
            "               HAVING quantityForCondition BETWEEN :quantityFrom AND :quantityTo" +
            "               ORDER BY name")
     fun getAllItemsByStorageStockBetween(itemNameSearchTerm:String,quantityFrom:Int,quantityTo:Int,deletedStatus: Int):List<StorageItemEntity>


    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND DATE(modified) = DATE(:dateModified) AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus  ORDER BY name")
     fun getStorageItemsByStorageFilterByModified(dateModified: String,itemNameSearchTerm:String,storageUniqueId:String,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT  storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "      SUM(CASE " +
            "       WHEN quantity < 0 THEN 0 ELSE quantity " +
            "       END) AS quantity" +
            " FROM storage_items " +
            " WHERE name  LIKE '%'||:itemNameSearchTerm||'%' " +
            " AND DATE(modified) = DATE(:dateModified) " +
            " AND item_status =:deletedStatus" +
            " GROUP BY name,unit " +
            " ORDER BY name")
     fun getAllItemsByStorageFilterByModified(dateModified: String,itemNameSearchTerm:String,deletedStatus: Int):List<StorageItemEntity>


    @Query("SELECT * FROM storage_items WHERE name  LIKE '%'||:itemNameSearchTerm||'%' AND quantity BETWEEN :fromQuantity AND :toQuantity AND storage_unique_id =:storageUniqueId AND item_status =:deletedStatus  ORDER BY name")
     fun getStorageItemsByStorageQuantityBetween(itemNameSearchTerm:String,storageUniqueId:String,fromQuantity:Int,toQuantity:Int,deletedStatus: Int):List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE unique_id=:uniqueId AND name=:storageItemName AND item_status =0 LIMIT 1")
     fun getItem(uniqueId:String, storageItemName:String):StorageItemEntity
    @Query("SELECT * FROM storage_items WHERE name=:name AND unit=:unit AND storage =:storage  AND item_status =0 LIMIT 1")
     fun getItemByNameAndUnitAndStorage(name:String,unit:String,storage:String):StorageItemEntity?

    @Query("SELECT * FROM storage_items WHERE name=:name AND unit=:unit AND storage_unique_id =:storageUniqueId  AND item_status =0 LIMIT 1")
     fun getItemByNameAndUnitAndStorageUniqueId(name:String,unit:String,storageUniqueId:String):StorageItemEntity?

    @Query("UPDATE storage_items SET name=:name,quantity=:quantity,unit=:unit,category=:category,stock_weight=:stockWeight,storage=:storage,notes=:notes,image_name=:imageName,modified=:modified WHERE unique_id=:uniqueId ")
     fun updateItem(name:String,quantity:Double,unit:String,category:String,stockWeight:Int,storage:String,notes:String,imageName:String,modified:String,uniqueId: String):Int
    @Query("UPDATE storage_items SET item_status=:deletedStatus,modified=:dateTimeModified WHERE unique_id=:uniqueId")
     fun updateItemAsDeleted(deletedStatus:Int,dateTimeModified:String,uniqueId: String):Int
    @Query("UPDATE storage_items SET item_status=${StorageItemEntityValues.DELETED_STATUS},modified=:dateTimeModified WHERE storage_unique_id =:storageUniqueId  ")
    fun updateItemsAsDeleted(dateTimeModified:String,storageUniqueId: String):Int

    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId")
     fun getStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId AND stock_weight=0")
     fun getNoStockStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId AND stock_weight=1")
     fun getLowStockStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT COUNT(*) FROM storage_items WHERE item_status =:deletedStatus AND storage_unique_id =:storageUniqueId AND stock_weight=2")
     fun getHighStockStorageItemCount(deletedStatus:Int,storageUniqueId:String):Int
    @Query("SELECT julianday(DATE(storage_item_expirations.expiration_date)) - julianday(:currentDate)  FROM storage_items " +
            " LEFT JOIN storage_item_expirations" +
            " ON storage_items.unique_id = storage_item_expirations.storage_item_unique_id AND storage_items.modified = storage_item_expirations.created\n" +
            " WHERE storage_items.storage_unique_id = :storageUniqueId" +
            " AND storage_items.item_status = 0 " +
            " AND DATE(storage_item_expirations.expiration_date) >DATE('now') "+
            " AND julianday(DATE(storage_item_expirations.expiration_date)) - julianday(:currentDate) <=31 " +
            " ORDER BY storage_item_expirations.expiration_date ASC")
     fun getItemThatExpireSoon(storageUniqueId:String,currentDate:String):String
    @Query("SELECT COUNT(DISTINCT(storage_item_unique_id))  FROM storage_items " +
            " LEFT JOIN storage_item_expirations" +
            " ON storage_items.unique_id = storage_item_expirations.storage_item_unique_id " +
            " AND storage_items.modified = storage_item_expirations.created" +
            " AND  storage_items.storage = storage_item_expirations.storage" +
            " WHERE storage_items.storage_unique_id = :storageUniqueId" +
            " AND storage_items.item_status = 0 " +
            " AND DATE(storage_item_expirations.expiration_date) <=DATE(:currentDate) "+
            " ORDER BY storage_item_expirations.expiration_date ASC")
     fun getItemCountThatExpired(storageUniqueId:String,currentDate:String):Int
    @Query("SELECT name as itemName,unit, image_name as imageName FROM storage_items " +
            " WHERE storage_items.storage_unique_id = :storageUniqueId" +
            " AND item_status = 0 "+
            " AND storage_items.unique_id " +
            " IN (" +
            "    SELECT storage_item_unique_id FROM storage_item_expirations" +
            "    WHERE  storage = storage_items.storage " +
            "    AND created = storage_items.modified" +
            "    AND DATE(expiration_date) <= DATE(:currentDate)" +
            ")" +
            "  ORDER BY name ")
     fun getExpiredItems(storageUniqueId:String,currentDate:String):List<SimpleGroceryLisItem>

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
            ")" +
            "  ORDER BY name")
     fun getStorageItemFilterByExpiredItems(itemNameSearchTerm:String,storageUniqueId:String,currentDate:String):List<StorageItemEntity>

    @Query("SELECT name as itemName, unit, image_name as imageName FROM storage_items" +
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
            "))" +
            "  ORDER BY name")
     fun getExpiredItemsWithStockWeight(stockWeight:List<Int>,storageUniqueId:String,currentDate:String):List<SimpleGroceryLisItem>
    @Query("SELECT name as itemName, unit, image_name as imageName FROM storage_items" +
            " WHERE " +
            " ( stock_weight IN (:stockWeight) AND storage_items.storage_unique_id = :storageUniqueId AND item_status= 0) "+
            "  ORDER BY name")
    fun getItemsWithStockWeight(stockWeight:List<Int>,storageUniqueId:String):List<SimpleGroceryLisItem>
    @Query("SELECT storage_unique_id, unique_id,name,unit,stock_weight,category,storage,notes,image_name,item_status,created,modified," +
            "  SUM(CASE" +
            "       WHEN quantity < 0 THEN 0 ELSE quantity " +
            "     END) AS quantity " +
            " FROM storage_items " +
            " WHERE name  LIKE '%'||:itemNameSearchTerm||'%' " +
            " AND item_status = 0 " +
            " AND storage_items.unique_id " +
            " IN ( " +
            " SELECT storage_item_unique_id FROM storage_item_expirations " +
            " WHERE  storage = storage_items.storage " +
            " AND created = storage_items.modified " +
            " AND DATE(expiration_date) <= DATE(:currentDate) " +
            " ) " +
            " GROUP BY name,unit " +
            " ORDER BY name")
     fun getAllItemFilterByExpiredItems(itemNameSearchTerm:String,currentDate:String):List<StorageItemEntity>

    @Query("SELECT *" +
            "FROM (" +
            " SELECT item_name as itemName,unit,category,image_name as imageName ,'0' as existInStorage FROM grocery_items GROUP BY item_name,unit " +
            " UNION ALL" +
            " SELECT name as itemName ,unit,category,image_name as imageName,'1' as existInStorage FROM storage_items WHERE item_status = 0 GROUP BY name,unit" +
            " )" +
            " WHERE itemName LIKE '%'||:itemNameSearchTerm||'%' GROUP BY itemName,unit ORDER BY itemName")
     fun getStorageAndGroceryItemForAutosuggest(itemNameSearchTerm:String):List<StorageItemAutoSuggest>
    @Query(" SELECT unit " +
            " FROM ( " +
            " SELECT unit FROM storage_items WHERE item_status = 0 " +
            " UNION ALL " +
            " SELECT unit FROM grocery_items WHERE item_status = 0 " +
            " ) " +
            " WHERE TRIM(unit) <> ''  AND unit LIKE '%'||:unitSearchTerm||'%'" +
            " GROUP BY unit ORDER BY unit ")
     fun getStorageAndGroceryItemUnitForAutousuggest(unitSearchTerm:String):List<String>


    @Query(" SELECT category " +
            " FROM ( " +
            " SELECT category FROM storage_items WHERE item_status = 0 " +
            " UNION ALL " +
            " SELECT category FROM grocery_items WHERE item_status = 0 " +
            " ) " +
            " WHERE TRIM(category) <> ''  AND category LIKE '%'||:categorySearchTerm||'%'" +
            " GROUP BY category ORDER BY category ")
     fun getStorageAndGroceryItemCategoryForAutousuggest(categorySearchTerm:String):List<String>

    data class SimpleGroceryLisItem(val itemName:String,val unit:String,val imageName:String)


}