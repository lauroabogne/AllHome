package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.databinding.AddSubtaskDialogFragmentBinding

class AddSubTaskDialogFragment(val onSubTaskSavedListener:OnSubTaskSavedListener? = null): DialogFragment() {

    lateinit var mAddSubtaskDialogFragmentBinding:AddSubtaskDialogFragmentBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mAddSubtaskDialogFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.add_subtask_dialog_fragment,null,false)

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

    interface OnSubTaskSavedListener {
        fun onSubTaskSaved(subTask:String)
    }

}