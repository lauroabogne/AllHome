package com.example.allhome.recipes

import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.allhome.R
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.example.allhome.utils.ImageUtil
import com.example.allhome.utils.IngredientEvaluator
import com.example.allhome.utils.NumberUtils
import com.google.android.material.textfield.TextInputEditText


@BindingAdapter("android:setServing")
    fun setServing(textView: TextView, serving:Int){

        if(serving <=0){
            textView.visibility = View.GONE
            return
        }

    textView.text = serving.toString()

    }
@BindingAdapter("android:setCost")
fun setCost(textView:TextView,cost:Double){

}
@BindingAdapter("android:setRecipeIngredientText")
fun setRecipeIngredientText(textView:TextView,ingredient: IngredientEntity){
    val quantity =  ingredient.quantity
    val unit = ingredient.unit
    var quantityAndUnit:String=""
    if(quantity.length > 0 && unit.length > 0){
        quantityAndUnit = "${quantity} ${unit}"
    }else if(quantity.length > 0 && unit.length <= 0){
        quantityAndUnit = "${quantity}"
    }else if(quantity.length <= 0 && unit.length > 0){
        quantityAndUnit = "${unit}"
    }

    if(ingredient.name.equals("onion (sliced)")){
        Log.e("FOUND","${ingredient}")
    }


    var ingredientText: SpannableStringBuilder
    ingredientText = SpannableStringBuilder("${quantityAndUnit} ${ingredient.name}")
    ingredientText.setSpan(StyleSpan(Typeface.BOLD), 0, quantityAndUnit.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    textView.text = ingredientText
}
@BindingAdapter("android:setRecipeImage")
fun setRecipeImage(view: ImageView, imageName:String){

    val context = view.context

    val imageUri = ImageUtil.getImageUriFromPath(context,ImageUtil.RECIPE_IMAGES_FINAL_LOCATION,"${imageName}.${ImageUtil.IMAGE_NAME_SUFFIX}")
    Glide.with(context).load(imageUri).error(R.drawable.ic_baseline_image_24).into(view)


}
@BindingAdapter("android:setServingText")
fun setServingText(textViwe: TextView, serving:Int){
    if(serving <=0){
        textViwe.visibility = View.GONE
        return
    }
    textViwe.text = "Serving: ${serving}"
}
@BindingAdapter("android:setRecipeCost")
fun setRecipeCost(textView: TextView, cost:Double){
    if(cost <=0.0){
        textView.visibility = View.GONE
        return
    }


    textView.text = "Cost: ${ NumberUtils.formatNumber(cost)}"
}
@BindingAdapter("android:setRecipeCostAndVisibility")
fun setRecipeCostAndVisibility(textView: TextView, cost:Double){
    if(cost <=0.0){
        textView.visibility = View.GONE
        return
    }

    textView.text = "${ NumberUtils.formatNumber(cost)}"
}

@BindingAdapter(value=["bind:totalIngredient","bind:totalIngredientMatch"])
fun setRecipeCount(textView: TextView, totalIngredient:Int,totalIngredientMatch:Int){

    textView.visibility = if(totalIngredientMatch <=0) View.GONE else View.VISIBLE
    textView.setText("${totalIngredientMatch} of ${totalIngredient} ingredient match")
}

@BindingAdapter("android:setPreparationTime")
fun setPreparationTime(textview:TextView,recipeEntity: RecipeEntity){

    if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes > 0){
        val hourText = if(recipeEntity.preparationHour == 1) "${recipeEntity.preparationHour} hour" else "${recipeEntity.preparationHour} hours"
        val minutestText = if(recipeEntity.preparationMinutes == 1) "${recipeEntity.preparationMinutes} minute" else "${recipeEntity.preparationMinutes} minutes"
        textview.text = hourText+" "+minutestText

    }else if(recipeEntity.preparationHour <=0 && recipeEntity.preparationMinutes > 0){
        val minutestText = if(recipeEntity.preparationMinutes == 1) "${recipeEntity.preparationMinutes} minute" else "${recipeEntity.preparationMinutes} minutes"
        textview.text = minutestText

    }else if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes <= 0){
        val hourText = if(recipeEntity.preparationHour == 1) "${recipeEntity.preparationHour} hour" else "${recipeEntity.preparationHour} hours"
        textview.text = hourText

    }
}
@BindingAdapter("android:setPreparationTime")
fun setPreparationTime(textInputEditText: TextInputEditText, recipeEntity: RecipeEntity?){


    if(recipeEntity == null){
        return
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

    }else if(recipeEntity.preparationHour > 0 && recipeEntity.preparationMinutes <= 0){
        val hourText =  "${recipeEntity.preparationHour} hour"
        textInputEditText.setText(hourText)

    }

}


@BindingAdapter("android:setCookingTime")
fun setCookingTime(textview:TextView,recipeEntity: RecipeEntity){

    if(recipeEntity.cookingHours > 0 && recipeEntity.cookingMinutes > 0){
        val hourText = if(recipeEntity.cookingHours == 1) "${recipeEntity.cookingHours} hour" else "${recipeEntity.cookingHours} hours"
        val minutestText = if(recipeEntity.cookingMinutes == 1) "${recipeEntity.cookingMinutes} minute" else "${recipeEntity.cookingMinutes} minutes"
        textview.text = hourText+" "+minutestText

    }else if(recipeEntity.cookingHours <=0 && recipeEntity.cookingMinutes > 0){
        val minutestText = if(recipeEntity.cookingMinutes == 1) "${recipeEntity.cookingMinutes} minute" else "${recipeEntity.cookingMinutes} minutes"
        textview.text = minutestText

    }else if(recipeEntity.cookingHours > 0 && recipeEntity.cookingHours <= 0){
        val hourText = if(recipeEntity.cookingHours == 1) "${recipeEntity.cookingHours} hour" else "${recipeEntity.cookingHours} hours"
        textview.text = hourText

    }else if(recipeEntity.cookingHours > 0 && recipeEntity.cookingMinutes <= 0){
        val hourText =  "${recipeEntity.cookingHours} hour"
        textview.text = hourText

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

    }else if(recipeEntity.cookingHours > 0 && recipeEntity.cookingMinutes <= 0){
        val hourText = "${recipeEntity.cookingHours} hour"
        textInputEditText.setText(hourText)

    }
}

@BindingAdapter("android:setDifficulty")
fun setDifficulty(textview:TextView,difficulty:Int){

    if(difficulty == RecipeEntity.DIFFICULTY_EASY){
        textview.text = RecipeEntity.DIFFICULTY_EASY_TEXT

    }else if(difficulty == RecipeEntity.DIFFICULTY_MEDIUM){
        textview.text = RecipeEntity.DIFFICULTY_MEDIUM_TEXT
    }else if(difficulty == RecipeEntity.DIFFICULTY_HARD){
        textview.text = RecipeEntity.DIFFICULTY_HARD_TEXT
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

    val name = ingredient.name
    textView.text = name



}
@BindingAdapter("android:setIngredient")
fun setIngredient(editText:EditText,ingredient:IngredientEntity){

    val ingredient = "${ingredient.quantity} ${ingredient.unit} ${ingredient.name}".trim()
    editText.setText(ingredient)

}
@BindingAdapter("android:setIngredentToGroceryQuantityAndUnit")
fun setIngredentToGroceryQuantityAndUnit(textView:TextView,ingredent:IngredientEntity){
    val quantity =  ingredent.quantity
    val unit = ingredent.unit
    var quantityAndUnit:String=""
    if(quantity.length > 0 && unit.length > 0){
        quantityAndUnit = "${quantity} ${unit}"
    }else if(quantity.length > 0 && unit.length <= 0){
        quantityAndUnit = "${quantity}"
    }else if(quantity.length <= 0 && unit.length > 0){
        quantityAndUnit = "${unit}"
    }

    textView.setText(quantityAndUnit)

}

@BindingAdapter("android:setCostSpinnerValue")
fun setCostSpinnerValue(spinner:Spinner,recipesFragmentViewModel:RecipesFragmentViewModel?){
    val resources = spinner.context.resources
    val filters = resources.getStringArray(R.array.cost_filter)
    recipesFragmentViewModel?.mCostCondition.let {
        when(it){
            FilterByInformationDialogFragment.EQAUL->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.cost_is_equal)))
            }
            FilterByInformationDialogFragment.GREATER_THAN->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.cost_is_greater_than)))
            }
            FilterByInformationDialogFragment.LESS_THAN->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.cost_is_less_than)))
            }
            else->{
                spinner.setSelection(0)
            }
        }
    }
}

@BindingAdapter("android:setServingSpinnerValue")
fun setServingSpinnerValue(spinner:Spinner,recipesFragmentViewModel:RecipesFragmentViewModel?){
    val resources = spinner.context.resources
    val filters = resources.getStringArray(R.array.serving_filter)
    recipesFragmentViewModel?.mServingCondition.let {
        when(it){
            FilterByInformationDialogFragment.EQAUL->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.serving_is_equal)))
            }
            FilterByInformationDialogFragment.GREATER_THAN->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.serving_is_greater_than)))
            }
            FilterByInformationDialogFragment.LESS_THAN->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.serving_is_less_than)))
            }
            else->{
                spinner.setSelection(0)
            }
        }
    }
}

@BindingAdapter("android:setPrepPlusCookingTimeSpinnerValue")
fun setPrepPlusCookingTimeSpinnerValue(spinner:Spinner,recipesFragmentViewModel:RecipesFragmentViewModel?){
    val resources = spinner.context.resources
    val filters = resources.getStringArray(R.array.total_time_filter)
    recipesFragmentViewModel?.mPrepPlusCookTimeCondition.let {
        when(it){
            FilterByInformationDialogFragment.EQAUL->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.prep_plus_cook_time_is_equal)))
            }
            FilterByInformationDialogFragment.GREATER_THAN->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.prep_plus_cook_time_is_greater_than)))
            }
            FilterByInformationDialogFragment.LESS_THAN->{
                spinner.setSelection(filters.indexOf(resources.getString(R.string.prep_plus_cook_time_is_less_than)))
            }
            else->{
                spinner.setSelection(0)
            }
        }
    }
}

@BindingAdapter("android:setCostText")
fun setCostText(textInputEditText:TextInputEditText,recipesFragmentViewModel:RecipesFragmentViewModel?){

    recipesFragmentViewModel?.let{
        val costString = it.mCostString

        if(costString.isEmpty()){
            textInputEditText.setText("")
            return
        }

        val servingInt = costString.toInt()

        if(servingInt <=0){
            textInputEditText.setText("")
            return
        }

        textInputEditText.setText(costString)
    }

}
@BindingAdapter("android:setServingText")
fun setServingText(textInputEditText:TextInputEditText,recipesFragmentViewModel:RecipesFragmentViewModel?){

    recipesFragmentViewModel?.let{
        val servingString = it.mServingString

        if(servingString.isEmpty()){
            textInputEditText.setText("")
            return
        }
        val servingInt = servingString.toInt()

        if(servingInt <=0){
            textInputEditText.setText("")
            return
        }
        textInputEditText.setText(servingString)
    }
}

@BindingAdapter("android:setHourText")
fun setHourText(textInputEditText:TextInputEditText,recipesFragmentViewModel:RecipesFragmentViewModel?){

    recipesFragmentViewModel?.let{
        val hourString = it.mPrepPlusCookHourString

        if(hourString.isEmpty()){
            textInputEditText.setText("")
            return
        }
        val servingInt = hourString.toInt()

        if(servingInt <=0){
            textInputEditText.setText("")
            return
        }
        textInputEditText.setText(hourString)
    }
}
@BindingAdapter("android:setMinuteText")
fun setMinuteText(textInputEditText:TextInputEditText,recipesFragmentViewModel:RecipesFragmentViewModel?){

    recipesFragmentViewModel?.let{
        val minutesString = it.mPrepPlusCookMinutesString

        if(minutesString.isEmpty()){
            textInputEditText.setText("")
            return
        }
        val servingInt = minutesString.toInt()

        if(servingInt <=0){
            textInputEditText.setText("")
            return
        }
        textInputEditText.setText(minutesString)
    }
}
