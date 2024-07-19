package com.example.allhome.bill

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.example.allhome.R
import com.example.allhome.data.entities.BillCategoryEntity
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.ExpensesCategoriesEntity
import com.example.allhome.databinding.AddBillCategoryBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.utils.DateUtil
import java.util.UUID

class BillAddCategoryDialogFragment(val addBillCategoryListener:AddBillCategoryListener?) : DialogFragment()  {

    private lateinit var mAddBillCategoryBinding: AddBillCategoryBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mAddBillCategoryBinding = DataBindingUtil.inflate(inflater, R.layout.add_bill_category,null,false)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(mAddBillCategoryBinding.root)
        alertDialogBuilder.setPositiveButton("Save",null)
        alertDialogBuilder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            this.dismiss()
        })


        val alertDialog =  alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setTitle("Add new bill category")
        alertDialog.setOnShowListener{
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                getData()
            }
        }


        return alertDialog
    }

    private fun getData(){

        val expenseCategory = mAddBillCategoryBinding.categoryNameTextInputEditText.text.toString().trim()
        val expenseCategoryDescription = mAddBillCategoryBinding.categoryDescriptionTextInputEditText.text.toString().trim()

        if(expenseCategory.isEmpty()){
           // Toast.makeText(requireContext(),"Category name must not empty.",Toast.LENGTH_SHORT).show()
            val customMessageDialogFragment =  CustomMessageDialogFragment("Error Message","Category name must not empty.",true)
            customMessageDialogFragment.show(requireActivity().supportFragmentManager,"CustomMessageDialogFragment")
            return
        }

        addBillCategoryListener.let {
            val billCategoryUniqueId = UUID.randomUUID().toString()
            val currentDatetimeString = DateUtil.getCurrentDateTime()
            val billCategoriesEntity = BillCategoryEntity(billCategoryUniqueId, expenseCategory, expenseCategoryDescription, BillCategoryEntity.NOT_DELETED_STATUS, BillCategoryEntity.NOT_UPLOADED, currentDatetimeString, currentDatetimeString)
            it?.onExpenseCategorySet(billCategoriesEntity)
        }

    }

    interface AddBillCategoryListener{
        fun onExpenseCategorySet(billCategoryEntity: BillCategoryEntity)
    }
}