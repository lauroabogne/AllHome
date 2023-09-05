package com.example.allhome.todo

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.TodoNotifyBinding

class NotifyAtDialogFragment(private val notifyAt:Int, private val notifyType:String): DialogFragment() {

    private var mOnNotifySetListener:OnNotifySetListener? = null

    private var mTodoNotifyBinding :TodoNotifyBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater

        mTodoNotifyBinding = DataBindingUtil.inflate(inflater, R.layout.todo_notify,null,false)



        mTodoNotifyBinding?.let{

            initUI()
            it.notifyEveryTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    var selectedRepeat = requireContext().resources.getStringArray(R.array.todo_alarm_options)[position]

                    when(selectedRepeat){
                        requireContext().resources.getString(R.string.grocery_notification_none),
                        requireContext().resources.getString(R.string.grocery_notification_same_day_and_time)-> {
                            it.notifyEveryTextInputEditText.setText("")
                            it.notifyAtTextInputLayout.visibility = View.GONE

                        }
                        else ->{
                            it.notifyAtTextInputLayout.visibility = View.VISIBLE
                            it.notifyEveryTextInputEditText.requestFocus()
                            it.notifyEveryTextInputEditText.postDelayed({
                                // Show the soft keyboard
                                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputMethodManager.showSoftInput(it.notifyEveryTextInputEditText, InputMethodManager.SHOW_IMPLICIT)
                            }, 100)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }

        builder.setTitle("Notify At")
        builder.setView(mTodoNotifyBinding?.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Continue",null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

            positiveBtn.setOnClickListener {
                mOnNotifySetListener?.let {
                    it.notifySet(getNotifyEvery(),getNotifyEveryType())
                }
            }
        }


        return dialog
    }

    private fun initUI(){

        mTodoNotifyBinding?.let {

            val adapter: ArrayAdapter<String> = mTodoNotifyBinding?.notifyEveryTypeSpinner?.adapter as ArrayAdapter<String>
            val defaultPosition = adapter.getPosition(notifyType)
            it.notifyEveryTypeSpinner.setSelection(defaultPosition)

            when(notifyType){
                requireContext().resources.getString(R.string.grocery_notification_none),
                requireContext().resources.getString(R.string.grocery_notification_same_day_and_time)-> {
                    it.notifyEveryTextInputEditText.setText("")
                    it.notifyAtTextInputLayout.visibility = View.GONE

                }
                else ->{
                    it.notifyAtTextInputLayout.visibility = View.VISIBLE
                    if(notifyAt >=1 ){
                        it.notifyEveryTextInputEditText.setText("$notifyAt")
                    }

                    it.notifyEveryTextInputEditText.requestFocus()
                    it.notifyEveryTextInputEditText.postDelayed({
                        // Show the soft keyboard
                        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.showSoftInput(it.notifyEveryTextInputEditText, InputMethodManager.SHOW_IMPLICIT)
                    }, 100)
                }
            }
        }


    }
    fun setOnNotifySetListener(onNumberOfMonthsSetListener:OnNotifySetListener){
        mOnNotifySetListener = onNumberOfMonthsSetListener
    }

    fun getNotifyEvery():Int{
        val notifyText = mTodoNotifyBinding?.notifyEveryTextInputEditText?.text.toString().trim()
        if(notifyText.isEmpty()){
            return 0
        }

        return notifyText.toInt()

    }
    fun getNotifyEveryType():String{
        return mTodoNotifyBinding?.notifyEveryTypeSpinner?.selectedItem.toString()
    }
    interface OnNotifySetListener{
        fun notifySet(notifyAt:Int,notifyType:String)
    }
}