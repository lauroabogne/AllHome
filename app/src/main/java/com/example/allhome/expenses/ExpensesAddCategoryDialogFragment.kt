package com.example.allhome.expenses

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
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.ExpensesCategoriesEntity
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.databinding.AddExpenseCategoryBinding
import com.example.allhome.databinding.AddExpenseLayoutBinding
import com.example.allhome.expenses.viewmodel.ExpensesCategoriesViewModel
import com.example.allhome.expenses.viewmodel.ExpensesCategoriesViewModelFactory
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.utils.DateUtil
import java.util.Date
import java.util.UUID

class ExpensesAddCategoryDialogFragment(val addExpensesCategoryListener:AddExpensesCategoryListener?) : DialogFragment()  {

    private lateinit var mAddExpenseCategoryBinding: AddExpenseCategoryBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mAddExpenseCategoryBinding = DataBindingUtil.inflate(inflater, R.layout.add_expense_category,null,false)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(mAddExpenseCategoryBinding.root)
        alertDialogBuilder.setPositiveButton("Save",null)
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })


        val alertDialog =  alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setTitle("Add new expense category")
        alertDialog.setOnShowListener{
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                getData()
            }
        }


        return alertDialog
    }

    private fun getData(){

        val expenseCategory = mAddExpenseCategoryBinding.categoryNameTextInputEditText.text.toString().trim()
        val expenseCategoryDescription = mAddExpenseCategoryBinding.categoryDescriptionTextInputEditText.text.toString().trim()

        if(expenseCategory.isEmpty()){
           // Toast.makeText(requireContext(),"Category name must not empty.",Toast.LENGTH_SHORT).show()
            val customMessageDialogFragment =  CustomMessageDialogFragment("Error Message","Category name must not empty.",true)
            customMessageDialogFragment.show(requireActivity().supportFragmentManager,"CustomMessageDialogFragment")
            return
        }

        addExpensesCategoryListener.let {
            val expenseCategoryUniqueId = UUID.randomUUID().toString()
            val currentDatetimeString = DateUtil.getCurrentDateTime()
            val expensesCategoriesEntity = ExpensesCategoriesEntity(expenseCategoryUniqueId, expenseCategory, expenseCategoryDescription, BillEntity.NOT_DELETED_STATUS, BillEntity.NOT_UPLOADED, currentDatetimeString, currentDatetimeString)

            it?.onExpenseCategorySet(expensesCategoriesEntity)
        }

    }

    interface AddExpensesCategoryListener{
        fun onExpenseCategorySet(expensesCategoriesEntity: ExpensesCategoriesEntity)
    }
}