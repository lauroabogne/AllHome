package com.example.allhome.bill

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.room.ColumnInfo
import com.example.allhome.R
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.databinding.FragmentAddBillBinding
import com.example.allhome.global_ui.CustomMessageDialogFragment
import com.example.allhome.meal_planner.AddMealDialogFragment
import com.example.allhome.meal_planner.viewmodel.MealPlannerViewModel
import com.example.allhome.storage.PantryItemRecyclerViewAdapter
import com.example.allhome.utils.MinMaxInputFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)

        mBillViewModel = ViewModelProvider(this).get(BillViewModel::class.java)

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


        return mFragmentAddBillBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.create_bill_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
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
        return super.onOptionsItemSelected(item)
    }

    fun clickCalendar(view:View){


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

        Toast.makeText(requireContext(),"Recurring",Toast.LENGTH_SHORT).show()

    }
    private fun saveBillWithEndDate(){


        val billAmountString = mFragmentAddBillBinding.billAmountTextinput.text.toString().trim()
        val billAmountDouble = if(billAmountString.isNullOrEmpty()) 0.0 else billAmountString.toDouble()

        val billName = mFragmentAddBillBinding.billNameTextInput.text.toString().trim()

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
            mBillViewModel.addBills(requireContext(),billEntities)
            withContext(Main){

            }
        }


    }
    private fun saveBillWithoutEndDate(){

        val billAmountString = mFragmentAddBillBinding.billAmountTextinput.text.toString().trim()
        val billAmountDouble = if(billAmountString.isNullOrEmpty()) 0.0 else billAmountString.toDouble()

        val billName = mFragmentAddBillBinding.billNameTextInput.text.toString().trim()

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


        val repeatTimesString = mFragmentAddBillBinding.recurringBillIncludeLayout.timesTextInputEditText.text.toString().trim()
        val repeatTimesInt = if(repeatTimesString.isEmpty()) 0 else repeatTimesString.toInt()

        val dueDateCalendar = Calendar.getInstance()
        dueDateCalendar.time = SimpleDateFormat("yyyy-MM-dd").parse(mDueDate)

        val billEntities = arrayListOf<BillEntity>()
        var billsGroupUniqueId = UUID.randomUUID().toString()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())


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

        Log.e("BILLS",billEntities.toString())
        mBillViewModel.mCoroutineScope.launch {
            mBillViewModel.addBills(requireContext(),billEntities)
            withContext(Main){

            }
        }
    }
    private fun saveOnetimeBill(){

        val billAmountString = mFragmentAddBillBinding.billAmountTextinput.text.toString().trim()
        val billAmountDouble = if(billAmountString.isNullOrEmpty()) 0.0 else billAmountString.toDouble()

        val billName = mFragmentAddBillBinding.billNameTextInput.text.toString().trim()
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
            val id = mBillViewModel.addBill(requireContext(),billEntity)
            withContext(Main){

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
                    dateInMonthDialogFragment.mDateSelectedListener = dateInMonthDialogFragmentDateSelectedListener
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
    private val dateInMonthDialogFragmentDateSelectedListener = object:DateInMonthDialogFragment.DateSelectedListener{
        override fun dateSelected(date: String) {
            Toast.makeText(requireContext(),"date selected ${date}",Toast.LENGTH_SHORT).show()
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
}