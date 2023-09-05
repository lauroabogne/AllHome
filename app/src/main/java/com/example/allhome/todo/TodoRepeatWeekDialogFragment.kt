package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.TodoRepeatWeeksBinding

class TodoRepeatWeekDialogFragment(val repeatWeekNumberValue:Int?): DialogFragment() {

    private var mTodoRepeatWeeksBinding:TodoRepeatWeeksBinding? = null
    private var mOnNumberOfWeeksSetListener:OnNumberOfWeeksSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = requireActivity().layoutInflater
        mTodoRepeatWeeksBinding = DataBindingUtil.inflate(inflater, R.layout.todo_repeat_weeks,null,false)
        initUI()

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(mTodoRepeatWeeksBinding?.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Continue"){dialog, _ ->

            }

        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveBtn.setOnClickListener {
                //mTodoRepeatWeeksBinding.daysRadioGroup.checkedRadioButtonId
               // mOnNumberOfDaysSetListener?.numberOfDays(mTodoRepeatDaysBinding?.numberOfDays?.text.toString().trim())
                val repeatWeekNumber = getRepeatWeekNumber()

                mOnNumberOfWeeksSetListener?.numberOfWeeksAndDays(repeatWeekNumber)

            }
        }


        return dialog
    }

    private fun initUI(){
        if(repeatWeekNumberValue != null && repeatWeekNumberValue > 0){
            mTodoRepeatWeeksBinding?.repeatTextInputEditText?.setText("$repeatWeekNumberValue")
        }
    }
     fun setOnNumberOfWeeksSetListener( onNumberOfWeeksSetListener:OnNumberOfWeeksSetListener){
        mOnNumberOfWeeksSetListener = onNumberOfWeeksSetListener
    }
    private fun getRepeatWeekNumber():Int{
       val repeatText =  mTodoRepeatWeeksBinding?.repeatTextInputEditText!!.text.toString().trim()
        if(repeatText.isEmpty()){
            return 0
        }

        return repeatText.toInt()
    }





    interface OnNumberOfWeeksSetListener{
        fun numberOfWeeksAndDays(repeat:Int)
    }
}