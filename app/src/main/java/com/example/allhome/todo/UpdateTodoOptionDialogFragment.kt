package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.R.layout.delete_todo_dialog_fragment_layout
import com.example.allhome.databinding.DeleteTodoDialogFragmentLayoutBinding

class UpdateTodoOptionDialogFragment(var title:String, var message:String): DialogFragment()  {

    var mOnClickListener:View.OnClickListener? = null
    var mDeleteTodoDialogFragmentLayoutBinding:DeleteTodoDialogFragmentLayoutBinding?  = null
    companion object{
        const val POSITIVE_BTN_ID = 1986
        const val NEGATIVE_BTN_ID = 1985
    }
    fun setClickListener(onClickListener:View.OnClickListener ){
        mOnClickListener = onClickListener

    }
    fun getDeleteTodoDialogFragmentLayoutBinding(): DeleteTodoDialogFragmentLayoutBinding? {
        return mDeleteTodoDialogFragmentLayoutBinding
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mDeleteTodoDialogFragmentLayoutBinding = DataBindingUtil.inflate(inflater, delete_todo_dialog_fragment_layout,null,false)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)

        title?.let{
            alertDialogBuilder.setTitle(title)
        }

        mDeleteTodoDialogFragmentLayoutBinding?.messageTextView?.text = message
        alertDialogBuilder.setView(mDeleteTodoDialogFragmentLayoutBinding?.root)

        mDeleteTodoDialogFragmentLayoutBinding?.selectedTaskOnlyBtn?.visibility = View.VISIBLE
        mDeleteTodoDialogFragmentLayoutBinding?.selectedAndAlsoFutureTaskBtn?.visibility = View.VISIBLE

        mOnClickListener?.let {
            mDeleteTodoDialogFragmentLayoutBinding?.selectedTaskOnlyBtn?.setOnClickListener(it)
            mDeleteTodoDialogFragmentLayoutBinding?.selectedAndAlsoFutureTaskBtn?.setOnClickListener(it)
        }

        alertDialogBuilder.setNegativeButton("Cancel", null)
        alertDialogBuilder.setPositiveButton("Continue", null)

        val dialog = alertDialogBuilder.create()
        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

            positiveBtn.id = POSITIVE_BTN_ID
            negativeBtn.id = NEGATIVE_BTN_ID

            mOnClickListener?.let {
                positiveBtn.setOnClickListener(it)
                negativeBtn.setOnClickListener(it)
            }

        }
        dialog.show()


        return dialog
    }


}