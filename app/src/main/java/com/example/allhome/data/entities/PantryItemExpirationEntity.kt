package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "pantry_item_expirations")
data class PantryItemExpirationEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "pantry_item_name") var pantryItemName:String,
    @ColumnInfo(name = "expiration_date") var expirationDate:String,
    @ColumnInfo(name = "created") var created:String
)
