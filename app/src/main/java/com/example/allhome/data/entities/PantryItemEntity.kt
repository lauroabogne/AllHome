package com.example.allhome.data.entities

import androidx.room.*

@Entity(tableName = "pantry_items")
data class PantryItemEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "quantity") var quantity:Double,
    @ColumnInfo(name = "unit") var unit:String,
    @ColumnInfo(name="stock_weight") var stockWeight:Int,
    @ColumnInfo(name="category") var category:String,
    @ColumnInfo(name="storage") var storage:String,
    @ColumnInfo(name = "notes") var notes:String,
    @ColumnInfo(name = "image_name") var imageName:String,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String


)

data class PantryItemWithExpirations(
    var pantryItemEntity: PantryItemEntity,
    var expirations:List<PantryItemExpirationEntity> = arrayListOf()
)

class PantryItemEntityValues{
    companion object{
        val NO_QUANTITY_INPUT = -1
        val NO_STOCK_WEIGHT_INPUT = -1
        val NO_STOCK = 0
        val LOW_STOCK = 1
        val HIGH_STOCK = 2

        val NO_STOCK_WEIGHT_INPUT_STRING = ""
        val NO_STOCK_STRING = "No stock"
        val LOW_STOCK_STRING = "Low"
        val HIGH_STOCK_STRING = "High"
    }
}