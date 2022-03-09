package com.example.allhome.global_ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.allhome.R

class CustomConfirmationDialog(context: Context) : AlertDialog.Builder(context) {

    companion object{
        val POSITIVE_BUTTON_ID = AlertDialog.BUTTON_POSITIVE
        val NEGATIVE_BUTTON_ID = AlertDialog.BUTTON_NEGATIVE

    }

    //var mMessageTextView: TextView;
    private var mOnClickListener: View.OnClickListener? = null
    lateinit var mAlertDialog: AlertDialog


    fun setCustomMessage(message:String){
        setMessage(message)

    }
    fun setButtonClickListener(onClickListener: View.OnClickListener) {
        mOnClickListener = onClickListener
    }
    fun createPositiveButton(buttonLabel: String){
        this.setPositiveButton(buttonLabel, null)
    }

    fun createNegativeButton(buttonLabel: String){
        this.setNegativeButton(buttonLabel, null)
    }


    override fun show(): AlertDialog {
        mAlertDialog = super.show()
        val positiveBtn: Button = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeBtn: Button = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        if(mOnClickListener != null){
            if(positiveBtn !=null){
                positiveBtn.id = POSITIVE_BUTTON_ID
                positiveBtn.setOnClickListener(mOnClickListener)
            }

            if(negativeBtn != null){

                negativeBtn.id = NEGATIVE_BUTTON_ID
                negativeBtn.setOnClickListener(mOnClickListener)
            }
        }

        return mAlertDialog
    }
}