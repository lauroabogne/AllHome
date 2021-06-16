package com.example.allhome.global_ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.MessageLayoutBinding


class CustomMessageDialogFragment(val title:String?,val message:String,val isErrorMessage:Boolean): DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val messageLayoutBinding:MessageLayoutBinding = DataBindingUtil.inflate(inflater,R.layout.message_layout,null,false)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)

        title?.let{
            alertDialogBuilder.setTitle(title)
        }

        if(isErrorMessage){
            messageLayoutBinding.messageTextView.setTextColor(Color.RED)
        }
        messageLayoutBinding.messageTextView.text = message
        alertDialogBuilder.setView(messageLayoutBinding.root)
        alertDialogBuilder.setPositiveButton("Close", DialogInterface.OnClickListener { dialog, which ->
            // on success
        })

        return alertDialogBuilder.create()
    }



}