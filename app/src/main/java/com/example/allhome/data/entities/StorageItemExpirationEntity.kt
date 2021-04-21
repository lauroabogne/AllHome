package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "storage_item_expirations")
data class StorageItemExpirationEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "storage_item_unique_id") var storageItemUniqueId:String,
    @ColumnInfo(name = "item_name") var storageItemName:String,
    @ColumnInfo(name = "storage") var storage:String,
    @ColumnInfo(name = "expiration_date") var expirationDate:String,
    @ColumnInfo(name = "created") var created:String
)
