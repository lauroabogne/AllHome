package com.example.allhome.expenses

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
import com.example.allhome.data.DAO.BillPaymentDAO
import com.example.allhome.data.DAO.ExpensesGroceryItemDAO
import com.example.allhome.data.DAO.ExpensesGroceryListDAO
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import com.example.allhome.databinding.ExpensesItemLayoutBinding
import com.example.allhome.databinding.FragmentExpensesSummaryByBillsBinding
import com.example.allhome.expenses.viewmodel.ExpensesSummaryByGroceryItemsViewModel
import com.example.allhome.expenses.viewmodel.ExpensesSummaryByGroceryItemsViewModelFactory
import com.example.allhome.expenses.viewmodel.ExpensesSummaryViewByBillsFragmentViewModel
import com.example.allhome.expenses.viewmodel.ExpensesSummaryViewByBillsFragmentViewModelFactory
import com.example.allhome.utils.NumberUtils
import java.util.*

private const val FROM_CALENDAR_PARAM = "FROM_CALENDAR_PARAM"
private const val TO_CALENDAR_PARAM = "TO_CALENDAR_PARAM"

class ExpensesSummaryViewByBillsFragment : Fragment() {
    private lateinit var mFragmentExpensesSummaryByBillsBinding: FragmentExpensesSummaryByBillsBinding

    private val mExpensesSummaryViewByBillsFragmentViewModel:ExpensesSummaryViewByBillsFragmentViewModel by viewModels {
        val billPaymentDAO: BillPaymentDAO = (context?.applicationContext as AllHomeBaseApplication).billPaymentDAO
        ExpensesSummaryViewByBillsFragmentViewModelFactory(billPaymentDAO)
    }

    companion object {
        @JvmStatic fun newInstance(fromCalendar: Calendar, toCalendar: Calendar) =
            ExpensesSummaryViewByBillsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(FROM_CALENDAR_PARAM,fromCalendar)
                    putSerializable(TO_CALENDAR_PARAM,toCalendar)

                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        mFragmentExpensesSummaryByBillsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expenses_summary_by_bills, container, false)

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentExpensesSummaryByBillsBinding.expensesItemRecyclerView.addItemDecoration(decorator)


        val expensesSummaryByGroceryItemsRecyclerviewViewAdapter = ExpensesItemRecyclerViewRecyclerviewViewAdapter(arrayListOf())
        mFragmentExpensesSummaryByBillsBinding.expensesItemRecyclerView.adapter = expensesSummaryByGroceryItemsRecyclerviewViewAdapter


        return mFragmentExpensesSummaryByBillsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var toCalendar = Calendar.getInstance()
        var fromCalender = Calendar.getInstance()
        arguments?.let {
            toCalendar = it.getSerializable(TO_CALENDAR_PARAM) as Calendar
            fromCalender = it.getSerializable(FROM_CALENDAR_PARAM) as Calendar

        }

        mExpensesSummaryViewByBillsFragmentViewModel.mLoadData.observe(viewLifecycleOwner, androidx.lifecycle.Observer { loadData->
            if( !loadData ){
                return@Observer
            }
            mExpensesSummaryViewByBillsFragmentViewModel.getBillExpenses()
        })
        mExpensesSummaryViewByBillsFragmentViewModel.mBillExpenses.observe(viewLifecycleOwner, androidx.lifecycle.Observer {expensesEntityWithItemNameAndType->
            val expensesItemRecyclerViewRecyclerviewViewAdapter = mFragmentExpensesSummaryByBillsBinding.expensesItemRecyclerView.adapter as ExpensesItemRecyclerViewRecyclerviewViewAdapter
            expensesItemRecyclerViewRecyclerviewViewAdapter.expensesEntitiesWithItemNameAndType = expensesEntityWithItemNameAndType as ArrayList<ExpensesEntityWithItemNameAndType>
            expensesItemRecyclerViewRecyclerviewViewAdapter.notifyDataSetChanged()
        })

        mExpensesSummaryViewByBillsFragmentViewModel.mDateToFilter = toCalendar
        mExpensesSummaryViewByBillsFragmentViewModel.mDateFromFilter = fromCalender
        mExpensesSummaryViewByBillsFragmentViewModel.mLoadData.value = true


    }
    inner class ExpensesItemRecyclerViewRecyclerviewViewAdapter(var expensesEntitiesWithItemNameAndType: ArrayList<ExpensesEntityWithItemNameAndType>) : RecyclerView.Adapter<ExpensesItemRecyclerViewRecyclerviewViewAdapter.ItemViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)

            val expensesItemLayoutBinding = ExpensesItemLayoutBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(expensesItemLayoutBinding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val expensesEntityWithItemNameAndType = expensesEntitiesWithItemNameAndType[position]
            holder.expensesItemLayoutBinding.itemNameTextView.text = expensesEntityWithItemNameAndType.item_name
            holder.expensesItemLayoutBinding.amountTextView.text = NumberUtils.formatNumber(expensesEntityWithItemNameAndType.expensesEntity.totalAmount)

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