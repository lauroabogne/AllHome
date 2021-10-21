package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = IngredientEntity.TABLE_NAME)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name=COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name=COLUMN_RECIPE_UNIQUE_ID) var recipeUniqueId:String,
    //@ColumnInfo(name=COLUMN_QUANTITY,defaultValue = 0.toString()) var quantity:Double,
    //@ColumnInfo(name=COLUMN_UNIT) var unit:String,
    @ColumnInfo(name=COLUMN_NAME) var name:String,
    @ColumnInfo(name=COLUMN_STATUS,defaultValue = NOT_DELETED_STATUS.toString()) var status:Int,
    @ColumnInfo(name=COLUMN_UPLOADED,defaultValue = NOT_UPLOADED.toString()) var uploaded:Int,
    @ColumnInfo(name=COLUMN_CREATED,defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name=COLUMN_MODIFIED,defaultValue = "CURRENT_TIMESTAMP") var modified:String
):Parcelable{
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1

        const val TABLE_NAME ="ingredients"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_RECIPE_UNIQUE_ID ="recipe_unique_id"
        //const val COLUMN_QUANTITY ="quantity"
        //const val COLUMN_UNIT ="unit"
        const val COLUMN_NAME ="name"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}
@Parcelize
data class IngredientEntityTransferringToGroceryList(
    @Embedded val ingredientEntity: IngredientEntity,
    var isSelected:Int = SELECTED,
):Parcelable{
    companion object{
        const val NOT_SELECTED = 0
        const val SELECTED = 1

    }
}