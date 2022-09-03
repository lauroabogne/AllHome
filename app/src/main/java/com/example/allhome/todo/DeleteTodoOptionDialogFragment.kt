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
import com.example.allhome.databinding.DeleteTodoDialogFragmentLayoutBinding

class DeleteTodoOptionDialogFragment(var title:String, var message:String): DialogFragment()  {

    var mOnClickListener:View.OnClickListener? = null
    companion object{
        const val POSITIVE_BTN_ID = 1986
    }
    fun setClickListener(onClickListener:View.OnClickListener ){
        mOnClickListener = onClickListener

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val deleteTodoDialogFragmentLayoutBinding: DeleteTodoDialogFragmentLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.delete_todo_dialog_fragment_layout,null,false)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)

        title?.let{
            alertDialogBuilder.setTitle(title)
        }

        deleteTodoDialogFragmentLayoutBinding.messageTextView.text = message
        alertDialogBuilder.setView(deleteTodoDialogFragmentLayoutBinding.root)

        deleteTodoDialogFragmentLayoutBinding.selectedTaskOnlyBtn.visibility = View.VISIBLE
        deleteTodoDialogFragmentLayoutBinding.selectedAndAlsoFutureTaskBtn.visibility = View.VISIBLE

        mOnClickListener?.let {
            deleteTodoDialogFragmentLayoutBinding.selectedTaskOnlyBtn.setOnClickListener(it)
            deleteTodoDialogFragmentLayoutBinding.selectedAndAlsoFutureTaskBtn.setOnClickListener(it)
        }


        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                // on success
        })



        val dialog = alertDialogBuilder.create()
        dialog.show()


        return dialog
    }


}