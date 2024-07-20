package com.example.allhome.bill

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.allhome.R
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.entities.BillCategoryEntity
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.databinding.FragmentAddBillBinding
import com.example.allhome.global_ui.DateInMonthDialogFragment
import com.example.allhome.utils.MinMaxInputFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AddBillFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    lateinit var mFragmentAddBillBinding:FragmentAddBillBinding
    lateinit var mBillViewModel:BillViewModel
    var mDueDate:String? = null
    var mRepeatUntilDate:String? = null


    private val mAddExpenseDialogFragment: BillAddCategoryDialogFragment by lazy {
        BillAddCategoryDialogFragment(mAddExpenseCategoryListener)
    }

    private val mAddExpenseCategoryListener = object : BillAddCategoryDialogFragment.AddBillCategoryListener{
        override fun onExpenseCategorySet(billCategoryEntity: BillCategoryEntity) {

            mBillViewModel.mCoroutineScope.launch {
                val id =  mBillViewModel.saveBillCategory(requireContext(),billCategoryEntity)
                withContext(Main){
                    if(id > 0){
                        mAddExpenseDialogFragment.dialog?.dismiss()
                        Toast.makeText(requireContext(),"Bill category save successfully.",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(),"Failed to save bill. Please try again.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)

        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar?.title = "Create bill"
        toolbar?.inflateMenu(R.menu.create_bill_menu)
        toolbar?.setNavigationOnClickListener {
           activity?.finish()
        }
        toolbar?.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.saveBillMenu->{
                    when(mFragmentAddBillBinding.oneTimeOrRecurringRadioGroup.checkedRadioButtonId){
                        R.id.recurringRadioButton->{
                            saveRecurring()
                        }
                        R.id.onetimeRadioButton->{
                            saveOnetimeBill()
                        }
                    }
                }
            }
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentAddBillBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_bill,null,false)
        mFragmentAddBillBinding.oneTimeOrRecurringRadioGroup.setOnCheckedChangeListener(oneTimeOrRecurringRadioGroupOnChangeCheckedListener)
        mFragmentAddBillBinding.recurringBillIncludeLayout.recurringParentElementConstraintLayout.visibility = View.GONE
        mFragmentAddBillBinding.recurringBillIncludeLayout.dueDateImageView.setOnClickListener(calendarImageViewClickListener)
        mFragmentAddBillBinding.recurringBillIncludeLayout.dueDateTextInputEditText.setOnClickListener(calendarImageViewClickListener)
        mFragmentAddBillBinding.recurringBillIncludeLayout.untilImageView.setOnClickListener(calendarImageViewClickListener)
        mFragmentAddBillBinding.recurringBillIncludeLayout.untilTextInputEditText.setOnClickListener(calendarImageViewClickListener)
        mFragmentAddBillBinding.recurringBillIncludeLayout.repeatSpinner.setSelection(2)// set Month as default selected
        mFragmentAddBillBinding.recurringBillIncludeLayout.repeatSpinner.onItemSelectedListener = repeatSpinnerOnItemSelectedListener
        mFragmentAddBillBinding.recurringBillIncludeLayout.recurringConditionRadioGroup.setOnCheckedChangeListener(recurringConditionRadioGroupOnChangeCheckedListener)

        mFragmentAddBillBinding.billAddCategoryImageView.setOnClickListener {

            mAddExpenseDialogFragment.show(requireActivity().supportFragmentManager,"BillAddCategoryDialogFragment")

        }

        val billNameAutoSuggestCustomAdapter = BillNameAutoSuggestCustomAdapter(requireContext(), arrayListOf())
        mFragmentAddBillBinding.billNameTextInput.threshold = 1
        mFragmentAddBillBinding.billNameTextInput.setAdapter(billNameAutoSuggestCustomAdapter)

        val billCategoryAutoSuggestCustomAdapter = BillCategoryAutoSuggestCustomAdapter(requireContext(), arrayListOf())
        mFragmentAddBillBinding.billCategoryTextinput.threshold = 1
        mFragmentAddBillBinding.billCategoryTextinput.setAdapter(billCategoryAutoSuggestCustomAdapter)


        return mFragmentAddBillBinding.root
    }
    private fun saveRecurring(){

        when(mFragmentAddBillBinding.recurringBillIncludeLayout.recurringConditionRadioGroup.checkedRadioButtonId){
            R.id.untilRadioButton->{
                saveBillWithEndDate()
            }
            R.id.forRadioButton->{
                saveBillWithoutEndDate()
            }
        }


    }
    private fun saveBillWithEndDate(){


        val billAmountString = mFragmentAddBillBinding.billAmountTextinput.text.toString().trim()
        val billAmountDouble = if(billAmountString.isNullOrEmpty()) 0.0 else billAmountString.toDouble()

        val billName = mFragmentAddBillBinding.billNameTextInput.text.toString().trim()
        val category = mFragmentAddBillBinding.billCategoryTextinput.text.toString().trim()

        val repeatEveryString = mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.text.toString().trim()
        val repeatEveryInt = if(repeatEveryString.isEmpty()) 0 else repeatEveryString.toInt()
        val repeatEvery:String = mFragmentAddBillBinding.recurringBillIncludeLayout.repeatSpinner.selectedItem as String

        if(billAmountDouble <=0){
            Toast.makeText(requireContext(),"Please input bill amount.",Toast.LENGTH_SHORT).show()
            return
        }

        if(billName.isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please input bill name.",Toast.LENGTH_SHORT).show()
            return
        }

        if(mDueDate.isNullOrEmpty() || mDueDate.isNullOrBlank()){
            Toast.makeText(requireContext(),"Please select due date or starting date.",Toast.LENGTH_SHORT).show()
            return
        }
        if(mRepeatUntilDate.isNullOrEmpty() || mRepeatUntilDate.isNullOrBlank()){
            Toast.makeText(requireContext(),"Please select until date.",Toast.LENGTH_SHORT).show()
            return
        }

        if(!repeatEvery.equals(requireContext().getString(R.string.end_of_month)) && repeatEveryInt <=0){
            Toast.makeText(requireContext(),"Please value for 'Repeat every'.",Toast.LENGTH_SHORT).show()
            mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.requestFocus()
            return
        }


        val dueDateCalendar = Calendar.getInstance()
        dueDateCalendar.time = SimpleDateFormat("yyyy-MM-dd").parse(mDueDate)

        val untilDateCalendar = Calendar.getInstance()
        untilDateCalendar.time = SimpleDateFormat("yyyy-MM-dd").parse(mRepeatUntilDate)

        //val billDueOrStartingDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)
        val billEntities = arrayListOf<BillEntity>()
        var billsGroupUniqueId = UUID.randomUUID().toString()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)
        var billUniqueId = UUID.randomUUID().toString()

        val billEntity = BillEntity(
            groupUniqueId =  billsGroupUniqueId,
            uniqueId = billUniqueId,
            amount = billAmountDouble,
            name = billName,
            category = category,
            dueDate = formatedDate,
            isRecurring = BillEntity.RECURRING,
            repeatEvery = repeatEveryInt,
            repeatBy = repeatEvery,
            repeatUntil= mRepeatUntilDate!!,
            repeatCount = 0,
            imageName = "",
            status = BillEntity.NOT_DELETED_STATUS,
            uploaded = BillEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime
        )
        //add first bill
        billEntities.add(billEntity)

        when(repeatEvery){
            requireContext().getString(R.string.day)->{


                do {
                    dueDateCalendar.add(Calendar.DAY_OF_MONTH,repeatEveryInt)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)
                    var billUniqueId = UUID.randomUUID().toString()

                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= mRepeatUntilDate!!,
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)

                }while (dueDateCalendar.before(untilDateCalendar))
            }
            requireContext().getString(R.string.week)->{

                do {
                    dueDateCalendar.add(Calendar.WEEK_OF_MONTH,repeatEveryInt)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()

                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= mRepeatUntilDate!!,
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)

                }while (dueDateCalendar.before(untilDateCalendar))
            }

            requireContext().getString(R.string.month)->{


                do {
                    dueDateCalendar.add(Calendar.MONTH,repeatEveryInt)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()

                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= mRepeatUntilDate!!,
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)

                }while (dueDateCalendar.before(untilDateCalendar))
            }

            requireContext().getString(R.string.end_of_month)->{

                do {
                    dueDateCalendar.add(Calendar.MONTH,1)
                    dueDateCalendar.set(Calendar.DAY_OF_MONTH,dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()

                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= mRepeatUntilDate!!,
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)

                }while (dueDateCalendar.before(untilDateCalendar))

            }

            requireContext().getString(R.string.date_of_month)->{

                do {
                    dueDateCalendar.add(Calendar.MONTH,1)
                    val maxDayOfMonth = dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                    if(repeatEveryInt > maxDayOfMonth ){
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)
                    }else{
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH,repeatEveryInt)
                    }

                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)
                    var billUniqueId = UUID.randomUUID().toString()

                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= mRepeatUntilDate!!,
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)


                }while(dueDateCalendar.before(untilDateCalendar))
            }

            requireContext().getString(R.string.year)->{

                do {
                    dueDateCalendar.add(Calendar.YEAR,1)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()

                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= mRepeatUntilDate!!,
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)

                }while (dueDateCalendar.before(untilDateCalendar))
            }
        }

        mBillViewModel.mCoroutineScope.launch {
            val billCategoryEntity = mBillViewModel.getCategory(requireContext(),category)
            if(category.isNotEmpty() && billCategoryEntity == null){
                val billCategoryEntity = BillCategoryEntity(uniqueId = UUID.randomUUID().toString(), name=category,"", status = BillCategoryEntity.NOT_DELETED_STATUS, uploaded = BillCategoryEntity.NOT_UPLOADED, created = currentDatetime, modified = currentDatetime)
                mBillViewModel.saveBillCategory(requireContext(),billCategoryEntity)
            }

            val id =  mBillViewModel.addBills(requireContext(),billEntities)

            withContext(Main){
                if(id.size > 0){
                    val intent = Intent()
                    requireActivity().setResult(Activity.RESULT_OK,intent)
                    requireActivity().finish()
                }else{
                    Toast.makeText(requireContext(),"Failed to save bill. Please try again.",Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
    private fun saveBillWithoutEndDate(){

        val billAmountString = mFragmentAddBillBinding.billAmountTextinput.text.toString().trim()
        val billAmountDouble = if(billAmountString.isNullOrEmpty()) 0.0 else billAmountString.toDouble()

        val billName = mFragmentAddBillBinding.billNameTextInput.text.toString().trim()
        val category = mFragmentAddBillBinding.billCategoryTextinput.text.toString().trim()

        val repeatEveryString = mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.text.toString().trim()
        val repeatEveryInt = if(repeatEveryString.isEmpty()) 0 else repeatEveryString.toInt()
        val repeatEvery:String = mFragmentAddBillBinding.recurringBillIncludeLayout.repeatSpinner.selectedItem as String

        if(billAmountDouble <=0){
            Toast.makeText(requireContext(),"Please input bill amount.",Toast.LENGTH_SHORT).show()
            return
        }

        if(billName.isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please input bill name.",Toast.LENGTH_SHORT).show()
            return
        }

        if(mDueDate.isNullOrEmpty() || mDueDate.isNullOrBlank()){
            Toast.makeText(requireContext(),"Please select due date or starting date.",Toast.LENGTH_SHORT).show()
            return
        }

        if(!repeatEvery.equals(requireContext().getString(R.string.end_of_month)) && repeatEveryInt <=0){
            Toast.makeText(requireContext(),"Please value for 'Repeat every'.",Toast.LENGTH_SHORT).show()
            mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.requestFocus()
            return
        }




        val dueDateCalendar = Calendar.getInstance()
        dueDateCalendar.time = SimpleDateFormat("yyyy-MM-dd").parse(mDueDate)

        val billEntities = arrayListOf<BillEntity>()
        var billsGroupUniqueId = UUID.randomUUID().toString()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)
        var billUniqueId = UUID.randomUUID().toString()

        val repeatTimesString = mFragmentAddBillBinding.recurringBillIncludeLayout.timesTextInputEditText.text.toString().trim()
        // repeatTimesInt - 1 because first bill added outside repeat function. See code below
        val repeatTimesInt = if(repeatTimesString.isEmpty()) 0 else repeatTimesString.toInt() - 1

        val billEntity = BillEntity(
            groupUniqueId =  billsGroupUniqueId,
            uniqueId = billUniqueId,
            amount = billAmountDouble,
            name = billName,
            category = category,
            dueDate = formatedDate,
            isRecurring = BillEntity.RECURRING,
            repeatEvery = repeatEveryInt,
            repeatBy = repeatEvery,
            repeatUntil= "",
            repeatCount = 0,
            imageName = "",
            status = BillEntity.NOT_DELETED_STATUS,
            uploaded = BillEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime
        )

        billEntities.add(billEntity)

        repeat(repeatTimesInt){

            when(repeatEvery){
                requireContext().getString(R.string.day)->{


                    dueDateCalendar.add(Calendar.DAY_OF_MONTH,repeatEveryInt)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()
                    val billEntity = BillEntity(
                            groupUniqueId =  billsGroupUniqueId,
                            uniqueId = billUniqueId,
                            amount = billAmountDouble,
                            name = billName,
                            category = category,
                            dueDate = formatedDate,
                            isRecurring = BillEntity.RECURRING,
                            repeatEvery = repeatEveryInt,
                            repeatBy = repeatEvery,
                            repeatUntil= "",
                            repeatCount = 0,
                            imageName = "",
                            status = BillEntity.NOT_DELETED_STATUS,
                            uploaded = BillEntity.NOT_UPLOADED,
                            created = currentDatetime,
                            modified = currentDatetime
                        )

                        billEntities.add(billEntity)

                }
                requireContext().getString(R.string.week)->{
                    dueDateCalendar.add(Calendar.WEEK_OF_MONTH,repeatEveryInt)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()
                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= "",
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)
                }
                requireContext().getString(R.string.month)->{
                    dueDateCalendar.add(Calendar.MONTH,repeatEveryInt)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()
                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= "",
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)
                }
                requireContext().getString(R.string.end_of_month)->{
                    dueDateCalendar.add(Calendar.MONTH,1)
                    dueDateCalendar.set(Calendar.DAY_OF_MONTH,dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))

                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()
                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= "",
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)
                }
                requireContext().getString(R.string.date_of_month)->{
                    dueDateCalendar.add(Calendar.MONTH,1)
                    val maxDayOfMonth = dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                    if(repeatEveryInt > maxDayOfMonth ){
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)
                    }else{
                        dueDateCalendar.set(Calendar.DAY_OF_MONTH,repeatEveryInt)
                    }


                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()
                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= "",
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)
                }
                requireContext().getString(R.string.year)->{
                    dueDateCalendar.add(Calendar.YEAR,1)
                    val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                    var billUniqueId = UUID.randomUUID().toString()
                    val billEntity = BillEntity(
                        groupUniqueId =  billsGroupUniqueId,
                        uniqueId = billUniqueId,
                        amount = billAmountDouble,
                        name = billName,
                        category = category,
                        dueDate = formatedDate,
                        isRecurring = BillEntity.RECURRING,
                        repeatEvery = repeatEveryInt,
                        repeatBy = repeatEvery,
                        repeatUntil= "",
                        repeatCount = 0,
                        imageName = "",
                        status = BillEntity.NOT_DELETED_STATUS,
                        uploaded = BillEntity.NOT_UPLOADED,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    billEntities.add(billEntity)
                }
            }
        }

        mBillViewModel.mCoroutineScope.launch {
            val billCategoryEntity = mBillViewModel.getCategory(requireContext(),category)
            if(category.isNotEmpty() && billCategoryEntity == null){
                val billCategoryEntity = BillCategoryEntity(uniqueId = UUID.randomUUID().toString(), name=category,"", status = BillCategoryEntity.NOT_DELETED_STATUS, uploaded = BillCategoryEntity.NOT_UPLOADED, created = currentDatetime, modified = currentDatetime)
                mBillViewModel.saveBillCategory(requireContext(),billCategoryEntity)

            }
            val ids = mBillViewModel.addBills(requireContext(),billEntities)
            withContext(Main){
                if(ids.size > 0){
                    val intent = Intent()
                    requireActivity().setResult(Activity.RESULT_OK,intent)
                    requireActivity().finish()
                }else{
                    Toast.makeText(requireContext(),"Failed to save bill. Please try again.",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun saveOnetimeBill(){

        val billAmountString = mFragmentAddBillBinding.billAmountTextinput.text.toString().trim()
        val billAmountDouble = if(billAmountString.isNullOrEmpty()) 0.0 else billAmountString.toDouble()

        val billName = mFragmentAddBillBinding.billNameTextInput.text.toString().trim()
        val category = mFragmentAddBillBinding.billCategoryTextinput.text.toString().trim()
        if(billAmountDouble <=0){
            Toast.makeText(requireContext(),"Please input bill amount.",Toast.LENGTH_SHORT).show()
            return
        }

        if(billName.isNullOrEmpty()){
            Toast.makeText(requireContext(),"Please input bill name.",Toast.LENGTH_SHORT).show()
            return
        }

        if(mDueDate.isNullOrEmpty() || mDueDate.isNullOrBlank()){
            Toast.makeText(requireContext(),"Please select due date or starting date.",Toast.LENGTH_SHORT).show()
            return
        }

        var billsGroupUniqueId = UUID.randomUUID().toString()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


        var billUniqueId = UUID.randomUUID().toString()
        val billEntity = BillEntity(
            groupUniqueId =  billsGroupUniqueId,
            uniqueId = billUniqueId,
            amount = billAmountDouble,
            name = billName,
            category = category,
            dueDate = mDueDate!!,
            isRecurring = BillEntity.NOT_RECURRING,
            repeatEvery = BillEntity.NOT_RECURRING,
            repeatBy = "",
            repeatUntil= "",
            repeatCount = 0,
            imageName = "",
            status = BillEntity.NOT_DELETED_STATUS,
            uploaded = BillEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime
        )

        mBillViewModel.mCoroutineScope.launch {


            val billCategoryEntity = mBillViewModel.getCategory(requireContext(),category)
            if(category.isNotEmpty() && billCategoryEntity == null){
                val billCategoryEntity = BillCategoryEntity(uniqueId = UUID.randomUUID().toString(), name=category,"", status = BillCategoryEntity.NOT_DELETED_STATUS, uploaded = BillCategoryEntity.NOT_UPLOADED, created = currentDatetime, modified = currentDatetime)
                mBillViewModel.saveBillCategory(requireContext(),billCategoryEntity)

            }

            val id = mBillViewModel.addBill(requireContext(),billEntity)

            withContext(Main){
                if(id > 0){
                    val intent = Intent()
                    requireActivity().setResult(Activity.RESULT_OK,intent)
                    requireActivity().finish()
                }else{
                    Toast.makeText(requireContext(),"Failed to save bill. Please try again.",Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
    fun showCalendar(requestDateType:Int){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val stringDate= SimpleDateFormat("yyyy-MM-dd").format(date)

            when(requestDateType){
                GET_DUE_DATE_REQUEST->{

                    mDueDate = stringDate

                    val selectedTime = SimpleDateFormat("yyyy-MM-dd").parse(mDueDate)
                    val dueDateString = SimpleDateFormat("MMMM dd,yyyy").format(selectedTime)
                    mFragmentAddBillBinding.recurringBillIncludeLayout.dueDateTextInputEditText.setText(dueDateString)

                }
                GET_UNTIL_DATE_REQUEST->{
                    mRepeatUntilDate = stringDate
                    val selectedTime = SimpleDateFormat("yyyy-MM-dd").parse(mRepeatUntilDate)
                    val repeatUntilDateString = SimpleDateFormat("MMMM dd,yyyy").format(selectedTime)
                    mFragmentAddBillBinding.recurringBillIncludeLayout.untilTextInputEditText.setText(repeatUntilDateString)
                }
            }

        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    private val calendarImageViewClickListener = object:View.OnClickListener{
        override fun onClick(v: View?) {
            when(v?.id){
                R.id.dueDateImageView,R.id.dueDateTextInputEditText->{
                    showCalendar(GET_DUE_DATE_REQUEST)
                }
                R.id.untilImageView,R.id.untilTextInputEditText->{
                    showCalendar(GET_UNTIL_DATE_REQUEST)
                }
            }

        }
    }
    private val oneTimeOrRecurringRadioGroupOnChangeCheckedListener =  object:RadioGroup.OnCheckedChangeListener{
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when(checkedId){
                    R.id.recurringRadioButton->{
                        mFragmentAddBillBinding.recurringBillIncludeLayout.recurringParentElementConstraintLayout.visibility = View.VISIBLE
                    }
                    R.id.onetimeRadioButton->{
                        mFragmentAddBillBinding.recurringBillIncludeLayout.recurringParentElementConstraintLayout.visibility = View.GONE
                    }
                }
        }
    }
    private val recurringConditionRadioGroupOnChangeCheckedListener =  object:RadioGroup.OnCheckedChangeListener{
        override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
            when(checkedId){
                R.id.untilRadioButton->{
                    Toast.makeText(requireContext(),"untilRadioButton",Toast.LENGTH_SHORT).show()
                    mFragmentAddBillBinding.recurringBillIncludeLayout.timesTextInputEditText.setText("")
                    showCalendar(GET_UNTIL_DATE_REQUEST)
                }
                R.id.forRadioButton->{
                    Toast.makeText(requireContext(),"forRadioButton",Toast.LENGTH_SHORT).show()

                    mFragmentAddBillBinding.recurringBillIncludeLayout.untilTextInputEditText.setText("")

                }
            }
        }
    }

    var mIsUserAction = false
    private val repeatSpinnerOnItemSelectedListener = object :AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            if(!mIsUserAction){
                mIsUserAction = true
                return
            }

            var selectedRepeat = requireContext().resources.getStringArray(R.array.bill_recurring)[position]
            when(selectedRepeat){
                requireContext().getString(R.string.end_of_month)->{
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.setText("")
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.isEnabled = false
                }
                requireContext().getString(R.string.date_of_month)->{
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.filters = arrayOf(MinMaxInputFilter( 1 , 31))
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.isEnabled = true

                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.requestFocus()

                    var dateInMonthDialogFragment = DateInMonthDialogFragment()
                    dateInMonthDialogFragment.setDateSelectedListener(dateInMonthDialogFragmentDateSelectedListener)
                    dateInMonthDialogFragment.show(childFragmentManager,"DateInMonthDialogFragment")

                }
                else->{
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.isEnabled = true
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.filters = arrayOf()
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.requestFocus()


                }
            }

        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }
    private val dateInMonthDialogFragmentDateSelectedListener = object: DateInMonthDialogFragment.DateSelectedListener{
        override fun dateSelected(date: String) {

            mFragmentAddBillBinding.recurringBillIncludeLayout.repeatEveryTextInputEditText.setText(date)
        }

    }
    companion object {
        const val GET_DUE_DATE_REQUEST = 1
        const val GET_UNTIL_DATE_REQUEST = 2

        @JvmStatic fun newInstance(param1: String, param2: String) =
            AddBillFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    inner class BillNameAutoSuggestCustomAdapter(context: Context, billNames:List<String>): ArrayAdapter<String>(context,0,billNames){
        private var filter  = object: Filter(){
            private var searchJob: Job? = null
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val results = FilterResults()
                searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val searchTerm = if (constraint == null) "" else constraint.toString()
                    val searchResults = AllHomeDatabase.getDatabase(context).getBillItemDAO().searchDistinctNamesCaseSensitive(searchTerm)

                    withContext(Dispatchers.Main) {
                        results.apply {
                            values = searchResults
                            count = searchResults.size
                        }
                        publishResults(constraint, results)
                    }
                }
                return results
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                addAll(results.values as ArrayList<String>)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue.toString()
            }

        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView: TextView? = if(convertView == null){
                LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                convertView as TextView?
            }
            val billName = getItem(position)
            textView!!.text = billName

            return textView
        }
    }

    inner class BillCategoryAutoSuggestCustomAdapter(context: Context, billCategories:List<String>): ArrayAdapter<String>(context,0,billCategories){
        private var filter  = object: Filter(){
            private var searchJob: Job? = null
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                searchJob?.cancel()
                val results = FilterResults()
                searchJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val searchTerm = if (constraint == null) "" else constraint.toString()
                    val searchResults = AllHomeDatabase.getDatabase(context).getBillCategoryDAO().searchDistinctNamesCaseSensitive(searchTerm)

                    withContext(Dispatchers.Main) {
                        results.apply {
                            values = searchResults
                            count = searchResults.size
                        }
                        publishResults(constraint, results)
                    }
                }
                return results
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.values == null){
                    return
                }
                clear()
                addAll(results.values as ArrayList<String>)
            }
            override fun convertResultToString(resultValue: Any?): CharSequence {
                return resultValue.toString()
            }

        }
        override fun getFilter(): Filter {
            return filter
        }
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView: TextView? = if(convertView == null){
                LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false) as TextView?
            }else{
                convertView as TextView?
            }
            val billName = getItem(position)
            textView!!.text = billName

            return textView
        }
    }
}