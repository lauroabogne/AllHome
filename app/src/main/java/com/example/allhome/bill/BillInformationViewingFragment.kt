package com.example.allhome.bill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    private val addBillPaymentResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){
            initFragments(mBillEntityWithTotalPayment)
        }
    }

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
        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)

        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar?.title = "Bill Informations"
        toolbar?.inflateMenu(R.menu.bill_information_menu)
        toolbar?.setNavigationOnClickListener(toolbarNavigationClickListener)
        toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.addBillPaymentMenu -> {
                    openActivityToAddPayment(mBillEntityWithTotalPayment)
                }
            }
            true
        }



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentBillInformationViewingBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bill_information_viewing,null,false)

        initFragments(mBillEntityWithTotalPayment)
        return mFragmentBillInformationViewingBinding.root
    }
    fun initFragments(billEntityWithTotalPayment:BillEntityWithTotalPayment){

        mBillViewModel.mCoroutineScope.launch {
            mBillEntityWithTotalPayment = mBillViewModel.getBillWithTotalPayment(requireContext(),billEntityWithTotalPayment.billEntity.uniqueId)
            withContext(Main){
                childFragmentManager.beginTransaction().replace(R.id.billInformationFragmentContainer,BillInformationFragment.newInstance(mBillEntityWithTotalPayment,"")).commit()
                childFragmentManager.beginTransaction().replace(R.id.billPaymentFragmentContainer,BillPaymentsFragment.newInstance(billEntityWithTotalPayment,"")).commit()
            }
        }


    }
    override fun updateBillInformationFragment(billEntityWithTotalPayment:BillEntityWithTotalPayment) {

        initFragments(billEntityWithTotalPayment)

    }


    fun openActivityToAddPayment(billEntity:BillEntityWithTotalPayment){

        val intent = Intent(requireContext(), BillActivity::class.java)
        intent.putExtra(BillActivity.TITLE_TAG,"Add bill payment")
        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_PAYMENT_FRAGMENT)
        intent.putExtra(AddPaymentFragment.ARG_ACTION,AddPaymentFragment.ADD_ACTION)
        intent.putExtra(AddPaymentFragment.ARG_BILL_ENTITY,billEntity)
        addBillPaymentResultContract.launch(intent)


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
