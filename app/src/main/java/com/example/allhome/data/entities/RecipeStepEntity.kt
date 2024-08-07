package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "recipe_steps")
data class RecipeStepEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="unique_id") var uniqueId:String,
    @ColumnInfo(name="recipe_unique_id") var recipeUniqueId:String,
    @ColumnInfo(name="instruction") var instruction:String,
    @ColumnInfo(name="sequence",defaultValue = 0.toString()) var sequence:Int,
    @ColumnInfo(name="status",defaultValue = NOT_DELETED_STATUS.toString()) var status:Int,
    @ColumnInfo(name="uploaded",defaultValue = NOT_UPLOADED.toString()) var uploaded:Int,
    @ColumnInfo(name="created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name="modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String,
):Parcelable{
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1

    }
}
