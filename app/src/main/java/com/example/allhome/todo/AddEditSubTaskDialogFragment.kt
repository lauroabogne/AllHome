package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.data.entities.TodoChecklistEntity
import com.example.allhome.databinding.AddSubtaskDialogFragmentBinding




class AddEditSubTaskDialogFragment(val onSubTaskSavedListener:OnSubTaskSavedListener? = null, val todoSubTasksEntity:TodoChecklistEntity? = null): DialogFragment() {

    lateinit var mAddSubtaskDialogFragmentBinding:AddSubtaskDialogFragmentBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mAddSubtaskDialogFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.add_subtask_dialog_fragment,null,false)

        todoSubTasksEntity?.let {
            mAddSubtaskDialogFragmentBinding.subtaskTextinputEdittext.setText(it.name)
        }

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setView(mAddSubtaskDialogFragmentBinding.root)
        alertDialogBuilder.setPositiveButton("Continue", null)
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                alertDialog.dismiss()
                onSubTaskSavedListener?.let {
                    it.onSubTaskSaved(mAddSubtaskDialogFragmentBinding.subtaskTextinputEdittext.text.toString())
                }
            }
        }

        return alertDialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val view: View? = activity!!.currentFocus
        if (view != null) {
            closeSoftKeyboard(view)
        }
    }
    private fun closeSoftKeyboard(view: View) {
            val imm: InputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

    }

    interface OnSubTaskSavedListener {
        fun onSubTaskSaved(subTask:String)
    }

}