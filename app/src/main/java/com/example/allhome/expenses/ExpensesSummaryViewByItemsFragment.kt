package com.example.allhome.expenses

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import com.example.allhome.databinding.ExpensesMonthlyItemBinding
import com.example.allhome.databinding.ExpensesSummaryPerItemLayoutBinding
import com.example.allhome.databinding.FragmentExpensesSummaryViewByItemsBinding
import com.example.allhome.expenses.viewmodel.ExpensesFragmentViewModel
import com.example.allhome.expenses.viewmodel.ExpensesSummaryViewByItemsFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val FROM_CALENDAR_PARAM = "FROM_CALENDAR_PARAM"
private const val TO_CALENDAR_PARAM = "TO_CALENDAR_PARAM"


class ExpensesSummaryViewByItemsFragment : Fragment() {

    lateinit var mExpensesSummaryViewByItemsFragmentViewModel:ExpensesSummaryViewByItemsFragmentViewModel
    lateinit var mFragmentExpensesSummaryViewByItemsBinding:FragmentExpensesSummaryViewByItemsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mExpensesSummaryViewByItemsFragmentViewModel = ViewModelProvider(this).get(ExpensesSummaryViewByItemsFragmentViewModel::class.java)

        arguments?.let {
            mExpensesSummaryViewByItemsFragmentViewModel.mDateFromFilter = it.getSerializable(FROM_CALENDAR_PARAM) as Calendar
            mExpensesSummaryViewByItemsFragmentViewModel.mDateToFilter  = it.getSerializable(TO_CALENDAR_PARAM)  as Calendar
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentExpensesSummaryViewByItemsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses_summary_view_by_items,null,false)
        mFragmentExpensesSummaryViewByItemsBinding.expensesSummaryViewByItemsFragmentViewModel = mExpensesSummaryViewByItemsFragmentViewModel

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentExpensesSummaryViewByItemsBinding.expensesItemRecyclerView.addItemDecoration(decorator)

        val expensesPerItemRecyclerviewViewAdapter = ExpensesPerItemRecyclerviewViewAdapter(arrayListOf())
        mFragmentExpensesSummaryViewByItemsBinding.expensesItemRecyclerView.adapter = expensesPerItemRecyclerviewViewAdapter

        getFilteredExpenses()
        return mFragmentExpensesSummaryViewByItemsBinding.root
    }
    fun getFilteredExpenses(){

        val fromDate = SimpleDateFormat("yyyy-MM-dd").format(mExpensesSummaryViewByItemsFragmentViewModel.mDateFromFilter.time)
        val toDate = SimpleDateFormat("yyyy-MM-dd").format(mExpensesSummaryViewByItemsFragmentViewModel.mDateToFilter.time)

        mExpensesSummaryViewByItemsFragmentViewModel.mCoroutineScope.launch {

            mExpensesSummaryViewByItemsFragmentViewModel.getExpenses(requireContext(),fromDate,toDate)
            mExpensesSummaryViewByItemsFragmentViewModel.getExpensesWithItemNameAndType(requireContext(), fromDate , toDate)

             withContext(Dispatchers.Main){

                 val monthlyExpensesRecyclerviewViewAdapter = mFragmentExpensesSummaryViewByItemsBinding.expensesItemRecyclerView.adapter as ExpensesPerItemRecyclerviewViewAdapter
                 monthlyExpensesRecyclerviewViewAdapter.expensesEntitiesWithItemNameAndType = mExpensesSummaryViewByItemsFragmentViewModel.mExpensesEntityWithItemNameAndType as ArrayList<ExpensesEntityWithItemNameAndType>
                 monthlyExpensesRecyclerviewViewAdapter.notifyDataSetChanged()
                 mFragmentExpensesSummaryViewByItemsBinding.invalidateAll()


            }
        }
    }

    inner class ExpensesPerItemRecyclerviewViewAdapter(var expensesEntitiesWithItemNameAndType: ArrayList<ExpensesEntityWithItemNameAndType>): RecyclerView.Adapter<ExpensesPerItemRecyclerviewViewAdapter.ItemViewHolder>() {
        val calendar = Calendar.getInstance()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val expensesSummaryPerItemLayoutBinding = ExpensesSummaryPerItemLayoutBinding.inflate(layoutInflater, parent, false)
            val itemHolder = ItemViewHolder(expensesSummaryPerItemLayoutBinding)

            return itemHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.setIsRecyclable(false);

            val expensesEntityWithItemNameAndType = expensesEntitiesWithItemNameAndType[position]

           holder.expensesSummaryPerItemLayoutBinding.expensesEntityWithItemNameAndType = expensesEntityWithItemNameAndType

           holder.expensesSummaryPerItemLayoutBinding.executePendingBindings()
        }

        override fun getItemCount(): Int {
            return expensesEntitiesWithItemNameAndType.size
        }
        inner class  ItemViewHolder(var expensesSummaryPerItemLayoutBinding: ExpensesSummaryPerItemLayoutBinding, ): RecyclerView.ViewHolder(expensesSummaryPerItemLayoutBinding.root),View.OnClickListener{
            override fun onClick(v: View?) {

                val month = v!!.tag as String

                val intent = Intent(requireContext(), ExpensesItemSummaryActivity::class.java)
                //intent.putExtra(BillActivity.TITLE_TAG,"Create bill payment")
                //intent.putExtra(BillActivity.WHAT_FRAGMENT, BillActivity.ADD_BILL_FRAGMENT)
                requireContext().startActivity(intent)

            }


        }
    }


    companion object {
        @JvmStatic fun newInstance(fromCalendar: Calendar, toCalendar: Calendar) =
            ExpensesSummaryViewByItemsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TO_CALENDAR_PARAM, toCalendar)
                    putSerializable(FROM_CALENDAR_PARAM, fromCalendar)
                }
            }
    }
}