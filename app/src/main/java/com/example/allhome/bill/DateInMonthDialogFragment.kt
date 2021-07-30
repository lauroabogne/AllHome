package com.example.allhome.bill

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.databinding.MessageLayoutBinding
import com.example.allhome.meal_planner.AddMealOptionFragment

class DateInMonthDialogFragment: DialogFragment() {

    var mDateSelectedListener:DateSelectedListener? = null
    fun setDateSelectedListener(dateSelectedListener:DateSelectedListener){
        mDateSelectedListener = dateSelectedListener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        val layout =  inflater.inflate(R.layout.date_in_month,null,false)

        val tableLayout = layout.findViewById<TableLayout>(R.id.tableLayout)
        repeat(tableLayout.childCount){
            val tableRow = tableLayout.getChildAt(it) as TableRow

            repeat(tableRow.childCount){elementIndex->
                tableRow.getChildAt(elementIndex).setOnClickListener(calendarTextViewClickListener)
            }
        }

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setTitle("Select date")
        alertDialogBuilder.setView(layout)
        alertDialogBuilder.setPositiveButton("Close", DialogInterface.OnClickListener { dialog, which ->
            // on success
        })

        return alertDialogBuilder.create()
    }
    val calendarTextViewClickListener = object :View.OnClickListener{
        override fun onClick(v: View?) {

            val selectedDate = (v as TextView).text.toString()
            mDateSelectedListener?.let{
                it.dateSelected(selectedDate)
            }
          dismiss()
        }

    }
    interface DateSelectedListener{
        fun dateSelected(date:String)
    }
}