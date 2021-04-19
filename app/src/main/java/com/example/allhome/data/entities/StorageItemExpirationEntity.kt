package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "storage_item_expirations")
data class StorageItemExpirationEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "pantry_item_name") var pantryItemName:String,
    @ColumnInfo(name = "storage") var storage:String,
    @ColumnInfo(name = "expiration_date") var expirationDate:String,
    @ColumnInfo(name = "created") var created:String
)
