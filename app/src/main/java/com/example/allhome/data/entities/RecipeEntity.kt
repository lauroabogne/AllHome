package com.example.allhome.data.entities

import android.net.Uri
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.*
import com.example.allhome.storage.StorageUtil
import com.example.allhome.utils.NumberUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = RecipeEntity.TABLE_NAME)
data class RecipeEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_NAME) var name:String,
    @ColumnInfo(name= COLUMN_SERVING) var serving:Int,
    @ColumnInfo(name= COLUMN_DIFFICULTY,defaultValue =DIFFICULTY_NONE.toString() ) var difficulty:Int,
    @ColumnInfo(name= COLUMN_PREPARATION_HOUR) var preparationHour:Int,
    @ColumnInfo(name= COLUMN_PREPARATION_MINUTES) var preparationMinutes:Int,
    @ColumnInfo(name= COLUMN_COOKING_HOUR) var cookingHours:Int,
    @ColumnInfo(name= COLUMN_COOKING_MINUTES) var cookingMinutes:Int,
    @ColumnInfo(name= COLUMN_CATEGORY) var category:String,
    @ColumnInfo(name= COLUMN_ESTIMATED_COST) var estimatedCost:Double,
    @ColumnInfo(name= COLUMN_DESCRIPTION) var description:String,
    @ColumnInfo(name= COLUMN_IMAGE_NAME) var imageName:String,
    @ColumnInfo(name= COLUMN_STATUS,defaultValue = NOT_DELETED_STATUS.toString()) var status:Int,
    @ColumnInfo(name= COLUMN_UPLOADED,defaultValue = NOT_UPLOADED.toString()) var uploaded:Int,
    @ColumnInfo(name= COLUMN_CREATED, defaultValue = "CURRENT_TIMESTAMP" ) var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED, defaultValue = "CURRENT_TIMESTAMP" ) var modified:String
):Parcelable{
    companion object{
        const val TABLE_NAME = "recipes"
        const val COLUMN_UNIQUE_ID= "unique_id"
        const val COLUMN_NAME= "name"
        const val COLUMN_SERVING= "serving"
        const val COLUMN_DIFFICULTY= "difficulty"
        const val COLUMN_PREPARATION_HOUR= "preparation_hour"
        const val COLUMN_PREPARATION_MINUTES= "preparationMinutes"
        const val COLUMN_COOKING_HOUR= "cooking_hours"
        const val COLUMN_COOKING_MINUTES= "cooking_minutes"
        const val COLUMN_CATEGORY= "category"
        const val COLUMN_ESTIMATED_COST= "estimated_cost"
        const val COLUMN_DESCRIPTION= "description"
        const val COLUMN_IMAGE_NAME= "image_name"
        const val COLUMN_STATUS= "status"
        const val COLUMN_UPLOADED= "uploaded"
        const val COLUMN_CREATED= "created"
        const val COLUMN_MODIFIED= "modified"



        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1

        const val DIFFICULTY_NONE = 0
        const val DIFFICULTY_EASY = 1
        const val DIFFICULTY_MEDIUM = 2
        const val DIFFICULTY_HARD = 3

        const val DIFFICULTY_NONE_TEXT = ""
        const val DIFFICULTY_EASY_TEXT = "easy"
        const val DIFFICULTY_MEDIUM_TEXT = "mediun"
        const val DIFFICULTY_HARD_TEXT = "hard"


    }
}
@Parcelize
data class RecipeEntityWithTotalIngredient(
    @Embedded val recipeEntity: RecipeEntity,
    var totalIngredientCount: Int = 0,
    var totalIngredientMatchCount: Int = 0,
    var totalIngredientCountLessTotalIngredientMatchCount:Int? = 0

):Parcelable{

    companion object{
        const val TOTAL_INGREDIENT_COUNT= "totalIngredientCount"
        const val TOTAL_INGREDIENT_MATCH_COUNT = "totalIngredientMatchCount"
        const val TOTAL_INGREDIENT_COUNT_LESS_TOTAL_INGREDIENT_MATCH_COUNT = "totalIngredientCountLessTotalIngredientMatchCount"
    }
}



