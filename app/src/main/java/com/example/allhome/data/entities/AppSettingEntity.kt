package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = AppSettingEntity.TABLE_NAME)
data class AppSettingEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_NAME) var name:String,
    @ColumnInfo(name= COLUMN_VALUE) var value:String,
    @ColumnInfo(name= COLUMN_STATUS,defaultValue="${AppSettingEntity.NOT_DELETED_STATUS}") var status:Int,
    @ColumnInfo(name= COLUMN_UPLOADED,defaultValue="${AppSettingEntity.NOT_UPLOADED}") var uploaded:Int,
    @ColumnInfo(name= COLUMN_CREATED,defaultValue="CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED,defaultValue="CURRENT_TIMESTAMP") var modified:String
):Parcelable{
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
        const val RECIPE_COLUMN_NAME  = "recipe_viewing"
        const val RECIPE_GRID_VIEWING = "grid"
        const val RECIPE_LIST_VIEWING = "list"

        const val TABLE_NAME ="app_settings"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_VALUE = "value"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}
