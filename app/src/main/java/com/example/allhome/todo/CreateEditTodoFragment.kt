package com.example.allhome.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.bill.DateInMonthDialogFragment
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.databinding.FragmentCreateEditTodoBinding
import com.example.allhome.expenses.viewmodel.ExpensesFragmentViewModelViewModelFactory
import com.example.allhome.todo.AddSubTaskDialogFragment.OnSubTaskSavedListener
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModelFactory
import com.example.allhome.utils.MinMaxInputFilter
import org.w3c.dom.Text
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

        val database = (context?.applicationContext as AllHomeBaseApplication).database
        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        val todoSubTasksDAO = (context?.applicationContext as AllHomeBaseApplication).todoSubTasksDAO

        CreateEditTodoFragmentViewModelFactory(database,todosDAO,todoSubTasksDAO)

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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentCreateEditTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_edit_todo,null,false)
        val toolbar = mFragmentCreateEditTodoBinding.toolbar
        toolbar.title = "Create To Do"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            activity?.finish()
        }

        toolbar.inflateMenu(R.menu.create_edit_todo_menu)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.save_menu->{
                    saveTodo()
                }
                R.id.update_menu->{

                }
            }
            true
        }
        mFragmentCreateEditTodoBinding.addSubmenuLinearLayout.setOnClickListener {
            val addSubTaskDialogFragment = AddSubTaskDialogFragment(object: OnSubTaskSavedListener{
                override fun onSubTaskSaved(subTask: String) {

                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDatetime: String = simpleDateFormat.format(Date())
                    var uniqueId = UUID.randomUUID().toString()

                    val todoSubTasksEntity = TodoSubTasksEntity(
                        uniqueId = uniqueId,
                        todoUniqueId="",
                        name = subTask,
                        itemStatus = 0,
                        uploaded = 0,
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    mCreateEditTodoFragmentViewModel.mTodoSubTask.add(todoSubTasksEntity)
                    Log.e("COUNT","${mCreateEditTodoFragmentViewModel.mTodoSubTask.size}")

                }
            })
            addSubTaskDialogFragment.isCancelable = false
            addSubTaskDialogFragment.show(requireActivity().supportFragmentManager,"AddSubTaskDialogFragment")

        }

        mFragmentCreateEditTodoBinding.repeatSpinner.onItemSelectedListener = repeatSpinnerOnItemSelectedListener
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

        mCreateEditTodoFragmentViewModel.mSaveSuccessfully.observe(viewLifecycleOwner) { isSuccess ->

            if(isSuccess){
                Toast.makeText(requireContext(),"saved successfully",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),"failed to save",Toast.LENGTH_SHORT).show()
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
    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveTodo(){
        val taskName = mFragmentCreateEditTodoBinding.taskNameTextInputEditText.text.toString()
        val repeatEvery = if(mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().toInt() else 0
        val repeatEveryType = mFragmentCreateEditTodoBinding.repeatSpinner.selectedItem.toString()
        val notifyEvery= if(mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().toInt() else 0
        val notifyEveryType = mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
        val dueDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.time) else "0000-00-00 00:00:00"
        val repeatUntilDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value?.time) else "0000-00-00 00:00:00"
        var taskUniqueId = UUID.randomUUID().toString()
        var taskUniqueGroupId = UUID.randomUUID().toString()

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        val todosEntity = TodoEntity(
            uniqueId = taskUniqueId,
            groupUniqueId=taskUniqueGroupId,
            name = taskName,
            dueDate = dueDateTimeFormatted,
            repeatEvery = repeatEvery,
            repeatEveryType = repeatEveryType,
            repeatUntil = repeatUntilDateTimeFormatted,
            notifyEvery = notifyEvery,
            notifyEveryType = notifyEveryType,
            itemStatus = TodoEntity.NOT_DELETED_STATUS,
            uploaded = TodoEntity.NOT_UPLOADED,
            created = currentDatetime,
            modified = currentDatetime
        )
        if(mCreateEditTodoFragmentViewModel.mTodoSubTask.size > 0){
            // add todo unique id to sub todo's
            mCreateEditTodoFragmentViewModel.mTodoSubTask.forEach {
                it.todoUniqueId = taskUniqueId
            }
        }
        mCreateEditTodoFragmentViewModel.saveTodo(todosEntity)


    }
    private val repeatSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            var selectedRepeat = requireContext().resources.getStringArray(R.array.todo_recurring)[position]
            when(selectedRepeat){
                requireContext().getString(R.string.year)->{
                    Toast.makeText(requireContext(),"YEAR",Toast.LENGTH_SHORT).show()
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.date_of_month)->{
                    Toast.makeText(requireContext(),"DATE OF MONTH",Toast.LENGTH_SHORT).show()
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.end_of_month)->{
                    Toast.makeText(requireContext(),"END OF MONTH",Toast.LENGTH_SHORT).show()
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.month)->{
                    Toast.makeText(requireContext(),"MONTH",Toast.LENGTH_SHORT).show()
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.week)->{
                    Toast.makeText(requireContext(),"WEEK",Toast.LENGTH_SHORT).show()
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.day)->{
                    Toast.makeText(requireContext(),"DAY",Toast.LENGTH_SHORT).show()
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.none)->{
                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.GONE
                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.GONE

                    Toast.makeText(requireContext(),"NONE",Toast.LENGTH_SHORT).show()
                }

            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

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