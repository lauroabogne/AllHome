package com.example.allhome.bill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.allhome.R
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.databinding.FragmentBillInformationViewingBinding
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class BillInformationViewingFragment : Fragment(),BillFragmentCommunicator {

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var mBillEntityWithTotalPayment:BillEntityWithTotalPayment
    lateinit var mFragmentBillInformationViewingBinding:FragmentBillInformationViewingBinding
    lateinit var mBillViewModel: BillViewModel

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

        setHasOptionsMenu(true)
        val toolbar: Toolbar = requireActivity().findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.setNavigationOnClickListener(toolbarNavigationClickListener)

        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bill_information_menu, menu)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentBillInformationViewingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill_information_viewing,null,false)

        initFragments(mBillEntityWithTotalPayment)
        return mFragmentBillInformationViewingBinding.root
    }
    fun initFragments(billEntityWithTotalPayment:BillEntityWithTotalPayment){
        childFragmentManager.beginTransaction().replace(R.id.billInformationFragmentContainer,BillInformationFragment.newInstance(billEntityWithTotalPayment,"")).commit()
        childFragmentManager.beginTransaction().replace(R.id.billPaymentFragmentContainer,BillPaymentsFragment.newInstance(billEntityWithTotalPayment,"")).commit()

    }
    override fun updateBillInformationFragment(billEntityWithTotalPayment:BillEntityWithTotalPayment) {

        mBillEntityWithTotalPayment = billEntityWithTotalPayment
        childFragmentManager.beginTransaction().replace(R.id.billInformationFragmentContainer,BillInformationFragment.newInstance(mBillEntityWithTotalPayment,"")).commit()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.addBillPaymentMenu -> {
                openActivityToAddPayment(mBillEntityWithTotalPayment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == BillsFragment.ADD_PAYMENT_REQUEST){

            mBillViewModel.mCoroutineScope.launch {
                mBillEntityWithTotalPayment = mBillViewModel.getBillWithTotalPayment(requireContext(),mBillEntityWithTotalPayment.billEntity.uniqueId)
                withContext(Main){
                    initFragments(mBillEntityWithTotalPayment)
                }
            }
        }

    }
    fun openActivityToAddPayment(billEntity:BillEntityWithTotalPayment){

        val intent = Intent(requireContext(), BillActivity::class.java)
        intent.putExtra(BillActivity.TITLE_TAG,"Add bill payment")
        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_PAYMENT_FRAGMENT)
        intent.putExtra(AddPaymentFragment.ARG_ACTION,AddPaymentFragment.ADD_ACTION)
        intent.putExtra(AddPaymentFragment.ARG_BILL_ENTITY,billEntity)
        startActivityForResult(intent, BillsFragment.ADD_PAYMENT_REQUEST)

    }
    val toolbarNavigationClickListener= object: View.OnClickListener{
        override fun onClick(v: View?) {

            val intent = Intent()
            intent.putExtra(BillsFragment.RESULT_TAG,mBillEntityWithTotalPayment)
            requireActivity().setResult(Activity.RESULT_OK,intent)
            requireActivity().finish()
        }

    }

}
interface BillFragmentCommunicator{
    fun updateBillInformationFragment(billEntityWithTotalPayment:BillEntityWithTotalPayment)
}
