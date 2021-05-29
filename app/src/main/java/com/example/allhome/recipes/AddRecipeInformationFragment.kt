package com.example.allhome.recipes

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
import com.example.allhome.R
import com.example.allhome.databinding.FragmentAddRecipeInformationBinding
import com.example.allhome.databinding.HourAndTimeInputBinding
import com.example.allhome.recipes.viewmodel.AddRecipeInformationFragmentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AddRecipeInformationFragment : Fragment() {


    private lateinit var mAddRecipeInformationFragmentViewModel: AddRecipeInformationFragmentViewModel

    lateinit var mDataBinding:FragmentAddRecipeInformationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

        mAddRecipeInformationFragmentViewModel = ViewModelProvider(this).get(AddRecipeInformationFragmentViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_recipe_information, container, false)
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
        val choices = arrayOf(
            "",
            "Easy",
            "Medium",
            "Hard",
        )

        val alertDialog =  MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Select difficulty")
            .setSingleChoiceItems(choices, 0, null)
            .setPositiveButton("Ok", null)
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .create()

        alertDialog.show()
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
            }else if(hourDisplay.isEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${minutesDisplay}")

            }else if(hourDisplay.isNotEmpty() && minutesDisplay.isEmpty()){
                mDataBinding.preparationTextInputEditText.setText("${hourDisplay}")
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
            }else if(hourDisplay.isEmpty() && minutesDisplay.isNotEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${minutesDisplay}")

            }else if(hourDisplay.isNotEmpty() && minutesDisplay.isEmpty()){
                mDataBinding.cookTimeTextInputEditText.setText("${hourDisplay}")
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