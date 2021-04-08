package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import com.example.allhome.data.entities.GroceryListEntityValues
import com.example.allhome.data.entities.GroceryListWithItemCount
import com.example.allhome.data.relations.GroceryListAndGroceryItems
import com.example.allhome.data.relations.GroceryListAndGroceryItemsName
import com.example.allhome.data.relations.GroceryListAndGroceryItemsName1

@Dao
interface GroceryListDAO {
    @Insert
    suspend fun addItem(groceryListEntity: GroceryListEntity):Long
    @Query("SELECT * FROM grocery_lists")
    fun readAllGroceryItem(): LiveData<List<GroceryListEntity>>

    @Query("SELECT *,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id ) as grocery_item_count FROM grocery_lists")
    fun readGroceryListWithItem():List<GroceryListAndGroceryItems>

    @Query("SELECT *,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id  AND item_status = 0) AS itemCount,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id AND bought= 1  AND item_status = 0) AS itemBought FROM grocery_lists WHERE item_status =:status ORDER BY datetime_created DESC")
    suspend fun selectGroceryListsWithItemCount(status:Int):List<GroceryListWithItemCount>

    @Query("SELECT *,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id AND item_status = 0 ) AS itemCount,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id AND bought= 1 AND item_status = 0 ) AS itemBought FROM grocery_lists WHERE auto_generated_unique_id =:autogeneratedUniqueId LIMIT 1")
    suspend fun getGroceryListWithItemCount(autogeneratedUniqueId:String):GroceryListWithItemCount

    @Query("DELETE FROM grocery_lists WHERE auto_generated_unique_id = :autoGeneratedUniqueId ")
    suspend fun deleteGroceryList(autoGeneratedUniqueId:String):Int
    @Query("SELECT * FROM grocery_lists WHERE auto_generated_unique_id =:autoGeneratedUniqueId LIMIT 1")
    fun getGroceryList(autoGeneratedUniqueId:String): GroceryListEntity
    @Query("UPDATE grocery_lists SET name=:name, shopping_datetime=:shoppingDatetime,location=:location,longitude=:longitude,latitude=:latitude WHERE auto_generated_unique_id =:autoGeneratedUniqueId")
    fun updateGroceryList(name:String,shoppingDatetime:String,location:String,longitude:Double,latitude:Double, autoGeneratedUniqueId:String)

    @Query("UPDATE grocery_lists SET item_status=:status,datetime_status_updated=:datetimeStatusUpdated WHERE auto_generated_unique_id =:autoGeneratedUniqueId")
    fun updateGroceryListAsDeleted(autoGeneratedUniqueId:String,datetimeStatusUpdated:String,status:Int)

    @Query("UPDATE grocery_lists SET uploaded=:status,datetime_status_updated=:datetimeStatusUpdated WHERE auto_generated_unique_id =:autoGeneratedUniqueId")
    suspend fun updateGroceryListAsNotUploaded(autoGeneratedUniqueId:String,datetimeStatusUpdated:String,status:Int)

    @Update
    fun updateGroceryList(groceryListEntity: GroceryListEntity):Int
    @Query("SELECT 'TEST DATA' FROM grocery_lists limit 1")
    fun select():String
    @Query("INSERT INTO grocery_lists (auto_generated_unique_id,name,datetime_created,shopping_datetime,location,longitude,latitude) SELECT :newGroceryListUniqueId,:newGroceryListName,:datetimeCreated,'0000-00-00 00:00:00',location,longitude,latitude FROM grocery_lists WHERE auto_generated_unique_id = :oldGroceryListUniqueId")
    fun copy(oldGroceryListUniqueId:String,newGroceryListUniqueId:String,newGroceryListName:String,datetimeCreated:String)
    @Query("UPDATE grocery_lists SET viewing_type=:viewing WHERE auto_generated_unique_id =:autoGeneratedUniqueId")
    fun updateGroceryListViewing(viewing:Int,autoGeneratedUniqueId:String)

    @Query("SELECT *,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id  AND item_status = 0) AS itemCount,(SELECT COUNT(*) FROM grocery_items WHERE grocery_list_unique_id =grocery_lists.auto_generated_unique_id AND bought= 1  AND item_status = 0) AS itemBought FROM grocery_lists WHERE item_status =:status ORDER BY datetime_created DESC")
    suspend fun getDeletedGroceryListsWithItemCount(status:Int):List<GroceryListWithItemCount>

}

