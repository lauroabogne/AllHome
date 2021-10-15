package com.example.allhome.bill

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.DeleteBillDialogFragmentLayoutBinding
import com.example.allhome.databinding.MessageLayoutBinding

class DeleteBillDialogFragment(var title:String,var message:String,var isRecurringBill:Boolean = false): DialogFragment()  {

    var mOnClickListener:View.OnClickListener? = null
    companion object{
        const val POSITIVE_BTN_ID = 1986
    }
    fun setClickListener(onClickListener:View.OnClickListener ){
        mOnClickListener = onClickListener

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val deleteBillDialogFragmentLayoutBinding: DeleteBillDialogFragmentLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.delete_bill_dialog_fragment_layout,null,false)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)

        title?.let{
            alertDialogBuilder.setTitle(title)
        }

        deleteBillDialogFragmentLayoutBinding.messageTextView.text = message
        alertDialogBuilder.setView(deleteBillDialogFragmentLayoutBinding.root)

        if(isRecurringBill){
            deleteBillDialogFragmentLayoutBinding.deleteSelectedBillOnlyBtn.visibility = View.VISIBLE
            deleteBillDialogFragmentLayoutBinding.deleteAlsoFutureBillBtn.visibility = View.VISIBLE

            mOnClickListener?.let {
                deleteBillDialogFragmentLayoutBinding.deleteSelectedBillOnlyBtn.setOnClickListener(it)
                deleteBillDialogFragmentLayoutBinding.deleteAlsoFutureBillBtn.setOnClickListener(it)
            }


            alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                // on success
            })
        }else{
            alertDialogBuilder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                // on success
            })
            alertDialogBuilder.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                // on success
            })

        }


        val dialog = alertDialogBuilder.create()
        dialog.show()

        if(!isRecurringBill){
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.id = POSITIVE_BTN_ID
            mOnClickListener?.let {
                positiveButton.setOnClickListener(it)
            }

        }
        return dialog
    }


}