package com.example.allhome.bill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.databinding.BillItemBinding
import com.example.allhome.databinding.FragmentBillsBinding
import com.example.allhome.databinding.RecipeItemBinding
import com.example.allhome.recipes.RecipesFragment
import com.example.allhome.recipes.ViewRecipeActivity
import com.example.allhome.recipes.ViewRecipeFragment
import com.example.allhome.storage.StorageFragment
import com.example.allhome.storage.StorageStorageListActivity
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BillsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    lateinit var mBillViewModel: BillViewModel
    lateinit var mFragmentBillsBinding:FragmentBillsBinding

    private val mStartingCalendar: Calendar = Calendar.getInstance()
    private val mEndingCalendar: Calendar = Calendar.getInstance()

    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            BillsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        requireActivity().title = "Bills"
        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        getBills(mStartingCalendar,mEndingCalendar)

        mFragmentBillsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bills,null,false)

        setDateDisplay(mStartingCalendar)
        mFragmentBillsBinding.previousMonthBtn.setOnClickListener(previousMonthBtnClickListener)
        mFragmentBillsBinding.nextMonthBtn.setOnClickListener(nextMonthBtnClickListener)

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentBillsBinding.recyclerView.addItemDecoration(decorator)

        val billRecyclerviewViewAdapater = BillRecyclerviewViewAdapater(arrayListOf())
        mFragmentBillsBinding.recyclerView.adapter = billRecyclerviewViewAdapater

        getBills(mStartingCalendar,mEndingCalendar)

        return mFragmentBillsBinding.root
    }

    fun setDateDisplay(calendar:Calendar){
        val dateString = SimpleDateFormat("MMMM yyyy").format(calendar.time)
        mFragmentBillsBinding.dateTextView.text = dateString
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_bill_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.addBillMenuItem->{
                val intent = Intent(requireContext(), BillActivity::class.java)
                intent.putExtra(BillActivity.TITLE_TAG,"Create bill payment")
                intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_FRAGMENT)
                requireContext().startActivity(intent)
            }
        }
        return true
    }

    fun getBills(startCalendar:Calendar,endCalendar:Calendar){

        mBillViewModel.mCoroutineScope.launch {

            val yearMonthDateString = SimpleDateFormat("yyyy-MM").format(startCalendar.time)

            Log.e("DATES","${yearMonthDateString}")
            val bills = mBillViewModel.getBillsInMonth(requireContext(), yearMonthDateString)
            Log.e("BILLS",bills.toString())

            withContext(Main){
                val billRecyclerviewViewAdapater = mFragmentBillsBinding.recyclerView.adapter as BillRecyclerviewViewAdapater
                billRecyclerviewViewAdapater.bills = bills
                billRecyclerviewViewAdapater.notifyDataSetChanged()
            }
        }

    }
    val previousMonthBtnClickListener= object:View.OnClickListener{
        override fun onClick(v: View?) {

            mStartingCalendar.add(Calendar.MONTH,-1)
            setDateDisplay(mStartingCalendar)
            getBills(mStartingCalendar,mEndingCalendar)

        }
    }
    val nextMonthBtnClickListener= object:View.OnClickListener{
        override fun onClick(v: View?) {
            mStartingCalendar.add(Calendar.MONTH,1)
            setDateDisplay(mStartingCalendar)
            getBills(mStartingCalendar,mEndingCalendar)
        }
    }

    class BillRecyclerviewViewAdapater(var bills:List<BillEntityWithTotalPayment>): RecyclerView.Adapter<BillRecyclerviewViewAdapater.ItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val billItemBinding = BillItemBinding.inflate(layoutInflater, parent, false)
            val itemHolder = ItemViewHolder(billItemBinding,this)
            itemHolder.setClickListener(itemHolder)
            return itemHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val billeEntity = bills[position]
            holder.billItemBinding.billEntityWithTotalPayment = billeEntity
            holder.billItemBinding.executePendingBindings()
        }

        override fun getItemCount(): Int {
            return bills.size
        }
        class  ItemViewHolder(var billItemBinding: BillItemBinding,var billRecyclerviewViewAdapater:BillRecyclerviewViewAdapater): RecyclerView.ViewHolder(billItemBinding.root),View.OnClickListener{

            fun setClickListener(clickListener:View.OnClickListener){
                billItemBinding.root.setOnClickListener(clickListener)
                billItemBinding.moreActionImageView.setOnClickListener(clickListener)
            }
            override fun onClick(view: View?) {
                val billEntity = billRecyclerviewViewAdapater.bills[adapterPosition]

                when(view!!.id){
                    R.id.moreActionImageView->{


                        val popupMenu = PopupMenu(view.context, view)
                        popupMenu.menuInflater.inflate(R.menu.bill_item_menu, popupMenu.menu)

                        popupMenu.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener{
                            override fun onMenuItemClick(item: MenuItem?): Boolean {
                                popupMenu.dismiss()
                                when (item!!.itemId) {
                                    R.id.addPaymentMenu->{
                                        val intent = Intent(view.context, BillActivity::class.java)
                                        intent.putExtra(BillActivity.TITLE_TAG,"Add bill payment")
                                        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_PAYMENT_FRAGMENT)
                                        intent.putExtra(AddPaymentFragment.ARG_ACTION,AddPaymentFragment.ADD_ACTION)
                                        intent.putExtra(AddPaymentFragment.ARG_BILL_ENTITY,billEntity)
                                        view.context.startActivity(intent)
                                    }
                                    R.id.viewInformationMenu->{

                                        val intent = Intent(view.context, BillActivity::class.java)
                                        intent.putExtra(BillActivity.TITLE_TAG,"Bill Informations")
                                        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.BILL_INFORMATIONS_VIEWING)
                                        intent.putExtra(BillInformationViewingFragment.ARG_BILL_ENTITY,billEntity)
                                        view.context.startActivity(intent)
                                    }
                                }
                                return true
                            }

                        })

                        popupMenu.show()
                    }
                    R.id.parent->{
                        val intent = Intent(view.context, BillActivity::class.java)
                        intent.putExtra(BillActivity.TITLE_TAG,"Bill Informations")
                        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.BILL_INFORMATIONS_VIEWING)
                        intent.putExtra(BillInformationViewingFragment.ARG_BILL_ENTITY,billEntity)
                        view.context.startActivity(intent)
                    }
                }
            }
        }
    }





}