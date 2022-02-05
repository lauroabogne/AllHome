package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = RecipeCategoryEntity.TABLE_NAME)
data class RecipeCategoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_NAME) var name:String,
    @ColumnInfo(name= COLUMN_STATUS) var status:Int,
    @ColumnInfo(name= COLUMN_UPLOADED) var uploaded:Int,
    @ColumnInfo(name= COLUMN_CREATED) var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED) var modified:String,
){
    companion object{
        const val TABLE_NAME = "recipe_categories"
        const val COLUMN_UNIQUE_ID = "unique_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_STATUS = "status"
        const val COLUMN_UPLOADED= "uploaded"
        const val COLUMN_CREATED= "created"
        const val COLUMN_MODIFIED =  "modified"
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
    }
}
