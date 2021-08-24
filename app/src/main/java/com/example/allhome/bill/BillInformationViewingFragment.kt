package com.example.allhome.bill

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.example.allhome.R
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.databinding.FragmentBillInformationViewingBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class BillInformationViewingFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBillEntityWithTotalPayment:BillEntityWithTotalPayment
    lateinit var mFragmentBillInformationViewingBinding:FragmentBillInformationViewingBinding

    companion object {
        const val ARG_BILL_ENTITY = "ARG_BILL_ENTITY"
        @JvmStatic fun newInstance(billEntity: BillEntityWithTotalPayment, param2: String) =
            BillInformationViewingFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BILL_ENTITY,billEntity)
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

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentBillInformationViewingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill_information_viewing,null,false)

        childFragmentManager.beginTransaction().replace(R.id.billInformationFragmentContainer,BillInformationFragment.newInstance(mBillEntityWithTotalPayment,"")).commit()
        childFragmentManager.beginTransaction().replace(R.id.billPaymentFragmentContainer,BillPaymentsFragment.newInstance(mBillEntityWithTotalPayment,"")).commit()


        return mFragmentBillInformationViewingBinding.root
    }




}