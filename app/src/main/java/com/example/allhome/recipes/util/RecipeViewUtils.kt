package com.example.allhome.recipes

import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.utils.NumberUtils
import com.google.android.material.textfield.TextInputEditText


@BindingAdapter("android:setServing")
    fun setServing(textView: TextView, serving:Int){

        if(serving <=0){
            textView.visibility = View.GONE
            return
        }

        textView.setText(serving.toString())

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
@BindingAdapter("android:setPreparationTime")
fun setPreparationTime(textInputEditText: TextInputEditText, recipeEntity: RecipeEntity?){


    if(recipeEntity == null){
        return;
    }
    if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes > 0){
        val hourText = if(recipeEntity.preparationHour == 1) "${recipeEntity.preparationHour} hour" else "${recipeEntity.preparationHour} hours"
        val minutestText = if(recipeEntity.preparationMinutes == 1) "${recipeEntity.preparationMinutes} minute" else "${recipeEntity.preparationMinutes} minutes"
        textInputEditText.setText(hourText+" "+minutestText)

    }else if(recipeEntity.preparationHour <=0 && recipeEntity.preparationMinutes > 0){
        val minutestText = if(recipeEntity.preparationMinutes == 1) "${recipeEntity.preparationMinutes} minute" else "${recipeEntity.preparationMinutes} minutes"
        textInputEditText.setText(minutestText)

    }else if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes <= 0){
        val hourText = if(recipeEntity.preparationHour == 1) "${recipeEntity.preparationHour} hour" else "${recipeEntity.preparationHour} hours"
        textInputEditText.setText(hourText)

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
@BindingAdapter("android:setCookingTime")
fun setCookingTime(textInputEditText:TextInputEditText,recipeEntity: RecipeEntity?){
    if(recipeEntity == null){
        return
    }
    if(recipeEntity.cookingHours > 0 && recipeEntity.cookingMinutes > 0){
        val hourText = if(recipeEntity.cookingHours == 1) "${recipeEntity.cookingHours} hour" else "${recipeEntity.cookingHours} hours"
        val minutestText = if(recipeEntity.cookingMinutes == 1) "${recipeEntity.cookingMinutes} minute" else "${recipeEntity.cookingMinutes} minutes"
        textInputEditText.setText(hourText+" "+minutestText)

    }else if(recipeEntity.cookingHours <=0 && recipeEntity.cookingMinutes > 0){
        val minutestText = if(recipeEntity.cookingMinutes == 1) "${recipeEntity.cookingMinutes} minute" else "${recipeEntity.cookingMinutes} minutes"
        textInputEditText.setText(minutestText)

    }else if(recipeEntity.cookingHours > 0 && recipeEntity.cookingHours <= 0){
        val hourText = if(recipeEntity.cookingHours == 1) "${recipeEntity.cookingHours} hour" else "${recipeEntity.cookingHours} hours"
        textInputEditText.setText(hourText)

    }
}

@BindingAdapter("android:setDifficulty")
fun setDifficulty(textview:TextView,difficulty:Int){

    if(difficulty == RecipeEntity.DIFFICULTY_EASY){
        textview.setText(RecipeEntity.DIFFICULTY_EASY_TEXT)

    }else if(difficulty == RecipeEntity.DIFFICULTY_MEDIUM){
        textview.setText(RecipeEntity.DIFFICULTY_MEDIUM_TEXT)
    }else if(difficulty == RecipeEntity.DIFFICULTY_HARD){
        textview.setText(RecipeEntity.DIFFICULTY_HARD_TEXT)
    }

}
@BindingAdapter("android:setDifficulty")
fun setDifficulty(textInputEditText:TextInputEditText,difficulty:Int){

    if(difficulty == RecipeEntity.DIFFICULTY_EASY){
        textInputEditText.setText(RecipeEntity.DIFFICULTY_EASY_TEXT)

    }else if(difficulty == RecipeEntity.DIFFICULTY_MEDIUM){
        textInputEditText.setText(RecipeEntity.DIFFICULTY_MEDIUM_TEXT)
    }else if(difficulty == RecipeEntity.DIFFICULTY_HARD){
        textInputEditText.setText(RecipeEntity.DIFFICULTY_HARD_TEXT)
    }

}

@BindingAdapter("android:setIngredient")
fun setIngredient(textView:TextView,ingredient:IngredientEntity){
    val quantity = ingredient.quantity
    val unit = ingredient.unit
    val name = ingredient.name

    if(quantity <=0 && unit.trim().length <=0){
        textView.setText(name)
    }else if(quantity > 0 && unit.trim().length <=0){
        textView.setText("${NumberUtils.fraction(quantity)} ${name}")
    }else if(quantity > 0 && unit.trim().length > 0){
        textView.setText("${NumberUtils.fraction(quantity)} ${unit} ${name}")
    }


}
@BindingAdapter("android:setIngredient")
fun setIngredient(editText:EditText,ingredient:IngredientEntity){
    val quantity = ingredient.quantity
    val unit = ingredient.unit
    val name = ingredient.name

    if(quantity <=0 && unit.trim().length <=0){
        editText.setText(name)
    }else if(quantity > 0 && unit.trim().length <=0){
        editText.setText("${NumberUtils.fraction(quantity)} ${name}")
    }else if(quantity > 0 && unit.trim().length > 0){
        editText.setText("${NumberUtils.fraction(quantity)} ${unit} ${name}")
    }


}
