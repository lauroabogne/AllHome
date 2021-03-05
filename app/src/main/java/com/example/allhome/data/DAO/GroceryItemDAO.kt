package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.GroceryItemEntity

@Dao
interface GroceryItemDAO {
    @Insert
    fun addItem(groceryItemEntity: GroceryItemEntity)

    @Query("SELECT * FROM grocery_items ORDER BY id ASC")
    fun readAllGroceryItem(): LiveData<List<GroceryItemEntity>>

    @Query("SELECT COUNT(*) from grocery_items WHERE grocery_items.grocery_list_unique_id = :groceryListUniqueId ")
    fun getCount(groceryListUniqueId: String): Int

    @Query("SELECT * from grocery_items WHERE grocery_list_unique_id = :groceryListUniqueId")
    fun getGroceryListItems(groceryListUniqueId:String): List<GroceryItemEntity>

    @Query("SELECT * from grocery_items WHERE grocery_list_unique_id = :groceryListUniqueId ORDER BY id DESC LIMIT 1")
    fun getGroceryListItem(groceryListUniqueId:String):GroceryItemEntity

    @Query("SELECT * from grocery_items WHERE id=:id AND grocery_list_unique_id = :groceryListUniqueId LIMIT 1")
    fun getGroceryListItemLiveData(id:Int, groceryListUniqueId:String):LiveData<GroceryItemEntity>

    @Query("SELECT * from grocery_items WHERE id=:id AND grocery_list_unique_id = :groceryListUniqueId LIMIT 1")
    fun getGroceryListItem(id:Int,groceryListUniqueId:String):GroceryItemEntity

    @Query("DELETE FROM grocery_items WHERE id=:id and grocery_list_unique_id = :groceryListUniqueId ")
    fun deleteGroceryItem(id:Int,groceryListUniqueId: String)
    @Query("UPDATE grocery_items SET item_name=:itemName,quantity=:quantity,unit=:unit,price_per_unit=:pricePerUnit,category=:category,notes=:notes,image_name=:imageName WHERE id=:id")
    fun updateItem(itemName:String,quantity:Double,unit:String,pricePerUnit:Double,category:String,notes:String,imageName:String,id:Int):Int
    @Query("UPDATE grocery_items SET bought = :bought WHERE id=:id AND item_name = :itemName")
    fun updateItem(bought:Int,id:Int,itemName: String):Int
    @Query("SELECT item_name from grocery_items WHERE item_name LIKE '%'||:itemName||'%' ORDER BY item_name")
    fun getItems(itemName:String):List<String>

    @Query("SELECT * from grocery_items WHERE item_name LIKE '%'||:itemName||'%' GROUP BY item_name ORDER BY item_name")
    fun getGroceryItemEntities(itemName:String):List<GroceryItemEntity>

    @Query("SELECT unit from grocery_items WHERE unit LIKE '%'||:searchTerm||'%' GROUP BY unit ORDER BY unit")
    fun getGroceryItemEntityUnits(searchTerm:String):List<String>
    @Query("SELECT category from grocery_items WHERE category LIKE '%'||:searchTerm||'%' GROUP BY category ORDER BY category")
    fun getGroceryItemEntityCategories(searchTerm:String):List<String>

}