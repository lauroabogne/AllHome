package com.example.allhome.expenses

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.data.DAO.ExpensesGroceryItemDAO
import com.example.allhome.data.DAO.ExpensesGroceryListDAO
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import com.example.allhome.databinding.ExpensesItemLayoutBinding
import com.example.allhome.databinding.FragmentExpensesSummaryItemsBinding
import com.example.allhome.expenses.viewmodel.ExpensesSummaryByGroceryItemsViewModel
import com.example.allhome.expenses.viewmodel.ExpensesSummaryByGroceryItemsViewModelFactory
import com.example.allhome.utils.NumberUtils
import java.util.*


private const val FROM_CALENDAR_PARAM = "FROM_CALENDAR_PARAM"
private const val TO_CALENDAR_PARAM = "TO_CALENDAR_PARAM"
private const val TAG = "ExpensesSummaryByGroceryItemsFragment"

class ExpensesSummaryByGroceryItemsFragment : Fragment() {

    companion object {
        @JvmStatic fun newInstance(fromCalendar: Calendar, toCalendar: Calendar) =
            ExpensesSummaryByGroceryItemsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TO_CALENDAR_PARAM, toCalendar)
                    putSerializable(FROM_CALENDAR_PARAM, fromCalendar)
                }
            }
    }

    private val mExpensesSummaryByGroceryItemsViewModel: ExpensesSummaryByGroceryItemsViewModel by viewModels {
        val expensesGroceryListDAO: ExpensesGroceryListDAO = (context?.applicationContext as AllHomeBaseApplication).expensesGroceryListDAO
        val expensesGroceryItemDAO: ExpensesGroceryItemDAO = (context?.applicationContext as AllHomeBaseApplication).expensesGroceryItemDAO
        ExpensesSummaryByGroceryItemsViewModelFactory(expensesGroceryListDAO, expensesGroceryItemDAO)
    }
    private lateinit var mFragmentExpensesSummaryItemsBinding: FragmentExpensesSummaryItemsBinding




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentExpensesSummaryItemsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses_summary_items, container, false)

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentExpensesSummaryItemsBinding.expensesItemRecyclerView.addItemDecoration(decorator)

        val expensesSummaryByGroceryItemsRecyclerviewViewAdapter = ExpensesSummaryByGroceryItemsRecyclerviewViewAdapter(arrayListOf())
        mFragmentExpensesSummaryItemsBinding.expensesItemRecyclerView.adapter = expensesSummaryByGroceryItemsRecyclerviewViewAdapter

        return mFragmentExpensesSummaryItemsBinding.root
    }

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var toCalendar = Calendar.getInstance()
        var fromCalender = Calendar.getInstance()
        arguments?.let {
            toCalendar = it.getSerializable(TO_CALENDAR_PARAM) as Calendar
            fromCalender = it.getSerializable(FROM_CALENDAR_PARAM) as Calendar

        }
        mExpensesSummaryByGroceryItemsViewModel.mLoadData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { loadData ->
            if (!loadData) {
                return@Observer
            }
            mExpensesSummaryByGroceryItemsViewModel.getTotalExpensesByDateRange()
            mExpensesSummaryByGroceryItemsViewModel.getExpensesGroceryItems()
        })

        mExpensesSummaryByGroceryItemsViewModel.mGroceryItemExpense.observe(viewLifecycleOwner, androidx.lifecycle.Observer {expensesEntitiesWithItemNameAndType->
            val expensesSummaryByGroceryItemsRecyclerviewViewAdapter = mFragmentExpensesSummaryItemsBinding.expensesItemRecyclerView.adapter as ExpensesSummaryByGroceryItemsRecyclerviewViewAdapter
            expensesSummaryByGroceryItemsRecyclerviewViewAdapter.expensesEntitiesWithItemNameAndType = expensesEntitiesWithItemNameAndType as ArrayList<ExpensesEntityWithItemNameAndType>
            expensesSummaryByGroceryItemsRecyclerviewViewAdapter.notifyDataSetChanged()
        })

        mExpensesSummaryByGroceryItemsViewModel.mTotalGroceryItemExpenses.observe(viewLifecycleOwner, androidx.lifecycle.Observer {totalExpenses->
            mFragmentExpensesSummaryItemsBinding.totalExpensesTextView.text = NumberUtils.formatNumber(totalExpenses)
        })

        mExpensesSummaryByGroceryItemsViewModel.mDateToFilter = toCalendar
        mExpensesSummaryByGroceryItemsViewModel.mDateFromFilter = fromCalender
        mExpensesSummaryByGroceryItemsViewModel.mLoadData.value = true
    }


    inner class ExpensesSummaryByGroceryItemsRecyclerviewViewAdapter(var expensesEntitiesWithItemNameAndType: ArrayList<ExpensesEntityWithItemNameAndType>) : RecyclerView.Adapter<ExpensesSummaryByGroceryItemsRecyclerviewViewAdapter.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)


            val expensesItemLayoutBinding = ExpensesItemLayoutBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(expensesItemLayoutBinding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val expensesEntityWithItemNameAndType = expensesEntitiesWithItemNameAndType[position]
            holder.expensesItemLayoutBinding.itemNameTextView.text = expensesEntityWithItemNameAndType.item_name
            holder.expensesItemLayoutBinding.amountTextView.text = NumberUtils.formatNumber(expensesEntityWithItemNameAndType.expensesEntity.amount)

        }

        override fun getItemCount(): Int {
            return expensesEntitiesWithItemNameAndType.size
        }

        inner class ItemViewHolder(var expensesItemLayoutBinding: ExpensesItemLayoutBinding) : RecyclerView.ViewHolder(expensesItemLayoutBinding.root), View.OnClickListener {
            override fun onClick(v: View?) {

                val month = v!!.tag as String

                val intent = Intent(requireContext(), ExpensesItemSummaryActivity::class.java)
                //intent.putExtra(BillActivity.TITLE_TAG,"Create bill payment")
                //intent.putExtra(BillActivity.WHAT_FRAGMENT, BillActivity.ADD_BILL_FRAGMENT)
                requireContext().startActivity(intent)

            }


        }
    }

}