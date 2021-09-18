package com.example.allhome.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.databinding.FragmentExpensesBinding
import com.example.allhome.expenses.viewmodel.ExpensesFragmentViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ExpensesFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null


    lateinit var mExpensesFragmentViewModel:ExpensesFragmentViewModel
    lateinit var mFragmentExpensesBinding:FragmentExpensesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        mExpensesFragmentViewModel = ViewModelProvider(this).get(ExpensesFragmentViewModel::class.java)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentExpensesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses,null,false)
        mFragmentExpensesBinding.expensesFragmentViewModel = mExpensesFragmentViewModel
        mFragmentExpensesBinding.fromCalendarImageView.setOnClickListener(fromDateOnClick)
        mFragmentExpensesBinding.fromDateTextInputEditText.setOnClickListener(fromDateOnClick)

        mFragmentExpensesBinding.toDateCalendarImageView.setOnClickListener(toDateOnClick)
        mFragmentExpensesBinding.toDateTextInputEditText.setOnClickListener(toDateOnClick)

        mFragmentExpensesBinding.filterButton.setOnClickListener(filterBtnOnClick)
        getExpenses()
        return mFragmentExpensesBinding.root
    }

    fun getExpenses(){

        mExpensesFragmentViewModel.mCoroutineScope.launch {
            mExpensesFragmentViewModel.getExpenses(requireContext(),SimpleDateFormat("yyyy-MM-dd").format(mExpensesFragmentViewModel.mDateFromFilter.time),SimpleDateFormat("yyyy-MM-dd").format(mExpensesFragmentViewModel.mDateToFilter.time))

            withContext(Main){

                val fromDateReadable = SimpleDateFormat("MMMM dd,yyyy").format(mExpensesFragmentViewModel.mDateFromFilter.time)
                val toDateReadable = SimpleDateFormat("MMMM dd,yyyy").format(mExpensesFragmentViewModel.mDateToFilter.time)

                mFragmentExpensesBinding.fromDateTextInputEditText.setText(fromDateReadable)
                mFragmentExpensesBinding.toDateTextInputEditText.setText(toDateReadable)
                mFragmentExpensesBinding.invalidateAll()
            }
        }
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
            val readableDate = SimpleDateFormat("MMMM d,yyyy").format(date)


            if(dateTypeRequest == BillCustomDateRangeDialogFragment.START_DATE_REQUEST){
                val fromDate = SimpleDateFormat("MMMM d,yyyy").parse(readableDate)
                val fromDateCalendar = Calendar.getInstance()
                fromDateCalendar.time = fromDate
                mExpensesFragmentViewModel.mDateFromFilter  = fromDateCalendar
                mFragmentExpensesBinding.fromDateTextInputEditText.setText(readableDate)

            }else if(dateTypeRequest == BillCustomDateRangeDialogFragment.END_DATE_REQUEST){

                val toDate = SimpleDateFormat("MMMM d,yyyy").parse(readableDate)
                val toDateCalendar = Calendar.getInstance()
                toDateCalendar.time = toDate

                mExpensesFragmentViewModel.mDateToFilter = toDateCalendar
                mFragmentExpensesBinding.toDateTextInputEditText.setText(readableDate)


            }
        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    val fromDateOnClick = object:View.OnClickListener{
        override fun onClick(v: View?) {
            showCalendar(FROM_DATE_REQUEST)
        }

    }

    val toDateOnClick = object:View.OnClickListener{
        override fun onClick(v: View?) {
            showCalendar(TO_DATE_REQUEST)
        }

    }
    val filterBtnOnClick = object :View.OnClickListener{
        override fun onClick(v: View?) {

            Log.e("DATA","XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXDDDD")
            Toast.makeText(requireContext(),"CLicked",Toast.LENGTH_SHORT).show()
            getExpenses()
        }

    }

    companion object {

        const val FROM_DATE_REQUEST = 0
        const val TO_DATE_REQUEST = 1


        @JvmStatic fun newInstance(param1: String, param2: String) =
            ExpensesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}