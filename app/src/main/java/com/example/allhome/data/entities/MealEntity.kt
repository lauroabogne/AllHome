package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = MealEntity.TABLE_NAME)
data class MealEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_RECIPE_UNIQUE_ID) var recipeUniqueId:String,
    @ColumnInfo(name= COLUMN_NAME) var name:String,
    @ColumnInfo(name= COLUMN_DATE) var date:String,
    @ColumnInfo(name= COLUMN_TYPE) var type:Int,
    @ColumnInfo(name= COLUMN_KIND) var kind:Int,
    @ColumnInfo(name= COLUMN_COST) var cost:Double,
    @ColumnInfo(name= COLUMN_UPLOADED,defaultValue = NOT_UPLOADED.toString()) var uploaded:Int,
    @ColumnInfo(name= COLUMN_DELETED,defaultValue = NOT_DELETED.toString()) var deleted:Int,
    @ColumnInfo(name= COLUMN_CREATED, defaultValue = "CURRENT_TIMESTAMP" ) var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED, defaultValue = "CURRENT_TIMESTAMP" ) var modified:String
):Parcelable{
    companion object{
        const val TABLE_NAME = "meals"
        const val COLUMN_UNIQUE_ID= "unique_id"
        const val COLUMN_RECIPE_UNIQUE_ID= "recipe_unique_id"
        const val COLUMN_NAME= "name"
        const val COLUMN_DATE= "date"
        const val COLUMN_TYPE= "type"
        const val COLUMN_KIND= "kind"
        const val COLUMN_COST= "cost"
        const val COLUMN_CREATED= "created"
        const val COLUMN_MODIFIED= "modified"
        const val COLUMN_UPLOADED= "uploaded"
        const val COLUMN_DELETED= "deleted"

        const val NO_KIND = 0
        const val RECIPE_KIND =1
        const val QUICK_RECIPE_KIND =2

        const val NO_TYPE =0
        const val BREAKFAST_TYPE = 1
        const val SNACK_AFTER_BREAKFAST_TYPE = 2
        const val LUNCK_TYPE = 3
        const val SNACK_AFTERLUNCK_TYPE = 4
        const val DINNER_TYPE = 5
        const val SNACK_AFTER_DINNER_TYPE = 6

        const val UPLOADED = 1
        const val NOT_UPLOADED = 0
        const val DELETED = 1
        const val NOT_DELETED = 0
    }
}
@Parcelize
data class MealTypes(
    var type:Int
):Parcelable
