package com.example.allhome.expenses

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import com.example.allhome.databinding.ExpensesItemLayoutBinding
import com.example.allhome.databinding.FragmentExpensesSummaryViewByOtherExpensesBinding
import com.example.allhome.expenses.viewmodel.ExpensesSummaryViewByOtherExpensesFragmentViewModel
import com.example.allhome.expenses.viewmodel.ExpensesSummaryViewByOtherExpensesFragmentViewModelFactory
import com.example.allhome.utils.NumberUtils
import java.util.*


private const val FROM_CALENDAR_PARAM = "FROM_CALENDAR_PARAM"
private const val TO_CALENDAR_PARAM = "TO_CALENDAR_PARAM"
private const val TAG = "ExpensesSummaryViewByOtherExpenses"

class ExpensesSummaryViewByOtherExpensesFragment : Fragment() {

    lateinit var mFragmentExpensesSummaryViewByOtherExpenses :FragmentExpensesSummaryViewByOtherExpensesBinding

    private val mExpensesSummaryViewByOtherExpensesFragmentViewModel:ExpensesSummaryViewByOtherExpensesFragmentViewModel by viewModels {
        val expensesDAO: ExpensesDAO = (context?.applicationContext as AllHomeBaseApplication).expensesDAO
        ExpensesSummaryViewByOtherExpensesFragmentViewModelFactory(expensesDAO)
    }

    companion object {

        @JvmStatic fun newInstance(fromCalendar: Calendar, toCalendar: Calendar) =
            ExpensesSummaryViewByOtherExpensesFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(TO_CALENDAR_PARAM, toCalendar)
                    putSerializable(FROM_CALENDAR_PARAM, fromCalendar)

                }
            }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        mFragmentExpensesSummaryViewByOtherExpenses = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses_summary_view_by_other_expenses, container, false)
        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentExpensesSummaryViewByOtherExpenses.expensesItemRecyclerView.addItemDecoration(decorator)

        val expensesSummaryViewByOtherExpensesRecyclerviewViewAdapter = ExpensesSummaryViewByOtherExpensesRecyclerviewViewAdapter(arrayListOf())
        mFragmentExpensesSummaryViewByOtherExpenses.expensesItemRecyclerView.adapter = expensesSummaryViewByOtherExpensesRecyclerviewViewAdapter


        return mFragmentExpensesSummaryViewByOtherExpenses.root
    }

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var toCalendar = Calendar.getInstance()
        var fromCalender = Calendar.getInstance()

        Log.e(TAG,"onViewCreated detected")

        arguments?.let {
            toCalendar = it.getSerializable(TO_CALENDAR_PARAM) as Calendar
            fromCalender = it.getSerializable(FROM_CALENDAR_PARAM) as Calendar

        }


        mExpensesSummaryViewByOtherExpensesFragmentViewModel.mOtherExpenses.observe(viewLifecycleOwner,androidx.lifecycle.Observer{ expensesEntitiesWithItemNameAndType->
            Log.e(TAG,"Changed detected ${expensesEntitiesWithItemNameAndType.size}")
            val expensesSummaryByGroceryItemsRecyclerviewViewAdapter = mFragmentExpensesSummaryViewByOtherExpenses.expensesItemRecyclerView.adapter as ExpensesSummaryViewByOtherExpensesFragment.ExpensesSummaryViewByOtherExpensesRecyclerviewViewAdapter
            expensesSummaryByGroceryItemsRecyclerviewViewAdapter.expensesEntitiesWithItemNameAndType = expensesEntitiesWithItemNameAndType as ArrayList<ExpensesEntityWithItemNameAndType>
            expensesSummaryByGroceryItemsRecyclerviewViewAdapter.notifyDataSetChanged()

        })

        mExpensesSummaryViewByOtherExpensesFragmentViewModel.mLoadData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { loadData ->
            Log.e(TAG,"Loading detected")
            if (!loadData) {
                return@Observer
            }
            mExpensesSummaryViewByOtherExpensesFragmentViewModel.getOtherExpensesItems()
            mExpensesSummaryViewByOtherExpensesFragmentViewModel.getTotalExpensesByDateRange()

        })

        mExpensesSummaryViewByOtherExpensesFragmentViewModel.mTotalOtherExpenses.observe(viewLifecycleOwner, androidx.lifecycle.Observer {totalExpenses->
            mFragmentExpensesSummaryViewByOtherExpenses.totalExpensesTextView.text = NumberUtils.formatNumber(totalExpenses)
        })


        mExpensesSummaryViewByOtherExpensesFragmentViewModel.mDateToFilter = toCalendar
        mExpensesSummaryViewByOtherExpensesFragmentViewModel.mDateFromFilter = fromCalender
        mExpensesSummaryViewByOtherExpensesFragmentViewModel.mLoadData.value = true



    }

    /**
     *
     */
    inner class ExpensesSummaryViewByOtherExpensesRecyclerviewViewAdapter(var expensesEntitiesWithItemNameAndType: ArrayList<ExpensesEntityWithItemNameAndType>) : RecyclerView.Adapter<ExpensesSummaryViewByOtherExpensesRecyclerviewViewAdapter.ItemViewHolder>() {

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