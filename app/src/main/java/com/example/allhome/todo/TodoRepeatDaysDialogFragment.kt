package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.TodoRepeatDaysBinding

class TodoRepeatDaysDialogFragment(val repeat: Int?, val days: List<String>?): DialogFragment() {

    private var mOnNumberOfDaysSetListener:OnNumberOfDaysSetListener? = null
    private var mTodoRepeatDaysBinding:TodoRepeatDaysBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater

        mTodoRepeatDaysBinding = DataBindingUtil.inflate(inflater, R.layout.todo_repeat_days,null,false)
        initUI()


        mTodoRepeatDaysBinding?.numberOfDays?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val inputValue = s.toString().toIntOrNull() ?: 0

                if (inputValue != 1) {
                    mTodoRepeatDaysBinding?.daysRadioGroup!!.visibility = View.GONE
                    mTodoRepeatDaysBinding?.repeatOn!!.visibility = View.GONE
                } else {
                    mTodoRepeatDaysBinding?.daysRadioGroup!!.visibility = View.VISIBLE
                    mTodoRepeatDaysBinding?.repeatOn!!.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        builder.setView(mTodoRepeatDaysBinding?.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Continue",null)


        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveBtn.setOnClickListener {

                val repeatDay  = getRepeatDayNumber()
                val daysSelected = if(repeatDay < 1 || repeatDay > 1) listOf() else getAllCheckedDays()
                mOnNumberOfDaysSetListener?.numberOfDays(repeatDay,daysSelected)

            }
        }

        return dialog
    }
    private fun initUI(){

        if(repeat != null && repeat > 0 ){
            mTodoRepeatDaysBinding?.numberOfDays?.setText("$repeat")
        }

        if(repeat != null && repeat > 1){
            mTodoRepeatDaysBinding?.daysRadioGroup!!.visibility = View.GONE
            mTodoRepeatDaysBinding?.repeatOn!!.visibility = View.GONE
        }

        if(days != null && days.isNotEmpty()){
            mTodoRepeatDaysBinding?.mondayCheckBox?.isChecked = false
            mTodoRepeatDaysBinding?.tuesdayCheckBox?.isChecked = false
            mTodoRepeatDaysBinding?.wednesdayCheckBox?.isChecked = false
            mTodoRepeatDaysBinding?.thursdayCheckbox?.isChecked = false
            mTodoRepeatDaysBinding?.fridayChecbox?.isChecked = false
            mTodoRepeatDaysBinding?.saturdayCheckbox?.isChecked = false
            mTodoRepeatDaysBinding?.sundayCheckbox?.isChecked = false
        }
        days?.forEach { day->
            if(day == requireContext().resources.getString(R.string.monday)){
                mTodoRepeatDaysBinding?.mondayCheckBox?.isChecked = true
            }
            if(day == requireContext().resources.getString(R.string.tuesday)){
                mTodoRepeatDaysBinding?.tuesdayCheckBox?.isChecked = true
            }
            if(day == requireContext().resources.getString(R.string.wednesday)){
                mTodoRepeatDaysBinding?.wednesdayCheckBox?.isChecked = true
            }
            if(day == requireContext().resources.getString(R.string.thursday)){
                mTodoRepeatDaysBinding?.thursdayCheckbox?.isChecked = true
            }
            if(day == requireContext().resources.getString(R.string.friday)){
                mTodoRepeatDaysBinding?.fridayChecbox?.isChecked = true
            }
            if(day == requireContext().resources.getString(R.string.saturday)){
                mTodoRepeatDaysBinding?.saturdayCheckbox?.isChecked = true
            }
            if(day == requireContext().resources.getString(R.string.sunday)){
                mTodoRepeatDaysBinding?.sundayCheckbox?.isChecked = true
            }
        }
    }

    private fun getRepeatDayNumber():Int{
        val repeatText =  mTodoRepeatDaysBinding?.numberOfDays?.text.toString().trim()
        if(repeatText.isEmpty()){
            return 0
        }

        return repeatText.toInt()
    }
    private fun getAllCheckedDays(): List<String>{

        val selectedDays = mutableListOf<String>()

        if(mTodoRepeatDaysBinding?.mondayCheckBox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.monday))
        }
        if(mTodoRepeatDaysBinding?.tuesdayCheckBox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.tuesday))
        }
        if(mTodoRepeatDaysBinding?.wednesdayCheckBox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.wednesday))
        }
        if(mTodoRepeatDaysBinding?.thursdayCheckbox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.thursday))
        }
        if(mTodoRepeatDaysBinding?.fridayChecbox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.friday))
        }
        if(mTodoRepeatDaysBinding?.saturdayCheckbox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.saturday))
        }
        if(mTodoRepeatDaysBinding?.sundayCheckbox!!.isChecked){
            selectedDays.add(requireContext().resources.getString(R.string.sunday))
        }
        return selectedDays
    }
    fun setOnNumberOfDaysSetListener(onPositiveListener:OnNumberOfDaysSetListener){
        mOnNumberOfDaysSetListener = onPositiveListener

    }
    interface OnNumberOfDaysSetListener{
        fun numberOfDays(repeat:Int, days:List<String>)
    }
}