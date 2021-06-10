package com.example.allhome.recipes

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.utils.NumberUtils


@BindingAdapter("android:setServing")
    fun setServing(textView: TextView, serving:Int){

    }
@BindingAdapter("android:setCost")
fun setCost(textView:TextView,cost:Double){

}
@BindingAdapter("android:setRecipeImage")
fun setRecipeImage(view: ImageView, imageName:String){

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
fun setRecipeCost(textView: TextView, cost:Double){
    if(cost <=0.0){
        textView.visibility = View.GONE
        return
    }


    textView.setText("Cost: ${ NumberUtils.formatNumber(cost)}")
}
@BindingAdapter("android:setRecipeCostAndVisibility")
fun setRecipeCostAndVisibility(textView: TextView, cost:Double){
    if(cost <=0.0){
        textView.visibility = View.GONE
        return
    }

    textView.setText("${ NumberUtils.formatNumber(cost)}")
}
@BindingAdapter("android:setPreparationTime")
fun setPreparationTime(textview:TextView,recipeEntity: RecipeEntity){

    if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes > 0){
        val hourText = if(recipeEntity.preparationHour == 1) "${recipeEntity.preparationHour} hour" else "${recipeEntity.preparationHour} hours"
        val minutestText = if(recipeEntity.preparationMinutes == 1) "${recipeEntity.preparationMinutes} minute" else "${recipeEntity.preparationMinutes} minutes"
        textview.setText(hourText+" "+minutestText)

    }else if(recipeEntity.preparationHour <=0 && recipeEntity.preparationMinutes > 0){
        val minutestText = if(recipeEntity.preparationMinutes == 1) "${recipeEntity.preparationMinutes} minute" else "${recipeEntity.preparationMinutes} minutes"
        textview.setText(minutestText)

    }else if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes <= 0){
        val hourText = if(recipeEntity.preparationHour == 1) "${recipeEntity.preparationHour} hour" else "${recipeEntity.preparationHour} hours"
        textview.setText(hourText)

    }
}

@BindingAdapter("android:setCookingTime")
fun setCookingTime(textview:TextView,recipeEntity: RecipeEntity){

    if(recipeEntity.cookingHours > 0 && recipeEntity.cookingMinutes > 0){
        val hourText = if(recipeEntity.cookingHours == 1) "${recipeEntity.cookingHours} hour" else "${recipeEntity.cookingHours} hours"
        val minutestText = if(recipeEntity.cookingMinutes == 1) "${recipeEntity.cookingMinutes} minute" else "${recipeEntity.cookingMinutes} minutes"
        textview.setText(hourText+" "+minutestText)

    }else if(recipeEntity.cookingHours <=0 && recipeEntity.cookingMinutes > 0){
        val minutestText = if(recipeEntity.cookingMinutes == 1) "${recipeEntity.cookingMinutes} minute" else "${recipeEntity.cookingMinutes} minutes"
        textview.setText(minutestText)

    }else if(recipeEntity.cookingHours > 0 && recipeEntity.cookingHours <= 0){
        val hourText = if(recipeEntity.cookingHours == 1) "${recipeEntity.cookingHours} hour" else "${recipeEntity.cookingHours} hours"
        textview.setText(hourText)

    }
}
