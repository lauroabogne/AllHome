package com.example.allhome.bill

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.databinding.FragmentBillInformationBinding


private const val ARG_PARAM2 = "param2"


class BillInformationFragment : Fragment() {
    private var param2: String? = null
    private lateinit var mBillEntityWithTotalPayment: BillEntityWithTotalPayment
    lateinit var mFragmentBillInformationBinding:FragmentBillInformationBinding

    companion object {
        const val ARG_BILL_ENTITY = "ARG_BILL_ENTITY"
        @JvmStatic fun newInstance(billEntity: BillEntityWithTotalPayment, param2: String) =
            BillInformationFragment().apply {
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

        mFragmentBillInformationBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill_information,null,false)
        mFragmentBillInformationBinding.billEntityWithTotalPayment = mBillEntityWithTotalPayment
        return mFragmentBillInformationBinding.root
    }


}