package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryItemEntityForAutoSuggest

@Dao
interface GroceryItemDAO {
    @Insert
     fun insert(groceryItemEntity: GroceryItemEntity): Long

    @Query("SELECT * FROM grocery_items ORDER BY unique_id ASC")
    fun readAllGroceryItem(): LiveData<List<GroceryItemEntity>>

    @Query("SELECT COUNT(*) from grocery_items WHERE grocery_items.grocery_list_unique_id = :groceryListUniqueId ")
    fun getCount(groceryListUniqueId: String): Int

    @Query("SELECT * from grocery_items WHERE grocery_list_unique_id = :groceryListUniqueId AND item_status=:status")
    fun getGroceryListItems(groceryListUniqueId:String,status:Int): List<GroceryItemEntity>

    @Query("SELECT * from grocery_items WHERE grocery_list_unique_id = :groceryListUniqueId ORDER BY unique_id DESC LIMIT 1")
    fun getGroceryListItem(groceryListUniqueId:String):GroceryItemEntity

    @Query("SELECT * from grocery_items WHERE unique_id=:id AND grocery_list_unique_id = :groceryListUniqueId LIMIT 1")
    fun getGroceryListItemLiveData(id:Int, groceryListUniqueId:String):LiveData<GroceryItemEntity>

    @Query("SELECT * from grocery_items WHERE unique_id=:id AND grocery_list_unique_id = :groceryListUniqueId LIMIT 1")
    fun getGroceryListItem(id:String,groceryListUniqueId:String):GroceryItemEntity

    @Query("DELETE FROM grocery_items WHERE unique_id=:id and grocery_list_unique_id = :groceryListUniqueId ")
    fun deleteGroceryItem(id:Int,groceryListUniqueId: String)

    @Query("UPDATE grocery_items SET item_status=:itemStatus WHERE unique_id=:id AND grocery_list_unique_id =:groceryListUniqueId ")
    fun updateGroceryItemAsDeleted(id:String,groceryListUniqueId: String,itemStatus:Int)

    @Query("UPDATE grocery_items SET item_name=:itemName,quantity=:quantity,unit=:unit,price_per_unit=:pricePerUnit,category=:category,notes=:notes,image_name=:imageName, datetime_modified= :datetimeModified WHERE unique_id=:id")
    fun updateItem(itemName:String,quantity:Double,unit:String,pricePerUnit:Double,category:String,notes:String,imageName:String,id:String,datetimeModified:String):Int
    @Query("UPDATE grocery_items SET datetime_modified= :datetimeModified WHERE unique_id=:id")
    fun updateItemQuantityDatetimeModified(id:String,datetimeModified:String):Int

    @Query("UPDATE grocery_items SET bought = :bought,datetime_modified=:datetimeModified WHERE unique_id=:id AND item_name = :itemName")
    fun updateItem(bought:Int,id:String,itemName: String,datetimeModified:String):Int
    @Query("SELECT item_name from grocery_items WHERE item_name LIKE '%'||:itemName||'%' ORDER BY item_name")
    fun getItems(itemName:String):List<String>

    @Query("SELECT * from grocery_items WHERE item_name LIKE '%'||:itemName||'%' GROUP BY item_name ORDER BY item_name")
    fun getGroceryItemEntities(itemName:String):List<GroceryItemEntity>

    @Query("SELECT *,MAX(datetime_modified),(SELECT COUNT(*)  from grocery_items as gi WHERE gi.item_name = grocery_items.item_name  AND gi.grocery_list_unique_id = :groceryListUniqueId AND item_status = 0) AS itemInListCount from grocery_items INNER JOIN grocery_lists ON grocery_items.grocery_list_unique_id = grocery_lists.auto_generated_unique_id WHERE item_name LIKE '%'||:itemName||'%' AND grocery_lists.item_status IN (0,1) AND grocery_items.item_status = 0  GROUP BY item_name ORDER BY datetime_modified ASC")
    fun getGroceryItemEntitiesForAutoSuggest(itemName:String,groceryListUniqueId:String):List<GroceryItemEntityForAutoSuggest>

    @Query("SELECT unit from grocery_items WHERE unit LIKE '%'||:searchTerm||'%' GROUP BY unit ORDER BY unit")
    fun getGroceryItemEntityUnits(searchTerm:String):List<String>
    @Query("SELECT category from grocery_items WHERE category LIKE '%'||:searchTerm||'%' GROUP BY category ORDER BY category")
    fun getGroceryItemEntityCategories(searchTerm:String):List<String>
    @Query("INSERT INTO grocery_items (unique_id, grocery_list_unique_id,sequence,item_name,quantity,unit,price_per_unit,category,notes,image_name,bought) SELECT :newGroceryListUniqueId || rowId,:newGroceryListUniqueId,sequence,item_name,quantity,unit,price_per_unit,category,notes,image_name,0 FROM grocery_items WHERE grocery_list_unique_id= :oldGroceryListUniqueId")
    fun copy(oldGroceryListUniqueId:String,newGroceryListUniqueId:String)
    @Query("SELECT * from grocery_items WHERE item_name =:itemName AND unit =:unit AND grocery_list_unique_id =:groceryListUniqueId AND  item_status = 0 LIMIT 1")
    fun getItemByGroceryListUniqueIdNameAndUnit(groceryListUniqueId:String, itemName:String, unit:String):GroceryItemEntity
    @Query("SELECT * from grocery_items WHERE item_name =:itemName AND unit =:unit AND  item_status = 0 LIMIT 1")
    fun getItemByNameAndUnit(itemName:String, unit:String):GroceryItemEntity
    @Query("SELECT * from grocery_items WHERE grocery_list_unique_id =:groceryListUniqueId AND  item_name =:itemName AND unit =:unit AND  item_status = :status LIMIT 1")
    fun getItemByNameAndUnitWithStatus(groceryListUniqueId:String,itemName:String, unit:String,status:Int):GroceryItemEntity
    @Query("SELECT * from grocery_items WHERE item_name =:itemName AND grocery_list_unique_id =:autoGenerateId AND item_status = 0 LIMIT 1")
    fun getItemByGroceryListAutoGeneratedIDnameAndUnit(autoGenerateId:String,itemName:String):GroceryItemEntity

    @Query("SELECT * from grocery_items WHERE grocery_list_unique_id = :groceryListUniqueId AND item_status= 0 AND bought = 1")
    fun getBoughtGroceryListItems(groceryListUniqueId:String): List<GroceryItemEntity>
    @Query("SELECT price_per_unit from grocery_items WHERE item_name = :itemName AND unit=:unit AND item_status= 0")
    fun getLatestPrice(itemName:String,unit:String):Double



}