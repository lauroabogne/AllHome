package com.example.allhome.bill

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import com.example.allhome.R
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.data.entities.StorageItemExpirationEntity
import com.example.allhome.databinding.FragmentAddBillBinding
import com.example.allhome.meal_planner.AddMealDialogFragment
import com.example.allhome.storage.PantryItemRecyclerViewAdapter
import com.example.allhome.utils.MinMaxInputFilter
import kotlinx.coroutines.Dispatchers
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

    var mDueDate:String? = null
    var mRepeatUntilDate:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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





        mFragmentAddBillBinding.saveBtn.setOnClickListener {
            when(mFragmentAddBillBinding.oneTimeOrRecurringRadioGroup.checkedRadioButtonId){
                R.id.recurringRadioButton->{

                    saveRecurring()
                }
                R.id.onetimeRadioButton->{
                    Toast.makeText(requireContext(),"one time",Toast.LENGTH_SHORT).show()
                }
            }

        }
        return mFragmentAddBillBinding.root
    }
    fun clickCalendar(view:View){


    }
    fun saveRecurring(){

        val repeatCountString =mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.text.toString().trim()
        val repeatCountInt = if(repeatCountString.isEmpty()) 0 else repeatCountString.toInt()
        val repeatEvery =mFragmentAddBillBinding.recurringBillIncludeLayout.repeatSpinner.selectedItem


        when(mFragmentAddBillBinding.recurringBillIncludeLayout.recurringConditionRadioGroup.checkedRadioButtonId){
            R.id.untilRadioButton->{
                val untilDateCalendar = Calendar.getInstance()
                untilDateCalendar.time = SimpleDateFormat("yyyy-MM-dd").parse(mRepeatUntilDate)

            }
            R.id.forRadioButton->{

                val repeatTimesString = mFragmentAddBillBinding.recurringBillIncludeLayout.timesTextInputEditText.text.toString().trim()
                val repeatTimesInt = if(repeatTimesString.isEmpty()) 0 else repeatTimesString.toInt()

                val dueDateCalendar = Calendar.getInstance()
                dueDateCalendar.time = SimpleDateFormat("yyyy-MM-dd").parse(mDueDate)


                repeat(repeatTimesInt){
                    when(repeatEvery){
                        requireContext().getString(R.string.day)->{

                            dueDateCalendar.add(Calendar.DAY_OF_MONTH,repeatCountInt)
                            val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                            Log.e("FORMATED_DATE",formatedDate)

                        }
                        requireContext().getString(R.string.week)->{
                            dueDateCalendar.add(Calendar.WEEK_OF_MONTH,repeatCountInt)
                            val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                            Log.e("FORMATED_WEEK",formatedDate)
                        }
                        requireContext().getString(R.string.month)->{
                            dueDateCalendar.add(Calendar.MONTH,repeatCountInt)
                            val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                            Log.e("FORMATED_MONTH",formatedDate)
                        }
                        requireContext().getString(R.string.end_of_month)->{
                            dueDateCalendar.add(Calendar.MONTH,1)
                            dueDateCalendar.set(Calendar.DAY_OF_MONTH,dueDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))

                            val formatedDate = SimpleDateFormat("yyyy-MM-dd").format(dueDateCalendar.time)

                            Log.e("FORMATED_MONTH",formatedDate)
                        }
                        requireContext().getString(R.string.year)->{

                        }
                    }
                }

            }
        }
        Toast.makeText(requireContext(),"Recurring",Toast.LENGTH_SHORT).show()

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
    private val repeatSpinnerOnItemSelectedListener = object :AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            var selectedRepeat = requireContext().resources.getStringArray(R.array.bill_recurring)[position]
            when(selectedRepeat){
                requireContext().getString(R.string.end_of_month)->{
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.setText("")
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.isEnabled = false
                }
                requireContext().getString(R.string.date_in_month)->{
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.filters = arrayOf(MinMaxInputFilter( 1 , 31))
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.isEnabled = true

                    var dialog = DateInMonthDialogFragment()
                    dialog.setDateSelectedListener(dateInMonthDialogFragmentDateSelectedListener)
                    dialog.show(childFragmentManager,"DateInMonthDialogFragment")
                }
                else->{
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.isEnabled = true
                    mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.filters = arrayOf()


                }
            }

        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }
    private val dateInMonthDialogFragmentDateSelectedListener = object:DateInMonthDialogFragment.DateSelectedListener{
        override fun dateSelected(date: String) {
            Toast.makeText(requireContext(),"date selected ${date}",Toast.LENGTH_SHORT).show()
            mFragmentAddBillBinding.recurringBillIncludeLayout.repeatCountTextInputEditText.setText(date)
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