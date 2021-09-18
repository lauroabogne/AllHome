package com.example.allhome.bill

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.BillCustomDateRangeDialogFragmentBinding
import java.text.SimpleDateFormat
import java.util.*

class BillCustomDateRangeDialogFragment: DialogFragment() {

    lateinit var mBillCustomDateRangeDialogFragmentBinding:BillCustomDateRangeDialogFragmentBinding
    var mOnClickListener: View.OnClickListener? = null
    var mStartDate:Calendar? = null
    var mEndDate:Calendar? = null

    companion object{
        const val START_DATE_REQUEST = 0
        const val END_DATE_REQUEST = 1

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mBillCustomDateRangeDialogFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.bill_custom_date_range_dialog_fragment,null,false)
        mBillCustomDateRangeDialogFragmentBinding.startDateTextInputEditText.setOnClickListener {
            showCalendar(START_DATE_REQUEST)
        }
        mBillCustomDateRangeDialogFragmentBinding.startDateCalendarImageView.setOnClickListener {
            showCalendar(START_DATE_REQUEST)
        }

        mBillCustomDateRangeDialogFragmentBinding.endDateTextInputEditText.setOnClickListener {
            showCalendar(END_DATE_REQUEST)
        }
        mBillCustomDateRangeDialogFragmentBinding.endDateCalendarImageView.setOnClickListener {
            showCalendar(END_DATE_REQUEST)
        }


        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Custom date range")
        alertDialogBuilder.setNegativeButton("Cancel",null)
        alertDialogBuilder.setPositiveButton("Go",null)

        alertDialogBuilder.setView(mBillCustomDateRangeDialogFragmentBinding.root)
        val alertDialog = alertDialogBuilder.create()

        alertDialog.setOnShowListener({
            val positveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positveButton?.let{
                it.setOnClickListener(mOnClickListener)
            }
        })

        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return alertDialog
    }
    fun showCalendar(dateTypeRequest:Int){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val readableDate = SimpleDateFormat("MMMM dd,yyyy").format(date)


            if(dateTypeRequest == START_DATE_REQUEST){
                val startDate = SimpleDateFormat("MMMM dd,yyyy").parse(readableDate)
                val startDateCalendar = Calendar.getInstance()
                startDateCalendar.time = startDate
                mStartDate = startDateCalendar

                mBillCustomDateRangeDialogFragmentBinding.startDateTextInputEditText.setText(readableDate)

            }else if(dateTypeRequest == END_DATE_REQUEST){
                Toast.makeText(requireContext(),"END_DATE_REQUEST",Toast.LENGTH_SHORT).show()
                val endDate = SimpleDateFormat("MMMM dd,yyyy").parse(readableDate)
                val endDateCalendar = Calendar.getInstance()
                endDateCalendar.time = endDate
                mEndDate = endDateCalendar

                mBillCustomDateRangeDialogFragmentBinding.endDateTextInputEditText.setText(readableDate)
            }
        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }

}