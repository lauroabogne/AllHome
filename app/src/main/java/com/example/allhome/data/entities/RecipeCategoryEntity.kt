package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_categories")
data class RecipeCategoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= "unique_id") var uniqueId:String,
    @ColumnInfo(name= "name") var name:String,
    @ColumnInfo(name= "status") var status:Int,
    @ColumnInfo(name= "uploaded") var uploaded:Int,
    @ColumnInfo(name= "created") var created:String,
    @ColumnInfo(name= "modified") var modified:String,
){
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
    }
}
