package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.TodoRepeatMonthsBinding

class TodoRepeatMonthDialogFragment(val repeat: Int?): DialogFragment() {

    private var mTodoRepeatMonthsBinding: TodoRepeatMonthsBinding? = null
    private var mOnNumberOfMonthsSetListener:OnNumberOfMonthsSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val inflater = requireActivity().layoutInflater
        mTodoRepeatMonthsBinding = DataBindingUtil.inflate(inflater, R.layout.todo_repeat_months,null,false)

        initUI()

        builder.setView(mTodoRepeatMonthsBinding?.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Continue",null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveBtn.setOnClickListener {
                mOnNumberOfMonthsSetListener?.numberOfMonths(getRepeatNumberOfMonths())
            }
        }


        return dialog
    }

    private fun initUI(){
        if(repeat !=null && repeat > 0){
            mTodoRepeatMonthsBinding?.numberOfMonthTextInputEditText?.setText("$repeat")
        }
    }
    fun setOnNumberOfMonthsSetListener(onNumberOfMonthsSetListener:OnNumberOfMonthsSetListener){
        mOnNumberOfMonthsSetListener = onNumberOfMonthsSetListener
    }
    private fun getRepeatNumberOfMonths():Int{
        val repeatText = mTodoRepeatMonthsBinding?.numberOfMonthTextInputEditText?.text.toString().trim()

        if(repeatText.isEmpty()){
            return 0
        }

        return repeatText.toInt()
    }

    interface OnNumberOfMonthsSetListener{
        fun numberOfMonths(repeat:Int)
    }
}