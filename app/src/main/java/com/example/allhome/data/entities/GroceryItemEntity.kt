package com.example.allhome.data.entities

import androidx.databinding.Bindable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
 data class GroceryItemEntity (
        @ColumnInfo(name = "grocery_list_unique_id") val groceryListUniqueId:String,
        @ColumnInfo(name = "sequence") val sequence:Int = 0,
        @ColumnInfo(name = "item_name") val itemName:String,
        @ColumnInfo(name = "quantity") val quantity:Double,
        @ColumnInfo(name = "unit") val unit:String,
        @ColumnInfo(name = "price_per_unit") val pricePerUnit:Double,
        @ColumnInfo(name="category") val category:String,
        @ColumnInfo(name = "notes") val notes:String,
        @ColumnInfo(name = "image_name") val imageName:String,
        @ColumnInfo(name = "bought",defaultValue = "0") var bought:Int

     ){
    @PrimaryKey(autoGenerate = true) var id:Int  = 0
    @Ignore
    var index:Int = 0
    @Ignore
    var forCategoryDivider = false
}