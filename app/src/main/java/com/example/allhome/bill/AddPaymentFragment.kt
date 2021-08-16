package com.example.allhome.bill

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.databinding.FragmentAddPaymentBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AddPaymentFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var mFragmentAddPaymentBinding:FragmentAddPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentAddPaymentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_payment,null,false)
        return mFragmentAddPaymentBinding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_bill_payment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.saveBillPaymentMenu->{
                Toast.makeText(requireContext(),"Save bill payment",Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            AddPaymentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}