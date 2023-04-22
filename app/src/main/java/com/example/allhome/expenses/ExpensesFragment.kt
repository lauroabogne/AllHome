package com.example.allhome.expenses

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.data.DAO.BillDAO
import com.example.allhome.data.DAO.ExpensesGroceryItemDAO
import com.example.allhome.data.DAO.ExpensesGroceryListDAO
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.databinding.ExpensesMonthlyItemBinding
import com.example.allhome.databinding.FragmentExpensesBinding
import com.example.allhome.expenses.viewmodel.ExpensesFragmentViewModel
import com.example.allhome.expenses.viewmodel.ExpensesFragmentViewModelViewModelFactory
import com.example.allhome.expenses.viewmodel.ExpensesSummaryByGroceryItemsViewModel
import com.example.allhome.expenses.viewmodel.ExpensesSummaryByGroceryItemsViewModelFactory
import com.example.allhome.recipes.FilterByInformationDialogFragment
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ExpensesFragment : Fragment() {

    private val TAG = "ExpensesFragment"
    private var param1: String? = null
    private var param2: String? = null


    private val mExpensesFragmentViewModel:ExpensesFragmentViewModel by viewModels{
        val expensesDAO = (context?.applicationContext as AllHomeBaseApplication).expensesDAO
        val billDAO = (context?.applicationContext as AllHomeBaseApplication).billDAO
        ExpensesFragmentViewModelViewModelFactory(expensesDAO,billDAO)
    }
    lateinit var mFragmentExpensesBinding:FragmentExpensesBinding
    var mContext:Context? = null

    private val addExpenseListener = object:AddExpenseDialogFragment.AddExpenseListener{
        override fun onExpenseSet(expenseEntity: ExpensesEntity) {
            mExpensesFragmentViewModel.addExpense(expenseEntity)
        }
    }
    private val savingExpensesObserver = Observer<Boolean> {saveSuccessfully->
        if(saveSuccessfully){
            Toast.makeText(requireContext(),"Save successfully.",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(),"Failed to save.",Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mExpensesFragmentViewModel.mSaveSuccessfully.observe(viewLifecycleOwner,savingExpensesObserver)

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.expenses_fragment_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_expenses_menu->{
                val addExpenseDialogFragment  = AddExpenseDialogFragment(addExpenseListener)
                addExpenseDialogFragment.show(requireActivity().supportFragmentManager,"AddExpenseDialogFragment")
            }
        }
        return true
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

            if(!isAdded){
                return@launch
            }
            val calendar = Calendar.getInstance()
            repeat(12){index->
                calendar.set(Calendar.DAY_OF_MONTH,1)
                calendar.set(Calendar.MONTH,index)
                val month = SimpleDateFormat("yyyy-MM").format(calendar.time)
                val expensesEntity = mExpensesFragmentViewModel.getExpensesByMonth(mContext!!,month)
                if(expensesEntity !=null){
                    mExpensesFragmentViewModel.mExpensesPerMonth.add(expensesEntity)
                }else{
                    mExpensesFragmentViewModel.mExpensesPerMonth.add(ExpensesEntity("","","",month,0.0))
                }
            }

            mExpensesFragmentViewModel.getCurrentYearExpenses(mContext!!,fromDate,toDate)

            withContext(Main){

                val monthlyExpensesRecyclerviewViewAdapter = mFragmentExpensesBinding.monthlyExpensesRecyclerview.adapter as MonthlyExpensesRecyclerviewViewAdapter
                monthlyExpensesRecyclerviewViewAdapter.monthlyExpenses =mExpensesFragmentViewModel.mExpensesPerMonth
                monthlyExpensesRecyclerviewViewAdapter.notifyDataSetChanged()
                mFragmentExpensesBinding.invalidateAll()


            }
        }
    }
    private fun showCalendar(dateTypeRequest:Int){
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
    private val fromDateOnClick = View.OnClickListener { showCalendar(FROM_DATE_REQUEST) }
    private val toDateOnClick = View.OnClickListener { showCalendar(TO_DATE_REQUEST) }
    private val filterBtnOnClick = View.OnClickListener {
        Toast.makeText(requireContext(),"CLicked",Toast.LENGTH_SHORT).show()
        getFilteredExpenses()
    }

    inner class MonthlyExpensesRecyclerviewViewAdapter(var monthlyExpenses:ArrayList<ExpensesEntity>): RecyclerView.Adapter<MonthlyExpensesRecyclerviewViewAdapter.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val expensesMonthlyItemBinding = ExpensesMonthlyItemBinding.inflate(layoutInflater, parent, false)
            val itemHolder = ItemViewHolder(expensesMonthlyItemBinding)

            return itemHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.setIsRecyclable(false);

            val expensesEntity = monthlyExpenses[position]
            val date = SimpleDateFormat("yyyy-MM").parse(expensesEntity.expenseDate)
            val readableDate = SimpleDateFormat("MMMM").format(date)//SimpleDateFormat("MMMM").format(calendar.time)

            holder.expensesMonthlyItemBinding.month = readableDate
            holder.expensesMonthlyItemBinding.expensesEntity = expensesEntity
            holder.expensesMonthlyItemBinding.root.tag = expensesEntity.expenseDate
            holder.expensesMonthlyItemBinding.root.setOnClickListener(holder)
            holder.expensesMonthlyItemBinding.executePendingBindings()
        }

        override fun getItemCount(): Int {
            return monthlyExpenses.size
        }
        inner class  ItemViewHolder(var expensesMonthlyItemBinding: ExpensesMonthlyItemBinding): RecyclerView.ViewHolder(expensesMonthlyItemBinding.root),View.OnClickListener{
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