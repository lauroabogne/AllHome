package com.example.allhome.data.entities

import android.net.Uri
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.example.allhome.storage.StorageUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="unique_id") var uniqueId:String,
    @ColumnInfo(name="name") var name:String,
    @ColumnInfo(name="serving") var serving:Int,
    @ColumnInfo(name="difficulty",defaultValue =DIFFICULTY_NONE.toString() ) var difficulty:Int,
    @ColumnInfo(name="preparation_hour") var preparationHour:Int,
    @ColumnInfo(name="preparationMinutes") var preparationMinutes:Int,
    @ColumnInfo(name="cooking_hours") var cookingHours:Int,
    @ColumnInfo(name="cooking_minutes") var cookingMinutes:Int,
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

        const val DIFFICULTY_NONE = 0
        const val DIFFICULTY_EASY = 1
        const val DIFFICULTY_MEDIUM = 2
        const val DIFFICULTY_HARD = 3

    }
}


@BindingAdapter("android:setRecipeImage")
fun setRecipeImage(view: ImageView,imageName:String){

    val context = view.context
    Glide.with(context)
        .load(context.resources.getIdentifier("adobo","drawable",context.packageName))
        .into(view)
}
@BindingAdapter("android:setServingText")
fun setServingText(textViwe: TextView, serving:Int){
    if(serving <=0){
        textViwe.visibility = View.GONE
        return
    }
    textViwe.setText("Serving: ${serving}")
}
@BindingAdapter("android:setRecipeCost")
fun setRecipeCost(textViwe: TextView, cost:Double){
    if(cost <=0.0){
        textViwe.visibility = View.GONE
        return
    }
    textViwe.setText("Cost: ${cost}")
}