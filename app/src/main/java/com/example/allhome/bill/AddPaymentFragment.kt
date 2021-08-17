package com.example.allhome.bill

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.allhome.R
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.databinding.FragmentAddPaymentBinding
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM2 = "param2"


class AddPaymentFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    var mSelectedPaymentDateCalendar = Calendar.getInstance()
    private var mBillEntity:BillEntity? = null

    lateinit var mFragmentAddPaymentBinding:FragmentAddPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        arguments?.let {
            mBillEntity = it.getParcelable(ARG_BILL_ENTITY)
            param2 = it.getString(ARG_PARAM2)


        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentAddPaymentBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_payment,null,false)
        mFragmentAddPaymentBinding.paymentDateImageView.setOnClickListener(paymentDateOnClickListener)

        initUI()
        return mFragmentAddPaymentBinding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.save_bill_payment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.saveBillPaymentMenu->{
                savePayment()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun initUI(){

        val paymentDateString = SimpleDateFormat("MMMM dd,yyyy").format(mSelectedPaymentDateCalendar.time)
        mFragmentAddPaymentBinding.paymentDateTextInputEditText.setText(paymentDateString)
    }
    private fun savePayment(){

        val paymentString = mFragmentAddPaymentBinding.paymentAmountTextInputEditText.text.toString()
        val paymentNoteString = mFragmentAddPaymentBinding.billPaymentNoteTextInputEditText.text.toString()
        val paymentDouble = if(paymentString.length > 0) paymentString.toDouble() else 0.0

        if(paymentDouble <=0){
            Toast.makeText(requireContext(),"Please add payment.",Toast.LENGTH_SHORT).show()
            return
        }


    }

    fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val paymentDateString = SimpleDateFormat("MMMM dd,yyyy").format(date)

            mFragmentAddPaymentBinding.paymentDateTextInputEditText.setText(paymentDateString)
            mSelectedPaymentDateCalendar.time = date


        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    val paymentDateOnClickListener = object:View.OnClickListener{
        override fun onClick(view: View?) {
            showCalendar()
        }
    }



    companion object {
        const val ARG_BILL_ENTITY = "ARG_BILL_ENTITY"
        @JvmStatic fun newInstance(billEntity: BillEntity, param2: String) =
            AddPaymentFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_BILL_ENTITY, billEntity)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}