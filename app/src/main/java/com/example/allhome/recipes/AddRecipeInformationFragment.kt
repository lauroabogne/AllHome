package com.example.allhome.recipes

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.RecipeEntity
import com.example.allhome.databinding.FragmentAddRecipeInformationBinding
import com.example.allhome.databinding.HourAndTimeInputBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.recipes.viewmodel.AddRecipeInformationFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*


class AddRecipeInformationFragment : Fragment() {


    private lateinit var mAddRecipeInformationFragmentViewModel: AddRecipeInformationFragmentViewModel

    lateinit var mDataBinding:FragmentAddRecipeInformationBinding
    var mRecipeEntity:RecipeEntity? = null
    var mAction = ADD_ACTION

    companion object{
        const val ADD_ACTION = 0
        const val EDIT_ACTION = 1

         val DIFICULTY_OPTIONS = arrayOf(
            "",
            "Easy",
            "Medium",
            "Hard",
        )

        val RECIPE_INTENT_TAG = "RECIPE_INTENT_TAG"

        @JvmStatic fun newInstanceForEditing(recipeEntity: RecipeEntity) =
            AddRecipeInformationFragment().apply {
                mAction = EDIT_ACTION
                arguments = Bundle().apply {
                    putParcelable(RECIPE_INTENT_TAG, recipeEntity)
                }

            }
        @JvmStatic fun newInstanceForAdd() =
            AddRecipeInformationFragment().apply {

            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if(mAction == EDIT_ACTION){
                mRecipeEntity = it.getParcelable(RECIPE_INTENT_TAG)!!
            }

        }

        mAddRecipeInformationFragmentViewModel = ViewModelProvider(this).get(AddRecipeInformationFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_information, container, false)
        mDataBinding.recipeEntity = mRecipeEntity

        mDataBinding.difficultyTextInputEditText.setOnClickListener {
           showDifficultyPopup()
        }
        mDataBinding.preparationTextInputEditText.setOnClickListener {
            showPrepationTimePopup()
        }
        mDataBinding.cookTimeTextInputEditText.setOnClickListener {
            showCookingTimePopup()
        }
        return mDataBinding.root
    }
    fun showDifficultyPopup(){

        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Select difficulty")
            .setSingleChoiceItems(DIFICULTY_OPTIONS, 0, null)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            val checkedPosition = alertDialog.listView.checkedItemPosition
            mDataBinding.difficultyTextInputEditText.setText(DIFICULTY_OPTIONS[checkedPosition])
            alertDialog.dismiss()
        }
    }
    fun showPrepationTimePopup(){

        val hourAndTimeInputBinding:HourAndTimeInputBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.hour_and_time_input, null, false);

        hourAndTimeInputBinding.hourEditext.filters = arrayOf(MinMaxFilter( 1 , 168 ))
        hourAndTimeInputBinding.minutesEditext.filters = arrayOf(MinMaxFilter( 1 , 59 ))

        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Preperation time")
            .setView(hourAndTimeInputBinding.root)
            .setPositiveButton("Done",null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hour = hourAndTimeInputBinding.hourEditext.text.toString()
            val minutes = hourAndTimeInputBinding.minutesEditext.text.toString()

            val hourDisplay  = if(hour.isEmpty()) "" else "${hour} hrs"
            val minutesDisplay  = if(minutes.isEmpty()) "" else "${minutes} min"

            if(hourDisplay.isNotEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${hourDisplay}  ${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour = hour.toInt()
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isNotEmpty() && minutesDisplay.isEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${hourDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour = hour.toInt()
            }

            alertDialog.dismiss()
        }

    }

    fun showCookingTimePopup(){
        val hourAndTimeInputBinding:HourAndTimeInputBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.hour_and_time_input, null, false);
        hourAndTimeInputBinding.hourEditext.filters = arrayOf(MinMaxFilter( 1 , 168 ))
        hourAndTimeInputBinding.minutesEditext.filters = arrayOf(MinMaxFilter( 1 , 59 ))
        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Cooking time")
            .setView(hourAndTimeInputBinding.root)
            .setPositiveButton("Done", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()
        alertDialog.show()

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val hour = hourAndTimeInputBinding.hourEditext.text.toString()
            val minutes = hourAndTimeInputBinding.minutesEditext.text.toString()

            val hourDisplay  = if(hour.isEmpty()) "" else "${hour} hrs"
            val minutesDisplay  = if(minutes.isEmpty()) "" else "${minutes} min"

            if(hourDisplay.isNotEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${hourDisplay}  ${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempCookTimeHour = hour.toInt()
                mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${minutesDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes = minutes.toInt()

            }else if(hourDisplay.isNotEmpty() && minutesDisplay.isEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${hourDisplay}")
                mAddRecipeInformationFragmentViewModel.mTempCookTimeHour = hour.toInt()
            }
            alertDialog.dismiss()
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun getRecipeInformation():RecipeEntity?{

        val name = mDataBinding.recipeNameTextInputEditText.text.toString()
        val serving = mDataBinding.servingTextInputEditText.text.toString().ifEmpty { "0" }
        val difficulty = mDataBinding.difficultyTextInputEditText.text.toString()
        val preperationHour = mAddRecipeInformationFragmentViewModel.mTempPrepaTimeHour
        val preperationMinute = mAddRecipeInformationFragmentViewModel.mTempPrepaTimeMinutes
        val cookTimeHour = mAddRecipeInformationFragmentViewModel.mTempCookTimeHour
        val cookTimeMinute = mAddRecipeInformationFragmentViewModel.mTempCookTimeMinutes
        val category = mDataBinding.categoryTimeTextInputEditText.text.toString()
        val estimatedCost = mDataBinding.estimatedCostTextInputEditText.text.toString().ifEmpty { "0" }

        val description = mDataBinding.descriptionTextInputEditText.text.toString()


        if(name.isEmpty()){
            showErroPopup("Recipe name must not empty.")
            return null
        }

        var itemUniqueID = UUID.randomUUID().toString()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val recipeInformation  = RecipeEntity(
            uniqueId = itemUniqueID,
            name= name,
            serving = serving.toInt(),
            difficulty = generateDifficultyInteger(difficulty),
            preparationHour=preperationHour,
            preparationMinutes = preperationMinute,
            cookingHours =cookTimeHour,
            cookingMinutes =cookTimeMinute,
            category=category,
            estimatedCost = estimatedCost.toDouble(),
            description = description,
            imageName = "",
            status = RecipeEntity.NOT_DELETED_STATUS,
            uploaded = RecipeEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime
        )

        return recipeInformation

    }

    fun showErroPopup(message:String){
        var dialog = CustomMessageDialogFragment(null,message,true)
        dialog.show(requireActivity().supportFragmentManager,"CustomMessageDialogFragment")
    }
    fun generateDifficultyInteger(difficultyString:String):Int{

        if(difficultyString.length <=0){
            return RecipeEntity.DIFFICULTY_NONE
        }else if(difficultyString.equals(DIFICULTY_OPTIONS[1])){
            return RecipeEntity.DIFFICULTY_EASY
        }else if(difficultyString.equals(DIFICULTY_OPTIONS[2])){
            return RecipeEntity.DIFFICULTY_MEDIUM
        }else if(difficultyString.equals(DIFICULTY_OPTIONS[3])){
            return RecipeEntity.DIFFICULTY_HARD
        }else{
            return RecipeEntity.DIFFICULTY_NONE
        }


    }


}

class MinMaxFilter( var mIntMin: Int,var mIntMax: Int) : InputFilter {

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(mIntMin, mIntMax, input)) return null
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) c >= a && c <= b else c >= b && c <= a
    }
}