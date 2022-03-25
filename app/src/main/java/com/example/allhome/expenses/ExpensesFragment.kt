package com.example.allhome.expenses

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.databinding.ExpensesMonthlyItemBinding
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
        requireActivity().title = "Expenses"


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        mFragmentExpensesBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses,null,false)
        mFragmentExpensesBinding.expensesFragmentViewModel = mExpensesFragmentViewModel
        mFragmentExpensesBinding.fromCalendarImageView.setOnClickListener(fromDateOnClick)
        mFragmentExpensesBinding.fromDateTextInputEditText.setOnClickListener(fromDateOnClick)

        mFragmentExpensesBinding.toDateCalendarImageView.setOnClickListener(toDateOnClick)
        mFragmentExpensesBinding.toDateTextInputEditText.setOnClickListener(toDateOnClick)
        mFragmentExpensesBinding.filterButton.setOnClickListener(filterBtnOnClick)
        mFragmentExpensesBinding.filterAmountTextView.setOnClickListener {
            val intent = Intent(requireContext(), ExpensesItemSummaryActivity::class.java)
            intent.putExtra(ExpensesItemSummaryActivity.DATE_FROM_TAG, mExpensesFragmentViewModel.mDateFromFilter)
            intent.putExtra(ExpensesItemSummaryActivity.DATE_TO_TAG, mExpensesFragmentViewModel.mDateToFilter)
            requireContext().startActivity(intent)
        }
        mFragmentExpensesBinding.currentYearExpensesTextView.setOnClickListener {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val fromCalendar = Calendar.getInstance()
            fromCalendar.set(Calendar.YEAR, currentYear)
            fromCalendar.set(Calendar.MONTH, 0)
            fromCalendar.set(Calendar.DAY_OF_MONTH, 1)

            val toCalendar = Calendar.getInstance()
            toCalendar.set(Calendar.YEAR, currentYear)
            toCalendar.set(Calendar.MONTH, 11)
            toCalendar.set(Calendar.DAY_OF_MONTH, 31)

            val intent = Intent(requireContext(), ExpensesItemSummaryActivity::class.java)
            intent.putExtra(ExpensesItemSummaryActivity.DATE_FROM_TAG, fromCalendar)
            intent.putExtra(ExpensesItemSummaryActivity.DATE_TO_TAG, toCalendar)
            requireContext().startActivity(intent)
        }


        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentExpensesBinding.monthlyExpensesRecyclerview.addItemDecoration(decorator)

        val monthlyExpensesRecyclerviewViewAdapter = MonthlyExpensesRecyclerviewViewAdapter(arrayListOf())
        mFragmentExpensesBinding.monthlyExpensesRecyclerview.adapter = monthlyExpensesRecyclerviewViewAdapter

        getFilteredExpenses()
        getExpensePerMonthAndCurrentYear()
        return mFragmentExpensesBinding.root
    }

    fun getFilteredExpenses(){

        mExpensesFragmentViewModel.mCoroutineScope.launch {

            mExpensesFragmentViewModel.getExpenses(requireContext(),SimpleDateFormat("yyyy-MM-dd").format(mExpensesFragmentViewModel.mDateFromFilter.time),SimpleDateFormat("yyyy-MM-dd").format(mExpensesFragmentViewModel.mDateToFilter.time))

            withContext(Main){

                val fromDateReadable = SimpleDateFormat("MMMM d,yyyy").format(mExpensesFragmentViewModel.mDateFromFilter.time)
                val toDateReadable = SimpleDateFormat("MMMM d,yyyy").format(mExpensesFragmentViewModel.mDateToFilter.time)

                mFragmentExpensesBinding.fromDateTextInputEditText.setText(fromDateReadable)
                mFragmentExpensesBinding.toDateTextInputEditText.setText(toDateReadable)
                mFragmentExpensesBinding.invalidateAll()
            }
        }
    }
    fun getExpensePerMonthAndCurrentYear(){

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)


        val fromDate = "${currentYear}-01-01"
        val toDate = "${currentYear}-12-31"

        mExpensesFragmentViewModel.mCoroutineScope.launch {

            val calendar = Calendar.getInstance()


            repeat(12){index->
                calendar.set(Calendar.MONTH,index)
                val month = SimpleDateFormat("yyyy-MM").format(calendar.time)

                val expensesEntity = mExpensesFragmentViewModel.getExpensesByMonth(requireContext(),month)



                if(expensesEntity !=null){
                    mExpensesFragmentViewModel.mExpensesPerMonth.add(expensesEntity)
                }else{
                    mExpensesFragmentViewModel.mExpensesPerMonth.add(ExpensesEntity(month,0.0))
                }
            }
            mExpensesFragmentViewModel.getCurrentYearExpenses(requireContext(),fromDate,toDate)


            withContext(Main){

                val monthlyExpensesRecyclerviewViewAdapter = mFragmentExpensesBinding.monthlyExpensesRecyclerview.adapter as MonthlyExpensesRecyclerviewViewAdapter
                monthlyExpensesRecyclerviewViewAdapter.monthlyExpenses =mExpensesFragmentViewModel.mExpensesPerMonth
                monthlyExpensesRecyclerviewViewAdapter.notifyDataSetChanged()
                mFragmentExpensesBinding.invalidateAll()


            }
        }
    }
    fun generateMonths(){

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

            Toast.makeText(requireContext(),"CLicked",Toast.LENGTH_SHORT).show()
            getFilteredExpenses()
        }

    }

    inner class MonthlyExpensesRecyclerviewViewAdapter(var monthlyExpenses:ArrayList<ExpensesEntity>): RecyclerView.Adapter<MonthlyExpensesRecyclerviewViewAdapter.ItemViewHolder>() {
        val calendar = Calendar.getInstance()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val expensesMonthlyItemBinding = ExpensesMonthlyItemBinding.inflate(layoutInflater, parent, false)
            val itemHolder = ItemViewHolder(expensesMonthlyItemBinding)

            return itemHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.setIsRecyclable(false);
            calendar.set(Calendar.MONTH,position)

            val readableDate = SimpleDateFormat("MMMM").format(calendar.time)
            val expensesEntity = monthlyExpenses[position]

            holder.expensesMonthlyItemBinding.month = readableDate
            holder.expensesMonthlyItemBinding.expensesEntity = expensesEntity
            holder.expensesMonthlyItemBinding.root.tag = expensesEntity.expenseDate
            holder.expensesMonthlyItemBinding.root.setOnClickListener(holder)
            holder.expensesMonthlyItemBinding.executePendingBindings()
        }

        override fun getItemCount(): Int {
            return monthlyExpenses.size
        }
        inner class  ItemViewHolder(var expensesMonthlyItemBinding: ExpensesMonthlyItemBinding, ): RecyclerView.ViewHolder(expensesMonthlyItemBinding.root),View.OnClickListener{
            override fun onClick(v: View?) {

                val month = v!!.tag as String
                val splitted = month.split("-")
                val yearInt = splitted[0].toInt()
                val monthInt = splitted[1].toInt()


                val startDateCalendar = Calendar.getInstance()
                startDateCalendar.set(Calendar.YEAR,yearInt)
                startDateCalendar.set(Calendar.MONTH,monthInt - 1)// MONTH START AT 0 as JANUARY
                startDateCalendar.set(Calendar.DAY_OF_MONTH,1)


                val endDateCalendar = startDateCalendar.clone() as Calendar
                endDateCalendar.set(Calendar.DAY_OF_MONTH,startDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))


                val intent = Intent(requireContext(), ExpensesItemSummaryActivity::class.java)
                intent.putExtra(ExpensesItemSummaryActivity.DATE_FROM_TAG,startDateCalendar)
                intent.putExtra(ExpensesItemSummaryActivity.DATE_TO_TAG, endDateCalendar)
                requireContext().startActivity(intent)

            }


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