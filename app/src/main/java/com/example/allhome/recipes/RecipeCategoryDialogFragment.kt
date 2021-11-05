package com.example.allhome.recipes

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R

class RecipeCategoryDialogFragment : DialogFragment() {




    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val frameLayout:FrameLayout = inflater.inflate(R.layout.recipe_category_dialog_fragment_layout,null,false) as FrameLayout

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)

        alertDialogBuilder.setView(frameLayout)
        alertDialogBuilder.setPositiveButton("Search", DialogInterface.OnClickListener { dialog, which ->

        })
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })

        val alertDialog = alertDialogBuilder.create()

        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return alertDialog
    }

}