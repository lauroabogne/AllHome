package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "storage")
data class StorageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "description") var description:String,
    @ColumnInfo(name = "image_name") var imageName:String,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String

)

data class StorageEntityWithExtraInformation(
    @Embedded val storageEntity: StorageEntity,
    val itemCount:Int,
    val noStockItemCount:Int,
    val lowStockItemCount:Int,
    val highStockItemCount:Int,
    val expiredItemCount:Int,
    val itemToExpireDayCount:Int
)
