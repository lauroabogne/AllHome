package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "recipes")
data class RecipeEntity(
    @ColumnInfo(name="unique_id") var uniqueId:String,
    @ColumnInfo(name="name") var name:String,
    @ColumnInfo(name="serving") var serving:Int,
    @ColumnInfo(name="difficulty") var difficulty:String,
    @ColumnInfo(name="preparation_time_in_minutes") var preparationTimeInMinutes:Int,
    @ColumnInfo(name="cooking_time_in_minutes") var cookingTimeInMinutes:Int,
    @ColumnInfo(name="category") var category:String,
    @ColumnInfo(name="estimated_cost") var estimatedCost:Double,
    @ColumnInfo(name="description") var description:String,
    @ColumnInfo(name="image_name") var imageName:String,
    @ColumnInfo(name="status",defaultValue = NOT_DELETED_STATUS.toString()) var status:Int,
    @ColumnInfo(name="uploaded",defaultValue = NOT_UPLOADED.toString()) var uploaded:Int,
    @ColumnInfo(name="created", defaultValue = "CURRENT_TIMESTAMP" ) var created:String,
    @ColumnInfo(name="modified", defaultValue = "CURRENT_TIMESTAMP" ) var modified:String
):Parcelable{
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
    }
}
