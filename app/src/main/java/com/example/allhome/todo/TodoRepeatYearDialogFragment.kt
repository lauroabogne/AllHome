package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.TodoRepeatMonthsBinding
import com.example.allhome.databinding.TodoRepeatYearBinding

class TodoRepeatYearDialogFragment(val repeat: Int?): DialogFragment() {

    private var mTodoRepeatYearBinding: TodoRepeatYearBinding? = null
    private var mOnNumberOfYearSetListener:OnNumberOfYearSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater
        mTodoRepeatYearBinding = DataBindingUtil.inflate(inflater, R.layout.todo_repeat_year,null,false)
        initUI()


        builder.setView(mTodoRepeatYearBinding?.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Continue",null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveBtn.setOnClickListener {
                mOnNumberOfYearSetListener?.numberOfYear(getRepeatNumberOfMonths())
            }
        }


        return dialog
    }
    private fun initUI(){

        if(repeat !=null && repeat > 0){
            mTodoRepeatYearBinding?.numberOfYearTextInputEditText?.setText("$repeat")
        }

    }

    fun setOnNumberOfYearSetListener(onNumberOfMonthsSetListener:OnNumberOfYearSetListener){
        mOnNumberOfYearSetListener = onNumberOfMonthsSetListener
    }
    private fun getRepeatNumberOfMonths():Int{
        val repeatText = mTodoRepeatYearBinding?.numberOfYearTextInputEditText?.text.toString().trim()

        if(repeatText.isEmpty()){
            return 0
        }

        return repeatText.toInt()
    }

    interface OnNumberOfYearSetListener{
        fun numberOfYear(repeat:Int)
    }
}