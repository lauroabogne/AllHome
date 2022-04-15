package com.example.allhome.expenses.viewmodel

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType
import com.example.allhome.databinding.ExpensesItemLayoutBinding
import com.example.allhome.expenses.ExpensesItemSummaryActivity
import com.example.allhome.utils.NumberUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ExpensesSummaryViewByOtherExpensesFragmentViewModel(private val expensesDAO: ExpensesDAO): ViewModel() {
    var mDateFromFilter: Calendar = Calendar.getInstance()
    var mDateToFilter: Calendar = Calendar.getInstance()
    val mLoadData: MutableLiveData<Boolean> = MutableLiveData(false)
    val mOtherExpenses: MutableLiveData<List<ExpensesEntityWithItemNameAndType>> by lazy {
        MutableLiveData<List<ExpensesEntityWithItemNameAndType>>()
    }
    val mTotalOtherExpenses:MutableLiveData<Double> by lazy{
        MutableLiveData<Double>()
    }

    fun getTotalExpensesByDateRange(){
        val readableFromDate = SIMPLE_DATE_FORMAT.format(mDateFromFilter.time)
        val readableToDate = SIMPLE_DATE_FORMAT.format(mDateToFilter.time)

        viewModelScope.launch {
            mTotalOtherExpenses.value = withContext(Dispatchers.IO){
                expensesDAO.getTotalPaymentAmount(readableFromDate, readableToDate)
            }

        }
    }
    fun getOtherExpensesItems() {
        val readableFromDate = SIMPLE_DATE_FORMAT.format(mDateFromFilter.time)
        val readableToDate = SIMPLE_DATE_FORMAT.format(mDateToFilter.time)

        viewModelScope.launch {
            mOtherExpenses.value = withContext(Dispatchers.IO){

                expensesDAO.getExpenses(readableFromDate,readableToDate)
            }
        }
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

//                val intent = Intent(requireContext(), ExpensesItemSummaryActivity::class.java)
//                //intent.putExtra(BillActivity.TITLE_TAG,"Create bill payment")
//                //intent.putExtra(BillActivity.WHAT_FRAGMENT, BillActivity.ADD_BILL_FRAGMENT)
//                requireContext().startActivity(intent)

            }


        }
    }

}
class ExpensesSummaryViewByOtherExpensesFragmentViewModelFactory(private val expensesDAO: ExpensesDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpensesSummaryViewByOtherExpensesFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpensesSummaryViewByOtherExpensesFragmentViewModel(expensesDAO) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}