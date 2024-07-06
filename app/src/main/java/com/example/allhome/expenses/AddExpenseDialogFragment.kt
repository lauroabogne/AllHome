package com.example.allhome.expenses

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.ExpensesCategoriesEntity
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.GroceryItemEntityForAutoSuggest
import com.example.allhome.databinding.AddExpenseLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseDialogFragment(val addExpenseListener:AddExpenseListener, val addExpensesCategoryListener:ExpensesAddCategoryDialogFragment.AddExpensesCategoryListener?): DialogFragment() {

    private lateinit var mAddExpenseLayoutBinding:AddExpenseLayoutBinding
    private var mExpenseDate:Date? = null

    val expensesAddCategoryDialogFragment:ExpensesAddCategoryDialogFragment by lazy {
        ExpensesAddCategoryDialogFragment(addExpensesCategoryListener)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = LayoutInflater.from(requireContext())
        mAddExpenseLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.add_expense_layout,null,false)
        mAddExpenseLayoutBinding.expenseAddCategoryImageView.setOnClickListener {
            expensesAddCategoryDialogFragment.show(requireActivity().supportFragmentManager,"ExpensesAddCategoryDialogFragment")
        }
        val suggestions = listOf("Apple", "Banana", "Orange", "Mango", "Grapes")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suggestions)

        val expensesCategoryAutoSuggestCustomAdapter = ExpensesCategoryAutoSuggestCustomAdapter(requireContext(), arrayListOf())

        mAddExpenseLayoutBinding.expenseCategoryTextField.threshold = 1
        mAddExpenseLayoutBinding.expenseCategoryTextField.setAdapter(expensesCategoryAutoSuggestCustomAdapter)
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
    inner class ExpensesCategoryAutoSuggestCustomAdapter(context: Context,expensesCategories:List<ExpensesCategoriesEntity>):ArrayAdapter<ExpensesCategoriesEntity>(context,0,expensesCategories){
        private var filter  = object: Filter(){
            private var searchJob: Job? = null

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val suggestion =  runBlocking {
                    val results = FilterResults()
                    searchJob = launch(Dispatchers.IO) {
                        val searchTerm = if(constraint == null) "" else constraint.toString()
                        val searchResults = AllHomeDatabase.getDatabase(context).getExpensesCategoriesDAO().searchCategories(searchTerm)
                        results.apply {
                            results.values = searchResults
                            results.count = searchResults.size
                        }
                    }
                    // return the result
                    results
                }
                return suggestion
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                addAll(results.values as ArrayList<ExpensesCategoriesEntity>)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return (resultValue as ExpensesCategoriesEntity).name
            }

        }

        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView: TextView? = if(convertView == null){
                    LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
                }else{
                    convertView as TextView?
                }
            val expensesCategory = getItem(position)
            textView!!.text = expensesCategory?.name

            return textView
        }
    }
}