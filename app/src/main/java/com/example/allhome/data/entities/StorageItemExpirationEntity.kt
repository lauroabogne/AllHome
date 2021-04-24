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
    @ColumnInfo(name = "deleted",defaultValue = "0") var deleted:Int,
    @ColumnInfo(name = "expiration_date") var expirationDate:String,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String
)

class StorageItemExpirationEntityValues{
    companion object{

        const val DELETED_STATUS = 0
        const val NOT_DELETED_STATUS = 1


    }
}
