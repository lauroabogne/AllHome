package com.example.allhome.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.allhome.R
import com.example.allhome.databinding.FragmentCreateEditTodoBinding
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateEditTodoFragment : Fragment() {

    private val TAG = "CreateEditTodoFragment"
    private val SHOW_CALENDAR_FOR_DUE_DATE = 1
    private val SHOW_CALENDAR_FOR_REPEAT_UNTIL = 2
    private var showCalendarFor = SHOW_CALENDAR_FOR_DUE_DATE

    private val mCreateEditTodoFragmentViewModel: CreateEditTodoFragmentViewModel by viewModels{
        CreateEditTodoFragmentViewModelFactory()

    }

    lateinit var mFragmentCreateEditTodoBinding: FragmentCreateEditTodoBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mFragmentCreateEditTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_edit_todo,null,false)
        val toolbar = mFragmentCreateEditTodoBinding.toolbar
        toolbar.title = "Create To Do"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            activity?.finish()
        }

        toolbar.inflateMenu(R.menu.create_edit_todo_menu)

        mFragmentCreateEditTodoBinding.addSubmenuLinearLayout.setOnClickListener {
            val addSubTaskDialogFragment = AddSubTaskDialogFragment()
            addSubTaskDialogFragment.isCancelable = false
            addSubTaskDialogFragment.show(requireActivity().supportFragmentManager,"AddSubTaskDialogFragment")

        }

        mFragmentCreateEditTodoBinding.dueDateImageView.setOnClickListener {
            showCalendarFor = SHOW_CALENDAR_FOR_DUE_DATE
            showCalendar()
        }
        mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setOnClickListener {
            showCalendarFor = SHOW_CALENDAR_FOR_DUE_DATE
            showCalendar()
        }
        mFragmentCreateEditTodoBinding.repeatUntilImageView.setOnClickListener {
            showCalendarFor = SHOW_CALENDAR_FOR_REPEAT_UNTIL
            showCalendar()
        }
        mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setOnClickListener {
            showCalendarFor = SHOW_CALENDAR_FOR_REPEAT_UNTIL
            showCalendar()
        }

        mCreateEditTodoFragmentViewModel.mDueDateCalendar.observe(viewLifecycleOwner) { calendar ->

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            if(hour == 0 && minutes == 0 ){
                val dueDateString = SimpleDateFormat("MMM dd, y").format(calendar.time)
                mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setText(dueDateString)
            }else{
                val dueDateString = SimpleDateFormat("MMM dd, y hh:mm:ss a").format(calendar.time)
                mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setText(dueDateString)
            }

        }

        mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.observe(viewLifecycleOwner) { calendar ->

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            if(hour == 0 && minutes == 0 ){
                val dueDateString = SimpleDateFormat("MMM dd, y").format(calendar.time)
                mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setText(dueDateString)
            }else{
                val dueDateString = SimpleDateFormat("MMM dd, y hh:mm:ss a").format(calendar.time)
                mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setText(dueDateString)
            }

        }

        return mFragmentCreateEditTodoBinding.root
    }

    private fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date? = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val stringDateForSaving = SimpleDateFormat("yyyy-MM-dd").format(date)

            showTimePicker(stringDateForSaving)
        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    private fun showTimePicker(date: String){
        val calendar = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            val selectedDateTimeString = date +" "+ SimpleDateFormat("HH:mm:00").format(calendar.time)
            val selectedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:00").parse(selectedDateTimeString)

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = selectedDateTime

            if(showCalendarFor == SHOW_CALENDAR_FOR_DUE_DATE){
                mCreateEditTodoFragmentViewModel.mDueDateCalendar.value = selectedCalendar
            }else{
                mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value = selectedCalendar
            }


        }
        val timePickerDialog = TimePickerDialog(requireContext(), timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No time", DialogInterface.OnClickListener { dialog, which ->

            val selectedDateTime = SimpleDateFormat("yyyy-MM-dd 00:00:00").parse("$date 00:00:00 ")

            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.time = selectedDateTime

            if(showCalendarFor == SHOW_CALENDAR_FOR_DUE_DATE){
                mCreateEditTodoFragmentViewModel.mDueDateCalendar.value = selectedCalendar
            }else{
                mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value = selectedCalendar

            }


        })
        timePickerDialog.show()

    }
    companion object {
        @JvmStatic fun newInstance(param1: String, param2: String) =
            CreateEditTodoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}