package com.example.allhome.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = RecipeCategoryAssignmentEntity.TABLE_NAME)
data class RecipeCategoryAssignmentEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_RECIPE_CATEGORY_UNIQUE_ID) var recipeCategoryUniqueId:String,
    @ColumnInfo(name= COLUMN_RECIPE_UNIQUE_ID) var recipeUniqueId:String,
    @ColumnInfo(name= COLUMN_STATUS) var status:Int,
    @ColumnInfo(name= COLUMN_UPLOADED) var uploaded:Int,
    @ColumnInfo(name= COLUMN_CREATED) var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED) var modified:String,
){
    companion object{
        const val TABLE_NAME = "recipe_category_assignments"
        const val COLUMN_UNIQUE_ID = "unique_id"
        const val COLUMN_RECIPE_CATEGORY_UNIQUE_ID = "recipe_category_unique_id"
        const val COLUMN_RECIPE_UNIQUE_ID = "recipe_unique_id"
        const val COLUMN_STATUS = "status"
        const val COLUMN_UPLOADED = "uploaded"
        const val COLUMN_CREATED = "created"
        const val COLUMN_MODIFIED = "modified"

        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
    }
}
