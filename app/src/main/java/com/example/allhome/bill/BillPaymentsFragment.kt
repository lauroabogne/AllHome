package com.example.allhome.bill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.R
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.data.entities.BillPaymentEntity
import com.example.allhome.databinding.BillItemBinding
import com.example.allhome.databinding.BillPaymentItemBinding
import com.example.allhome.databinding.FragmentBillPaymentsBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BillPaymentsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBillEntityWithTotalPayment:BillEntityWithTotalPayment
    lateinit var mFragmentBillPaymentsBinding:FragmentBillPaymentsBinding
    lateinit var mBillViewModel:BillViewModel



    companion object {
        const val ARG_BILL_ENTITY = "ARG_BILL_ENTITY"
        @JvmStatic fun newInstance(billEntity: BillEntityWithTotalPayment, param2: String) =
            BillPaymentsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BILL_ENTITY,billEntity)
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mBillEntityWithTotalPayment = it.getParcelable(ARG_BILL_ENTITY)!!
            param2 = it.getString(ARG_PARAM2)
        }

        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        mFragmentBillPaymentsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill_payments,null,false)

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentBillPaymentsBinding.recyclerView.addItemDecoration(decorator)

        val billPaymentsRecyclerviewViewAdapater = BillPaymentsRecyclerviewViewAdapater(arrayListOf(),this)
        mFragmentBillPaymentsBinding.recyclerView.adapter = billPaymentsRecyclerviewViewAdapater

        getBillPayments()

        return mFragmentBillPaymentsBinding.root
    }

    fun getBillPayments(){
        mBillViewModel.mCoroutineScope.launch {

            val payments = mBillViewModel.getPayments(requireContext(),mBillEntityWithTotalPayment.billEntity.uniqueId)


            withContext(Main){
                val billPaymentsRecyclerviewViewAdapater = (mFragmentBillPaymentsBinding.recyclerView.adapter as BillPaymentsRecyclerviewViewAdapater)
                billPaymentsRecyclerviewViewAdapater.billPaymentEntities = payments
                billPaymentsRecyclerviewViewAdapater.notifyDataSetChanged()
            }


        }

    }

    class BillPaymentsRecyclerviewViewAdapater(var billPaymentEntities:List<BillPaymentEntity>,val billPaymentsFragment:BillPaymentsFragment): RecyclerView.Adapter<BillPaymentsRecyclerviewViewAdapater.ItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val billPaymentItemBinding = BillPaymentItemBinding.inflate(layoutInflater, parent, false)
            val itemHolder = ItemViewHolder(billPaymentItemBinding,this)

            itemHolder.setClickListener(itemHolder)
            itemHolder.setClickListener(itemHolder)
            return itemHolder
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            val billPaymentEntity = billPaymentEntities[position]
            holder.billItemBinding.billPaymentEntity = billPaymentEntity
            holder.billItemBinding.executePendingBindings()
        }

        override fun getItemCount(): Int {
            Log.e("THE_SIZE",billPaymentEntities.size.toString())
            return billPaymentEntities.size
        }
        inner class  ItemViewHolder(var billItemBinding: BillPaymentItemBinding, var billPaymentsRecyclerviewViewAdapater:BillPaymentsRecyclerviewViewAdapater): RecyclerView.ViewHolder(billItemBinding.root),View.OnClickListener{

            fun setClickListener(clickListener:View.OnClickListener){
                billItemBinding.moreActionImageBtn.setOnClickListener(clickListener)

            }
            override fun onClick(view: View?) {

                val billPaymentEntity  = billPaymentsRecyclerviewViewAdapater.billPaymentEntities[adapterPosition]

                val popupMenu = PopupMenu(view!!.context, view)
                popupMenu.menuInflater.inflate(R.menu.bill_payment_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener{
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        popupMenu.dismiss()
                        when (item!!.itemId) {
                            R.id.editBillMenu->{
                                val intent = Intent(view.context, BillActivity::class.java)
                                intent.putExtra(BillActivity.TITLE_TAG,"Edit bill payment")
                                intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_PAYMENT_FRAGMENT)
                                intent.putExtra(AddPaymentFragment.ARG_BILL_ENTITY,billPaymentsFragment.mBillEntityWithTotalPayment)
                                intent.putExtra(AddPaymentFragment.ARG_ACTION,AddPaymentFragment.EDIT_ACTION)
                                intent.putExtra(AddPaymentFragment.ARG_PAYMENT_ENTITY,billPaymentEntity)

                                view.context.startActivity(intent)
                            }
                            R.id.deleteBillMenu->{

                                /*val intent = Intent(view.context, BillActivity::class.java)
                                intent.putExtra(BillActivity.TITLE_TAG,"Bill Informations")
                                intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.BILL_INFORMATIONS_VIEWING)
                                intent.putExtra(BillInformationViewingFragment.ARG_BILL_ENTITY,billEntity)
                                view.context.startActivity(intent)*/
                            }
                        }
                        return true
                    }

                })
                popupMenu.show()
            }
        }
    }


}