package com.example.allhome.bill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class BillsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    lateinit var mBillViewModel: BillViewModel
    lateinit var mFragmentBillsBinding:FragmentBillsBinding



    companion object {
        const val RESULT_TAG = "RESULT_TAG"
        const val VIEW_INFORMATION_REQUEST = 1986
        const val ADD_PAYMENT_REQUEST = 1987
        const val CREATE_BILL_REQUEST = 1988
        const val WEEK_VIEWING = 0
        const val MONTH_VIEWING = 1
        const val YEAR_VIEWING = 2
        const val CUSTOM_VIEWING = 3

        @JvmStatic fun newInstance(param1: String, param2: String) =
            BillsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private val createBillResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){
            getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
        }
    }

    private val viewBillOrAddPaymentResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){
            val billEntityWithTotalPayment = activityResult.data?.getParcelableExtra<BillEntityWithTotalPayment>(RESULT_TAG)
            mBillViewModel.mCoroutineScope.launch {
                val startDateString = SimpleDateFormat("yyyy-MM-dd").format(mBillViewModel.mStartingCalendar.time)
                val endDateString = SimpleDateFormat("yyyy-MM-dd").format(mBillViewModel.mEndingCalendar.time)
                mBillViewModel.mTotalAmountDue = mBillViewModel.getTotalAmountDue(requireContext(),startDateString,endDateString)
                mBillViewModel.mTotalAmountPaid = mBillViewModel.getTotalPaymentAmount(requireContext(),startDateString,endDateString)
                mBillViewModel.mTotalAmountOverdue = mBillViewModel.getOverdueAmount(requireContext(),startDateString,endDateString)
                val newBillEntityWithTotalPayment = mBillViewModel.getBillWithTotalPayment(requireContext(), billEntityWithTotalPayment?.billEntity!!.uniqueId)
                withContext(Main){
                    val adapter = mFragmentBillsBinding.recyclerView.adapter as BillRecyclerviewViewAdapater
                    val index = adapter.bills.indexOfFirst {
                        it.billEntity.uniqueId == newBillEntityWithTotalPayment.billEntity.uniqueId
                    }
                    adapter.bills.set(index,newBillEntityWithTotalPayment)
                    adapter.notifyItemChanged(index)
                    mFragmentBillsBinding.invalidateAll()
                }
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


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)
        setDates()

        mFragmentBillsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bills,null,false)
        mFragmentBillsBinding.billViewModel = mBillViewModel
        setDateDisplay(mBillViewModel.mStartingCalendar)
        mFragmentBillsBinding.previousMonthBtn.setOnClickListener(previousMonthBtnClickListener)
        mFragmentBillsBinding.nextMonthBtn.setOnClickListener(nextMonthBtnClickListener)

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentBillsBinding.recyclerView.addItemDecoration(decorator)

        val billRecyclerviewViewAdapater = BillRecyclerviewViewAdapater(arrayListOf())
        mFragmentBillsBinding.recyclerView.adapter = billRecyclerviewViewAdapater

        getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)

        return mFragmentBillsBinding.root
    }
    fun setDates(){
        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.DAY_OF_MONTH, 1)
        mBillViewModel.mStartingCalendar = startCalendar

        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        mBillViewModel.mEndingCalendar = endCalendar

    }
    fun generateWeek(){
            mBillViewModel.mStartingCalendar = Calendar.getInstance();
            mBillViewModel.mStartingCalendar.set(Calendar.DAY_OF_WEEK, 1)

            mBillViewModel.mEndingCalendar = Calendar.getInstance()
            mBillViewModel.mEndingCalendar.set(Calendar.DAY_OF_WEEK, 7)

    }
    fun generateYear(){
        mBillViewModel.mStartingCalendar = Calendar.getInstance();
        mBillViewModel.mStartingCalendar.set(Calendar.MONTH, 0)
        mBillViewModel.mStartingCalendar.set(Calendar.DAY_OF_MONTH, 1)

        mBillViewModel.mEndingCalendar = Calendar.getInstance()
        mBillViewModel.mEndingCalendar.set(Calendar.MONTH, 11)
        mBillViewModel.mEndingCalendar.set(Calendar.DAY_OF_MONTH, 31)
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
                intent.putExtra(BillActivity.TITLE_TAG,"Create bill")
                intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_FRAGMENT)
                createBillResultContract.launch(intent)
            }
            R.id.billViewByWeekMenu->{
                Toast.makeText(requireContext(),"billViewByWeekMenu",Toast.LENGTH_SHORT).show()
                mBillViewModel.mVIEWING = WEEK_VIEWING
                generateWeek()
                getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)

            }
            R.id.billViewByMonthMenu->{
                mBillViewModel.mVIEWING = MONTH_VIEWING
                setDates()
                getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
                Toast.makeText(requireContext(),"billViewByMonthMenu",Toast.LENGTH_SHORT).show()
            }
            R.id.billViewByYearMenu->{
                mBillViewModel.mVIEWING = YEAR_VIEWING
                generateYear()
                getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
                Toast.makeText(requireContext(),"billViewByYearMenu",Toast.LENGTH_SHORT).show()
            }
            R.id.billViewByCustomDate->{

                val mBillCustomDateRangeDialogFragment = BillCustomDateRangeDialogFragment()
                mBillCustomDateRangeDialogFragment.mOnClickListener = object:View.OnClickListener{
                    override fun onClick(v: View?) {
                        mBillCustomDateRangeDialogFragment.dismiss()

                        mBillViewModel.mVIEWING = CUSTOM_VIEWING
                        mBillViewModel.mStartingCalendar = mBillCustomDateRangeDialogFragment.mStartDate!!
                        mBillViewModel.mEndingCalendar = mBillCustomDateRangeDialogFragment.mEndDate!!

                        getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)

                    }

                }
                mBillCustomDateRangeDialogFragment.show(childFragmentManager,"BillCustomDateRangeDialogFragment")



                Toast.makeText(requireContext(),"billViewByCustomDate",Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    fun getBills(startCalendar:Calendar,endCalendar:Calendar){

        mBillViewModel.mCoroutineScope.launch {

            val yearMonthDateString = SimpleDateFormat("yyyy-MM").format(startCalendar.time)
            val startDateString = SimpleDateFormat("yyyy-MM-dd").format(startCalendar.time)
            val endDateString = SimpleDateFormat("yyyy-MM-dd").format(endCalendar.time)

            val bills = mBillViewModel.getBillsByDateRange(requireContext(),startDateString,endDateString)
            mBillViewModel.mTotalAmountDue = mBillViewModel.getTotalAmountDue(requireContext(),startDateString,endDateString)
            mBillViewModel.mTotalAmountPaid = mBillViewModel.getTotalPaymentAmount(requireContext(),startDateString,endDateString)
            mBillViewModel.mTotalAmountOverdue = mBillViewModel.getOverdueAmount(requireContext(),startDateString,endDateString)

            withContext(Main){

                val billRecyclerviewViewAdapater = mFragmentBillsBinding.recyclerView.adapter as BillRecyclerviewViewAdapater
                billRecyclerviewViewAdapater.bills = bills as ArrayList<BillEntityWithTotalPayment>
                billRecyclerviewViewAdapater.notifyDataSetChanged()

                mFragmentBillsBinding.invalidateAll()
            }
        }

    }
    fun showDeleteDialogFragment(billEntityWithTotalPayment:BillEntityWithTotalPayment){

        val billEntity = billEntityWithTotalPayment.billEntity
        mBillViewModel.mCoroutineScope.launch {
            val recordCount = mBillViewModel.getRecordCountByGroupId(requireContext(),billEntityWithTotalPayment.billEntity.groupUniqueId)
            val isRecurring = recordCount > 1
            withContext(Main){
                val title = if(isRecurring) "Selected bill is part of recurring bill. What you want to delete?" else "Are you sure to delete bill?"
                val dialog = DeleteBillDialogFragment("",title,isRecurring)
                dialog.setClickListener( CustomOnClickListener(billEntity,dialog))
                dialog.show(childFragmentManager,"DeleteBillDialogFragment")
            }
        }


    }
    fun openActivityToViewBillInformations(billEntity:BillEntityWithTotalPayment){

        val intent = Intent(requireContext(), BillActivity::class.java)
        intent.putExtra(BillActivity.TITLE_TAG,"Bill Informations")
        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.BILL_INFORMATIONS_VIEWING)
        intent.putExtra(BillInformationViewingFragment.ARG_BILL_ENTITY,billEntity)
        viewBillOrAddPaymentResultContract.launch(intent)
        //startActivityForResult(intent,VIEW_INFORMATION_REQUEST)
    }
    fun openActivityToAddPayment(billEntity:BillEntityWithTotalPayment){

        val intent = Intent(requireContext(), BillActivity::class.java)
        intent.putExtra(BillActivity.TITLE_TAG,"Add bill payment")
        intent.putExtra(BillActivity.WHAT_FRAGMENT,BillActivity.ADD_BILL_PAYMENT_FRAGMENT)
        intent.putExtra(AddPaymentFragment.ARG_ACTION,AddPaymentFragment.ADD_ACTION)
        intent.putExtra(AddPaymentFragment.ARG_BILL_ENTITY,billEntity)
        //startActivityForResult(intent,ADD_PAYMENT_REQUEST)
        viewBillOrAddPaymentResultContract.launch(intent)

    }


    val previousMonthBtnClickListener= object:View.OnClickListener{
        override fun onClick(v: View?) {

            if(mBillViewModel.mVIEWING == MONTH_VIEWING){

                mBillViewModel.mStartingCalendar.add(Calendar.MONTH,-1)
                mBillViewModel.mEndingCalendar.add(Calendar.MONTH,-1)

                val maxDayOfMonth = mBillViewModel.mStartingCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                mBillViewModel.mStartingCalendar.set(Calendar.DAY_OF_MONTH,1)
                mBillViewModel.mEndingCalendar.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)

            }else if(mBillViewModel.mVIEWING == WEEK_VIEWING){

                mBillViewModel.mStartingCalendar.add(Calendar.WEEK_OF_YEAR,-1)
                mBillViewModel.mEndingCalendar.add(Calendar.WEEK_OF_YEAR,-1)

            }else if(mBillViewModel.mVIEWING == YEAR_VIEWING){

                mBillViewModel.mStartingCalendar.set(Calendar.YEAR,mBillViewModel.mStartingCalendar.get(Calendar.YEAR)-1)
                mBillViewModel.mEndingCalendar.set(Calendar.YEAR,mBillViewModel.mEndingCalendar.get(Calendar.YEAR)-1)
            }else if(mBillViewModel.mVIEWING == CUSTOM_VIEWING){

                val daysBetweenStartAndEnding = Days.daysBetween(DateTime(mBillViewModel.mStartingCalendar.time),DateTime(mBillViewModel.mEndingCalendar.time)).days
                mBillViewModel.mStartingCalendar.add(Calendar.DAY_OF_MONTH,-(daysBetweenStartAndEnding + 1))
                mBillViewModel.mEndingCalendar.add(Calendar.DAY_OF_MONTH,-(daysBetweenStartAndEnding + 1))

            }


            setDateDisplay(mBillViewModel.mStartingCalendar)
            getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)

        }
    }
    val nextMonthBtnClickListener= object:View.OnClickListener{
        override fun onClick(v: View?) {

            if(mBillViewModel.mVIEWING == MONTH_VIEWING){

                mBillViewModel.mStartingCalendar.add(Calendar.MONTH,1)
                mBillViewModel.mEndingCalendar.add(Calendar.MONTH,1)

                val maxDayOfMonth = mBillViewModel.mStartingCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                mBillViewModel.mStartingCalendar.set(Calendar.DAY_OF_MONTH,1)
                mBillViewModel.mEndingCalendar.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)


            }else if(mBillViewModel.mVIEWING == WEEK_VIEWING){

                mBillViewModel.mStartingCalendar.add(Calendar.WEEK_OF_YEAR,1)
                mBillViewModel.mEndingCalendar.add(Calendar.WEEK_OF_YEAR,1)

            }else if(mBillViewModel.mVIEWING == YEAR_VIEWING){

                mBillViewModel.mStartingCalendar.set(Calendar.YEAR,mBillViewModel.mStartingCalendar.get(Calendar.YEAR)+1)
                mBillViewModel.mEndingCalendar.set(Calendar.YEAR,mBillViewModel.mEndingCalendar.get(Calendar.YEAR)+1)


            }else if(mBillViewModel.mVIEWING == CUSTOM_VIEWING){

                val daysBetweenStartAndEnding = Days.daysBetween(DateTime(mBillViewModel.mStartingCalendar.time),DateTime(mBillViewModel.mEndingCalendar.time)).days
                mBillViewModel.mStartingCalendar.add(Calendar.DAY_OF_MONTH,daysBetweenStartAndEnding + 1)
                mBillViewModel.mEndingCalendar.add(Calendar.DAY_OF_MONTH,daysBetweenStartAndEnding + 1)

            }




            setDateDisplay(mBillViewModel.mStartingCalendar)
            getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
        }
    }


    inner class CustomOnClickListener(val billEntity:BillEntity,val dialog:DeleteBillDialogFragment):View.OnClickListener{
        override fun onClick(view: View?) {

            when(view!!.id){
                R.id.deleteSelectedBillOnlyBtn->{
                    Toast.makeText(requireContext(),"Delete selected only",Toast.LENGTH_SHORT).show()

                    mBillViewModel.mCoroutineScope.launch {

                        val affectedCount = mBillViewModel.updateSelectedBillAsDeleted(requireContext(),billEntity.uniqueId)
                        withContext(Main){
                            dialog.dismiss()
                            Toast.makeText(requireContext(),"Bill deleted successfully.",Toast.LENGTH_SHORT).show()
                            getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
                        }
                    }

                }
                R.id.deleteAlsoFutureBillBtn->{
                    Toast.makeText(requireContext(),"Delete future bill also",Toast.LENGTH_SHORT).show()

                    mBillViewModel.mCoroutineScope.launch {
                        val affectedCount = mBillViewModel.updateSelectedAndFutureBillAsDeleted(requireContext(),billEntity.groupUniqueId,billEntity.dueDate)
                        withContext(Main){
                            dialog.dismiss()
                            Toast.makeText(requireContext(),"Bill deleted successfully.",Toast.LENGTH_SHORT).show()
                            getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
                        }
                    }
                }
                DeleteBillDialogFragment.POSITIVE_BTN_ID->{

                    mBillViewModel.mCoroutineScope.launch {

                        val affectedCount = mBillViewModel.updateSelectedBillAsDeleted(requireContext(),billEntity.uniqueId)
                        withContext(Main){
                            dialog.dismiss()
                            Toast.makeText(requireContext(),"Bill deleted successfully.",Toast.LENGTH_SHORT).show()
                            getBills(mBillViewModel.mStartingCalendar,mBillViewModel.mEndingCalendar)
                        }
                    }
                }
            }
        }

    }
    inner class BillRecyclerviewViewAdapater(var bills:ArrayList<BillEntityWithTotalPayment>): RecyclerView.Adapter<BillRecyclerviewViewAdapater.ItemViewHolder>() {
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
        inner class  ItemViewHolder(var billItemBinding: BillItemBinding,var billRecyclerviewViewAdapater:BillRecyclerviewViewAdapater): RecyclerView.ViewHolder(billItemBinding.root),View.OnClickListener{

            fun setClickListener(clickListener:View.OnClickListener){
                billItemBinding.root.setOnClickListener(clickListener)
                billItemBinding.moreActionImageView.setOnClickListener(clickListener)
            }
            override fun onClick(view: View?) {
                val billEntity = billRecyclerviewViewAdapater.bills[adapterPosition]
                Log.e("billEntity",billEntity.toString())

                when(view!!.id){
                    R.id.moreActionImageView->{

                        val popupMenu = PopupMenu(view.context, view)
                        popupMenu.menuInflater.inflate(R.menu.bill_item_menu, popupMenu.menu)

                        popupMenu.setOnMenuItemClickListener(object: PopupMenu.OnMenuItemClickListener{
                            override fun onMenuItemClick(item: MenuItem?): Boolean {
                                popupMenu.dismiss()
                                when (item!!.itemId) {
                                    R.id.addPaymentMenu->{

                                        openActivityToAddPayment(billEntity)
                                    }
                                    R.id.viewInformationMenu->{

                                        openActivityToViewBillInformations(billEntity)
                                    }
                                    R.id.deleteBillMenu->{
                                        Toast.makeText(view.context,"Delete",Toast.LENGTH_SHORT).show()

                                        showDeleteDialogFragment(billEntity)


                                    }
                                }
                                return true
                            }

                        })

                        popupMenu.show()
                    }
                    R.id.parent->{

                        openActivityToViewBillInformations(billEntity)
                    }
                }
            }
        }
    }





}