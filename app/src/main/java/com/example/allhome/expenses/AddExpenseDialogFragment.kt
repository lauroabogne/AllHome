package com.example.allhome.expenses

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.databinding.AddExpenseLayoutBinding
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseDialogFragment(val addExpenseListener:AddExpenseListener): DialogFragment() {

    private lateinit var mAddExpenseLayoutBinding:AddExpenseLayoutBinding
    private var mExpenseDate:Date? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mAddExpenseLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.add_expense_layout,null,false)
        mAddExpenseLayoutBinding.expenseDateImageView.setOnClickListener {
            showCalendar()
        }
        mAddExpenseLayoutBinding.expenseDateTextInputEditText.setOnClickListener {
            showCalendar()
        }
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(mAddExpenseLayoutBinding.root)
        alertDialogBuilder.setPositiveButton("Continue",null)
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })


        val alertDialog =  alertDialogBuilder.create()
        alertDialog.setTitle("Add new expense record")
        alertDialog.setOnShowListener{
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                getData()
            }
        }


        return alertDialog
    }

    private fun getData(){
        val expenseName:String = mAddExpenseLayoutBinding.expenseNameTextField.text.toString().trim()
        val expenseAmountString:String = mAddExpenseLayoutBinding.expenseAmountTextField.text.toString().trim()
        val expenseCategory = mAddExpenseLayoutBinding.expenseCategoryTextField.text.toString().trim()
        if(expenseName.isEmpty()){
            Toast.makeText(requireContext(),"Please input expense name",Toast.LENGTH_SHORT).show()
            return
        }
        if(expenseAmountString.isEmpty()){
            Toast.makeText(requireContext(),"Please input expense amount.",Toast.LENGTH_SHORT).show()
            return
        }
        if(mExpenseDate  == null){
            Toast.makeText(requireContext(),"Please select expense date.",Toast.LENGTH_SHORT).show()
            return
        }

        addExpenseListener.let {addExpenseListenerParam->
            val expenseUniqueId = UUID.randomUUID().toString()
            val expenseDate = SimpleDateFormat("yyyy-MM-dd").format(mExpenseDate)
            val expensesEntity = ExpensesEntity(expenseUniqueId,expenseName,expenseCategory,expenseDate,expenseAmountString.toDouble())
            addExpenseListenerParam.onExpenseSet(expensesEntity)
        }

    }
    private fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val readableDate = SimpleDateFormat("MMMM d,yyyy").format(date)
            mAddExpenseLayoutBinding.expenseDateTextInputEditText.setText(readableDate)
            mExpenseDate = date
        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }

    interface AddExpenseListener{
        fun onExpenseSet(expenseEntity:ExpensesEntity)
    }
}