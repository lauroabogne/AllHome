package com.example.allhome.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.FilterByInformationDialogFragmentBinding
import com.example.allhome.recipes.viewmodel.RecipesFragmentViewModel
import com.example.allhome.utils.MinMaxInputFilter
class FilterByInformationDialogFragment(val mRecipesFragmentViewModel: RecipesFragmentViewModel): DialogFragment() {
         lateinit var mFilterByInformationDialogFragmentBinding:FilterByInformationDialogFragmentBinding
         var mRecipeInformationFilterListener:RecipesFragment.RecipeInformationFilterListener? = null

    companion object{
        const val EQAUL = "="
        const val LESS_THAN = "<"
        const val GREATER_THAN = ">"
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val inflater = LayoutInflater.from(requireContext())
            mFilterByInformationDialogFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.filter_by_information_dialog_fragment,null,false)
            mFilterByInformationDialogFragmentBinding.recipesFragmentViewModel = mRecipesFragmentViewModel

            val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
            mFilterByInformationDialogFragmentBinding.hourPrepPlusCookTimeTextInputEditText.filters = arrayOf(MinMaxInputFilter( 1 , 168))
            mFilterByInformationDialogFragmentBinding.minutePrepPlusCookTimeTextInputEditText.filters = arrayOf(MinMaxInputFilter( 1 , 59 ))

            alertDialogBuilder.setView(mFilterByInformationDialogFragmentBinding.root)
            alertDialogBuilder.setPositiveButton("Continue", DialogInterface.OnClickListener { dialog, which ->
                checkFilters()

            })
            alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
            })

        return alertDialogBuilder.create()
    }
    fun setRecipeInformationFilterListener(recipeInformationFilterListener:RecipesFragment.RecipeInformationFilterListener){
        mRecipeInformationFilterListener = recipeInformationFilterListener
    }
    private fun checkFilters(){

        val costSpinnerSelectedFilter = mFilterByInformationDialogFragmentBinding.costSpinner.selectedItem.toString()
        val servingSelectedFilter = mFilterByInformationDialogFragmentBinding.servingSpinner.selectedItem.toString()
        val prepPlusCookTimeSelectedFilter = mFilterByInformationDialogFragmentBinding.prepPlusCookTimeSpinner.selectedItem.toString()

        val costString = mFilterByInformationDialogFragmentBinding.costTextInputEditText.text.toString()
        val servingString = mFilterByInformationDialogFragmentBinding.servingTextInputEditText.text.toString()
        val prepPlusCookHourString = mFilterByInformationDialogFragmentBinding.hourPrepPlusCookTimeTextInputEditText.text.toString()
        val prepPlusCookMinutesString = mFilterByInformationDialogFragmentBinding.minutePrepPlusCookTimeTextInputEditText.text.toString()

        val hasCostInput = hasCostInput(costString)
        val hasServingInput = hasServingInput(servingString)
        val hasHourOrMinuteInput = hasHourOrMinuteInput(prepPlusCookHourString,prepPlusCookMinutesString)

        mRecipeInformationFilterListener?.let {
            it.filterConditions(convertEqualLessThanGreaterThanToSymbol(costSpinnerSelectedFilter), convertEqualLessThanGreaterThanToSymbol(servingSelectedFilter), convertEqualLessThanGreaterThanToSymbol(prepPlusCookTimeSelectedFilter))
            it.filters(costString,servingString,prepPlusCookHourString,prepPlusCookMinutesString)
            it.onFilterSet(hasCostInput,hasServingInput,hasHourOrMinuteInput)
        }


    }

    fun hasCostInput(costString:String):Boolean{
        if(costString.isEmpty()){
            return false
        }
        if(costString.toDouble() == 0.0){
            return false
        }
        return true
    }
    fun hasServingInput(servingString:String):Boolean{
        if(servingString.isEmpty()){
            return false
        }

        if(servingString.toInt() == 0){
            return false
        }

        return true

    }

    fun hasHourOrMinuteInput(hourString:String,minuteString:String):Boolean{

        if(hourString.isEmpty() && minuteString.isEmpty()){
            return false
        }

        val hour = if(hourString.isEmpty()) 0  else hourString.toInt()
        val minutes = if(minuteString.isEmpty()) 0 else minuteString.toInt()

        if(hour == 0 && minutes == 0){
            return  false
        }

        return true
    }
    private fun convertEqualLessThanGreaterThanToSymbol(stringToConvert:String):String{

        Log.e("FILTER",stringToConvert+" ${stringToConvert.equals(getString(R.string.prep_plus_cook_time_is_greater_than))}")
        if(stringToConvert.equals(getString(R.string.cost_is_equal)) || stringToConvert.equals(getString(R.string.prep_plus_cook_time_is_equal))|| stringToConvert.equals(getString(R.string.serving_is_equal))){
            return EQAUL
        }

        if(stringToConvert.equals(getString(R.string.cost_is_less_than)) || stringToConvert.equals(getString(R.string.prep_plus_cook_time_is_less_than))|| stringToConvert.equals(getString(R.string.serving_is_less_than))){
            return LESS_THAN
        }
        if(stringToConvert.equals(getString(R.string.cost_is_greater_than)) || stringToConvert.equals(getString(R.string.prep_plus_cook_time_is_greater_than))|| stringToConvert.equals(getString(R.string.serving_is_greater_than))){
            return GREATER_THAN
        }
        return EQAUL

    }

}