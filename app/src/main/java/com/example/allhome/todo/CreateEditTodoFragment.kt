package com.example.allhome.todo

import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import androidx.room.withTransaction
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.NotificationReceiver
import com.example.allhome.R
import com.example.allhome.data.entities.AlarmRecordsEntity
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoChecklistEntity
import com.example.allhome.databinding.FragmentCreateEditTodo2Binding
import com.example.allhome.databinding.TodoItemSubTaskBinding
import com.example.allhome.global_ui.DateInMonthDialogFragment
import com.example.allhome.todo.AddEditSubTaskDialogFragment.OnSubTaskSavedListener
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModelFactory
import com.example.allhome.utils.CustomAlarmManager
import com.example.allhome.utils.DateUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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
        val alarmRecordsDAO = (context?.applicationContext as AllHomeBaseApplication).alarmsRecordsDAO



        CreateEditTodoFragmentViewModelFactory(database,todosDAO,todoSubTasksDAO,alarmRecordsDAO)

    }
    lateinit var mFragmentCreateEditTodoBinding: FragmentCreateEditTodo2Binding
    private var mUpdateTodoOptionDialogFragment:UpdateTodoOptionDialogFragment? = null
    companion object {
        const val TODO_UNIQUE_ID_TAG = "TODO_UNIQUE_ID_TAG"
        const val ACTION_TAG = "ACTION_TAG"
        const val ACTION_CREATE = 1
        const val ACTION_EDIT = 2
        var mAction = ACTION_CREATE

        @JvmStatic fun newInstance(todoUniqueId: String) =
            CreateEditTodoFragment().apply {
                arguments = Bundle().apply {
                    putString(TODO_UNIQUE_ID_TAG, todoUniqueId)
                    putInt(ACTION_TAG, ACTION_EDIT)

                }
            }
        @JvmStatic fun newInstance() =
            CreateEditTodoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ACTION_TAG, ACTION_CREATE)

                }
            }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {

            mAction = it.getInt(ACTION_TAG)
            if(mAction == ACTION_EDIT){
                val uniqueId = it.getString(TODO_UNIQUE_ID_TAG)
                mCreateEditTodoFragmentViewModel.mTodoUniqueId.value = uniqueId
            }
        }


    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mFragmentCreateEditTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_edit_todo_2,null,false)
        val toolbar = mFragmentCreateEditTodoBinding.toolbar
        toolbar.title = if(mAction == ACTION_CREATE) "Create Todo" else "Edit Todo"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener { activity?.finish() }
        toolbar.inflateMenu(R.menu.create_edit_todo_menu)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.save_menu->{
                    /**
                     * @todo temporary disable
                     */
                    saveTodo()
                }
                R.id.update_menu->{
                    mCreateEditTodoFragmentViewModel.mUpdateTask.value = true
                }
            }
            true
        }

        //hide or show option menu
        toolbar.menu.findItem(R.id.update_menu).isVisible = mAction == ACTION_EDIT
        toolbar.menu.findItem(R.id.save_menu).isVisible = mAction == ACTION_CREATE

        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview.addItemDecoration(decorator)

        val todoSubTaskListRecyclerviewAdapter = TodoSubTaskListRecyclerviewAdapter()
        mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview.adapter = todoSubTaskListRecyclerviewAdapter
        mFragmentCreateEditTodoBinding.addChecklistBtn.setOnClickListener {
            val addSubTaskDialogFragment = AddEditSubTaskDialogFragment(object: OnSubTaskSavedListener{
                override fun onSubTaskSaved(subTask: String) {

                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    val currentDatetime: String = simpleDateFormat.format(Date())
                    var uniqueId = UUID.randomUUID().toString()

                    val todoSubTasksEntity = TodoChecklistEntity(
                        uniqueId = uniqueId,
                        todoUniqueId="",
                        name = subTask,
                        itemStatus = 0,
                        uploaded = 0,
                        isFinished = 0,
                        datetimeFinished = "",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value!!.add(todoSubTasksEntity)
                    (mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview.adapter as TodoSubTaskListRecyclerviewAdapter).setData(mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value!!)
                    (mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview.adapter as TodoSubTaskListRecyclerviewAdapter).notifyDataSetChanged()
                }
            })
            addSubTaskDialogFragment.isCancelable = false
            addSubTaskDialogFragment.show(requireActivity().supportFragmentManager,"AddSubTaskDialogFragment")

        }

        mFragmentCreateEditTodoBinding.dueDateLinearLayout.setOnClickListener{
            showCalendarFor = SHOW_CALENDAR_FOR_DUE_DATE
            showCalendar()
        }
        mFragmentCreateEditTodoBinding.repeatEveryLinearLayout.setOnClickListener{

            val todoRepeatDaysDialogFragment = TodoRepeatEveryDialogFragment()
            todoRepeatDaysDialogFragment.setOnItemClickListener(object:TodoRepeatEveryDialogFragment.OnItemClickListener{
                override fun onItemClick(selectedOption: String) {


                    when(selectedOption) {
                        requireContext().getString(R.string.none)->{
                            mCreateEditTodoFragmentViewModel.mRepeatEvery.value = 0
                            mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedOption
                        }
                        requireContext().getString(R.string.day)->{

                            showDialogForRepeatEveryDay(selectedOption)


                        }
                        requireContext().getString(R.string.week)->{
                            showDialogForRepeatEveryWeek(selectedOption)
                        }
                        requireContext().getString(R.string.month)->{
                            showDialogForRepeatMonthly(selectedOption)

                        }
                        requireContext().getString(R.string.end_of_month)->{

                            mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedOption

                        }
                        requireContext().getString(R.string.date_of_month)->{
                            showDialogForRepeatByMonthDate(selectedOption)

                        }
                        requireContext().getString(R.string.year)->{
                            showDialogForRepeatYearly(selectedOption)
                        }
                    }

                }

            })
            todoRepeatDaysDialogFragment.show(requireActivity().supportFragmentManager,"todo_repeat_dialog")

        }
        mFragmentCreateEditTodoBinding.repeatEndAtLinearLayout.setOnClickListener{
            showCalendarFor = SHOW_CALENDAR_FOR_REPEAT_UNTIL
            showCalendar()
        }
        mFragmentCreateEditTodoBinding.notifyAtLinearLayout.setOnClickListener{
            val notifyAt = if(mCreateEditTodoFragmentViewModel.mNotifyAt.value != null) mCreateEditTodoFragmentViewModel.mNotifyAt.value!! else 0
            val notifyEveryType = if(mCreateEditTodoFragmentViewModel.mNotifyEveryType.value != null) mCreateEditTodoFragmentViewModel.mNotifyEveryType.value!! else requireContext().resources.getString(R.string.grocery_notification_minute_before)
            var notifyAtDialogFragment = NotifyAtDialogFragment(notifyAt, notifyEveryType)
            notifyAtDialogFragment.setOnNotifySetListener(object:NotifyAtDialogFragment.OnNotifySetListener{
                override fun notifySet(notifyAt: Int, notifyType: String) {
                    val sameDayAndTimeNotification =requireContext().resources.getString(R.string.grocery_notification_same_day_and_time)
                    val noNotification =  requireContext().resources.getString(R.string.grocery_notification_none)
                    if( ((notifyType != sameDayAndTimeNotification) and (notifyType != noNotification)) and (notifyAt <=0)){

                        Toast.makeText(requireContext(),"Notify at must value is greater than 0 $notifyType $notifyAt",Toast.LENGTH_SHORT).show()
                        return
                    }

                    notifyAtDialogFragment.dismiss()

                    if(notifyType == requireContext().resources.getString(R.string.none)){
                        mCreateEditTodoFragmentViewModel.mNotifyAt.value = 0
                        mCreateEditTodoFragmentViewModel.mNotifyEveryType.value = requireContext().resources.getString(R.string.none)
                        return
                    }
                    mCreateEditTodoFragmentViewModel.mNotifyAt.value = notifyAt
                    mCreateEditTodoFragmentViewModel.mNotifyEveryType.value = notifyType
                }

            })

            notifyAtDialogFragment.show(childFragmentManager,"notify_at_dialog")
        }


//        mFragmentCreateEditTodoBinding.repeatSpinner.onItemSelectedListener = repeatSpinnerOnItemSelectedListener
//        mFragmentCreateEditTodoBinding.dueDateImageView.setOnClickListener {
//            showCalendarFor = SHOW_CALENDAR_FOR_DUE_DATE
//            showCalendar()
//        }
//        mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setOnClickListener {
//            showCalendarFor = SHOW_CALENDAR_FOR_DUE_DATE
//            showCalendar()
//        }
//        mFragmentCreateEditTodoBinding.repeatUntilImageView.setOnClickListener {
//            showCalendarFor = SHOW_CALENDAR_FOR_REPEAT_UNTIL
//            showCalendar()
//        }
//        mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setOnClickListener {
//            showCalendarFor = SHOW_CALENDAR_FOR_REPEAT_UNTIL
//            showCalendar()
//        }

        mCreateEditTodoFragmentViewModel.mDueDateCalendar.observe(viewLifecycleOwner) { calendar ->
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            if(hour == 0 && minutes == 0 ){
                val dueDateString = SimpleDateFormat("MMM dd, y").format(calendar.time)
                //mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setText(dueDateString)
                mFragmentCreateEditTodoBinding.dueDateTextView.text = dueDateString
            }else{
                val dueDateString = SimpleDateFormat("MMM dd, y hh:mm:ss a").format(calendar.time)
               // mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setText(dueDateString)
                mFragmentCreateEditTodoBinding.dueDateTextView.text = dueDateString
            }

        }
        mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.observe(viewLifecycleOwner) { calendar ->

            if (calendar == null) {
                return@observe
            }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            if (hour == 0 && minutes == 0) {
                val dueDateString = SimpleDateFormat("MMM dd, y").format(calendar.time)
                //mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setText(dueDateString)
                mFragmentCreateEditTodoBinding.repeatEndAtTextView.text = dueDateString
            } else {
                val dueDateString = SimpleDateFormat("MMM dd, y hh:mm:ss a").format(calendar.time)
                //mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setText(dueDateString)
                mFragmentCreateEditTodoBinding.repeatEndAtTextView.text = dueDateString
            }
        }
        mCreateEditTodoFragmentViewModel.mSaveSuccessfully.observe(viewLifecycleOwner) { isSuccess ->

            if(isSuccess){

                val intent = Intent()
                intent.putExtra(TodoFragment.ACTION_TAG, TodoFragment.RELOAD_ACTION_TAG)
                activity?.setResult(Activity.RESULT_OK, intent)
                activity?.finish()
            }else{
                Toast.makeText(requireContext(),"failed to save",Toast.LENGTH_SHORT).show()
            }

        }
        mCreateEditTodoFragmentViewModel.mTodoUniqueId.observe(viewLifecycleOwner){
            getTodoInformation(it)
        }
        mCreateEditTodoFragmentViewModel.mTodoName.observe(viewLifecycleOwner){
            mFragmentCreateEditTodoBinding.taskNameTextInputEditText.setText(it)
        }
        mCreateEditTodoFragmentViewModel.mTodoDescription.observe(viewLifecycleOwner){
            mFragmentCreateEditTodoBinding.taskDescriptionTextInputEditText.setText(it)
        }
        mCreateEditTodoFragmentViewModel.mDueDateCalendar.observe(viewLifecycleOwner){dueDate->

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dueDateTimeString: String = simpleDateFormat.format(dueDate.time)

            if(dueDateTimeString.contains(" 00:00:00")){
                val dueDateString = SimpleDateFormat("MMMM dd, y").format(dueDate.time)
                //mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setText(dueDateString)
            }else{
                val dueDateString = SimpleDateFormat("MMMM dd, y hh:mm:ss a").format(dueDate.time)
                //mFragmentCreateEditTodoBinding.dueDateTextInputEditText.setText(dueDateString)
            }

        }
        mCreateEditTodoFragmentViewModel.mRepeatEvery.observe(viewLifecycleOwner){
            //mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.setText(if(it==0) "" else it.toString())
        }
        mCreateEditTodoFragmentViewModel.mRepeatEveryType.observe(viewLifecycleOwner){

            val indexOfSelectedRepeat = context?.resources?.getStringArray(R.array.todo_recurring)?.indexOf(it)
            if (indexOfSelectedRepeat != null) {
                //mFragmentCreateEditTodoBinding.repeatSpinner.setSelection(indexOfSelectedRepeat)
            }

            repeatEveryDisplay(it)
        }
        mCreateEditTodoFragmentViewModel.mWeekDaysSelected.observe(viewLifecycleOwner){

            mCreateEditTodoFragmentViewModel.mRepeatEveryType.value?.let {
                repeatEveryDisplay(it)
            }

        }

        mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.observe(viewLifecycleOwner){repeatUntil->

            if(repeatUntil == null){
                return@observe
            }

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val dueDateTimeString: String = simpleDateFormat.format(repeatUntil.time)

            if(dueDateTimeString.contains(" 00:00:00")){
                val repeatUntilDateString = SimpleDateFormat("MMMM dd, y").format(repeatUntil.time)
                //mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setText(repeatUntilDateString)
            }else{
                val repeatUntilDateString = SimpleDateFormat("MMMM dd, y hh:mm:ss a").format(repeatUntil.time)
                //mFragmentCreateEditTodoBinding.repeatUntilDateTextInputEditText.setText(repeatUntilDateString)
            }

        }
        mCreateEditTodoFragmentViewModel.mNotifyAt.observe(viewLifecycleOwner){
            //mFragmentCreateEditTodoBinding.notifyTextInputEditText.setText(if(it==0) "" else it.toString())
        }
        mCreateEditTodoFragmentViewModel.mNotifyEveryType.observe(viewLifecycleOwner){
            val indexOfAlarmOption = context?.resources?.getStringArray(R.array.todo_alarm_options)?.indexOf(it)
            if (indexOfAlarmOption != null) {
                //mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.setSelection(indexOfAlarmOption)
            }
           // Toast.makeText(requireContext(),"Notification",Toast.LENGTH_SHORT).show()
            val notifyValue = mCreateEditTodoFragmentViewModel.mNotifyAt.value
            notifyAtDisplay(notifyValue!!,it)

        }
        mCreateEditTodoFragmentViewModel.mTodoSubTask.observe(viewLifecycleOwner){

            val todoSubTaskListRecyclerviewAdapter = (mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview.adapter as TodoSubTaskListRecyclerviewAdapter)
            todoSubTaskListRecyclerviewAdapter.todoSubTasksEntities = it as ArrayList<TodoChecklistEntity>
            todoSubTaskListRecyclerviewAdapter.notifyDataSetChanged()

        }
        mCreateEditTodoFragmentViewModel.mUpdateTask.observe(viewLifecycleOwner){updating->
            if(updating){
                mCreateEditTodoFragmentViewModel.checkIfTodoIsRecurring(mCreateEditTodoFragmentViewModel.mGroupUniqueId?.value!! )
            }

        }
        mCreateEditTodoFragmentViewModel.mDoTaskNeedToUpdateIsRecurring.observe(viewLifecycleOwner){isRecurring->
            if(isRecurring){
                mUpdateTodoOptionDialogFragment = UpdateTodoOptionDialogFragment("","Selected task is recurring. What you want to update?")
                mUpdateTodoOptionDialogFragment?.setClickListener { view ->
                   // mUpdateTodoOptionDialogFragment?.dismiss()
                    val selectedRadioBtn = mUpdateTodoOptionDialogFragment?.getDeleteTodoDialogFragmentLayoutBinding()?.radioButtonGroup?.checkedRadioButtonId
                    when (view?.id) {
                        UpdateTodoOptionDialogFragment.POSITIVE_BTN_ID-> {
                            when(selectedRadioBtn){
                                R.id.selectedTaskOnlyBtn->{
                                    mCreateEditTodoFragmentViewModel.mUpdateSelectedTask.value = true
                                }
                                R.id.selectedAndAlsoFutureTaskBtn->{
                                    mCreateEditTodoFragmentViewModel.mUpdateFutureAndSelectedTask.value = true
                                }
                            }
                        }
                    }
                }

                mUpdateTodoOptionDialogFragment?.show(childFragmentManager,"UpdateTodoOptionDialogFragment")
            }else{
                mCreateEditTodoFragmentViewModel.mUpdateSelectedTask.value = true
            }
        }
        mCreateEditTodoFragmentViewModel.mUpdateSelectedTask.observe(viewLifecycleOwner){update->

            if(update){
                updateTodo()
            }

        }
        mCreateEditTodoFragmentViewModel.mUpdateFutureAndSelectedTask.observe(viewLifecycleOwner){update->
            if(update){
                updateTodos()
            }
        }
        val mItemTouchHelper = ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0){

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition

                Collections.swap(todoSubTaskListRecyclerviewAdapter.todoSubTasksEntities,sourcePosition,targetPosition)
                todoSubTaskListRecyclerviewAdapter.notifyItemMoved(sourcePosition, targetPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }

        })

        mItemTouchHelper.attachToRecyclerView(mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview)


        return mFragmentCreateEditTodoBinding.root
    }
    private fun getTodoInformation(todoUniqueId: String){
        mCreateEditTodoFragmentViewModel.getTodoInformation(requireContext(),todoUniqueId)
    }
    private fun showDialogForRepeatEveryDay(selectedRepeatEveryType:String){

        val repeatValue = if(mCreateEditTodoFragmentViewModel.mRepeatEveryType.value == requireContext().getString(R.string.day)) mCreateEditTodoFragmentViewModel.mRepeatEvery.value else 0
        val dialogFragment = TodoRepeatDaysDialogFragment( repeatValue, mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        dialogFragment.show(requireActivity().supportFragmentManager, "repeat_day_dialog")
        dialogFragment.setOnNumberOfDaysSetListener(object : TodoRepeatDaysDialogFragment.OnNumberOfDaysSetListener{
            override fun numberOfDays(repeat: Int, days: List<String>) {


                if(repeat <=0){

                    Toast.makeText(requireContext(),"Repeat every must greater than 0 value",Toast.LENGTH_SHORT).show()
                    return
                }
                dialogFragment.dismiss()


                mCreateEditTodoFragmentViewModel.mRepeatEvery.value = repeat
                mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value = days
                mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedRepeatEveryType


            }

        })
    }
    private fun showDialogForRepeatEveryWeek(selectedRepeatEveryType:String){

        val repeatValue = if(mCreateEditTodoFragmentViewModel.mRepeatEveryType.value == requireContext().getString(R.string.week)) mCreateEditTodoFragmentViewModel.mRepeatEvery.value else 0
        val dialogFragment = TodoRepeatWeekDialogFragment(repeatValue)
        dialogFragment.setOnNumberOfWeeksSetListener(object:TodoRepeatWeekDialogFragment.OnNumberOfWeeksSetListener{
            override fun numberOfWeeksAndDays(repeat: Int) {


               if(repeat <=0){
                   Toast.makeText(requireContext(),"Repeat every must greater than 0 value",Toast.LENGTH_SHORT).show()
                   return
               }

                mCreateEditTodoFragmentViewModel.mRepeatEvery.value = repeat
                mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedRepeatEveryType

                dialogFragment.dismiss()
            }

        })
        dialogFragment.show(requireActivity().supportFragmentManager, "repeat_week_dialog")


    }
    private fun showDialogForRepeatMonthly(selectedRepeatEveryType:String){

        val repeatValue = if(mCreateEditTodoFragmentViewModel.mRepeatEveryType.value == requireContext().getString(R.string.month)) mCreateEditTodoFragmentViewModel.mRepeatEvery.value else 0
        val dialogFragment = TodoRepeatMonthDialogFragment(repeatValue)
        dialogFragment.setOnNumberOfMonthsSetListener(object : TodoRepeatMonthDialogFragment.OnNumberOfMonthsSetListener{
            override fun numberOfMonths(repeat: Int) {
                if(repeat <= 0){
                    Toast.makeText(requireContext(),"Repeat every must greater than 0 value.",Toast.LENGTH_SHORT).show()
                    return
                }
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value = repeat
                mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedRepeatEveryType
                dialogFragment.dismiss()

            }

        })
        dialogFragment.show(requireActivity().supportFragmentManager, "repeat_month_dialog")
    }
    private fun showDialogForRepeatYearly(selectedRepeatEveryType:String){
        val repeatValue = if(mCreateEditTodoFragmentViewModel.mRepeatEveryType.value == requireContext().getString(R.string.year)) mCreateEditTodoFragmentViewModel.mRepeatEvery.value else 0
        val dialogFragment = TodoRepeatYearDialogFragment(repeatValue)
        dialogFragment.setOnNumberOfYearSetListener(object:TodoRepeatYearDialogFragment.OnNumberOfYearSetListener{
            override fun numberOfYear(repeat: Int) {
                if(repeat <= 0){
                    Toast.makeText(requireContext(),"Repeat every must greater than 0 value.",Toast.LENGTH_SHORT).show()
                    return
                }
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value = repeat
                mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedRepeatEveryType
                dialogFragment.dismiss()
            }

        })
        dialogFragment.show(requireActivity().supportFragmentManager, "repeat_month_dialog")
    }
    private fun showDialogForRepeatByMonthDate(selectedRepeatEveryType:String){
        var dateInMonthDialogFragment = DateInMonthDialogFragment()
        dateInMonthDialogFragment.setDateSelectedListener(object:DateInMonthDialogFragment.DateSelectedListener{
            override fun dateSelected(date: String) {
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value = date.toInt()
                mCreateEditTodoFragmentViewModel.mRepeatEveryType.value = selectedRepeatEveryType
            }

        })
        dateInMonthDialogFragment.show(childFragmentManager,"date_in_month_dialog")
    }


    private fun repeatEveryDisplay(selectedRepeat:String){

        val indexOfSelectedRepeat = context?.resources?.getStringArray(R.array.todo_recurring)?.indexOf(selectedRepeat)
        if (indexOfSelectedRepeat != null) {
            //mFragmentCreateEditTodoBinding.repeatSpinner.setSelection(indexOfSelectedRepeat)
        }
        when(selectedRepeat){
            requireContext().getString(R.string.none)->{
                mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "Not set"
            }
            requireContext().getString(R.string.day)->{
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value?.let{ it ->
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "$it "+(if(it > 1) "days"  else "day")
                    val selectedDaysString = generateWeekDaysDisplay()
                    if(selectedDaysString.isNotEmpty()){
                        mFragmentCreateEditTodoBinding.repeatEveryTextView.append(" ( ")
                        mFragmentCreateEditTodoBinding.repeatEveryTextView.append(selectedDaysString)
                        mFragmentCreateEditTodoBinding.repeatEveryTextView.append(" )")
                    }


                }?:run{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = ""
                }
            }

            requireContext().getString(R.string.week)->{
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value?.let{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "$it "+(if(it > 1) "weeks"  else "week")

                }?:run{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = ""
                }
            }
            requireContext().getString(R.string.month)->{
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value?.let{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "$it "+(if(it > 1) "months"  else "month")

                }?:run{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = ""
                }
            }

            requireContext().getString(R.string.date_of_month)->{
                mCreateEditTodoFragmentViewModel.mRepeatEvery.value?.let{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "$it of month"

                }?:run{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = ""
                }
            }
            requireContext().getString(R.string.end_of_month)->{

                mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "End of month"

            }
            requireContext().getString(R.string.year)->{

                mCreateEditTodoFragmentViewModel.mRepeatEvery.value?.let{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = "$it "+(if(it > 1) "years"  else "year")

                }?:run{
                    mFragmentCreateEditTodoBinding.repeatEveryTextView.text = ""
                }

            }
        }
    }
    private fun generateWeekDaysDisplay():String{

        val selectedDaysList = mutableListOf<String>()

        mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value?.let{ selectedDays->


            selectedDays.forEach{selectedDay->

                if(selectedDay == requireContext().resources.getString(R.string.monday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.monday_abbr))
                }
                if(selectedDay == requireContext().resources.getString(R.string.tuesday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.tuesday_abbr))
                }
                if(selectedDay == requireContext().resources.getString(R.string.wednesday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.wednesday_abbr))
                }
                if(selectedDay == requireContext().resources.getString(R.string.thursday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.thursday_abbr))
                }
                if(selectedDay == requireContext().resources.getString(R.string.friday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.friday_abbr))
                }
                if(selectedDay == requireContext().resources.getString(R.string.saturday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.saturday_abbr))
                }
                if(selectedDay == requireContext().resources.getString(R.string.sunday)){
                    selectedDaysList.add( requireContext().resources.getString(R.string.sunday_abbr))
                }

            }
        }

        return if (selectedDaysList.isEmpty()) "" else selectedDaysList.joinToString(", ")

    }
    private fun notifyAtDisplay(notifyValue:Int, notify:String){


        when(notify){
            requireContext().resources.getString(R.string.grocery_notification_none)->{
                mFragmentCreateEditTodoBinding.notifyAtTextView.text = "No set"
            }
            requireContext().resources.getString(R.string.grocery_notification_same_day_and_time)->{
                mFragmentCreateEditTodoBinding.notifyAtTextView.text =  requireContext().resources.getString(R.string.grocery_notification_same_day_and_time)
            }
            requireContext().resources.getString(R.string.grocery_notification_minute_before)->{


                mFragmentCreateEditTodoBinding.notifyAtTextView.text = if(notifyValue > 1) "$notifyValue minutes before due date" else "$notifyValue minute before due date"

            }

            requireContext().resources.getString(R.string.grocery_notification_hour_before)->{
                mFragmentCreateEditTodoBinding.notifyAtTextView.text = if(notifyValue > 1) "$notifyValue hours before due date" else "$notifyValue hour before due date"
            }
            requireContext().resources.getString(R.string.grocery_notification_day_before)->{
                mFragmentCreateEditTodoBinding.notifyAtTextView.text = if(notifyValue > 1) "$notifyValue days before due date" else "$notifyValue day before due date"
            }
        }


    }
    private fun showCalendar(){
        val calendar = DateUtil.getCustomCalendar()
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
        val calendar = DateUtil.getCustomCalendar()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            val selectedDateTimeString = date +" "+ SimpleDateFormat("HH:mm:00").format(calendar.time)
            val selectedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:00").parse(selectedDateTimeString)

            val selectedCalendar = DateUtil.getCustomCalendar()
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

            val selectedCalendar = DateUtil.getCustomCalendar()
            selectedCalendar.time = selectedDateTime

            if(showCalendarFor == SHOW_CALENDAR_FOR_DUE_DATE){
                mCreateEditTodoFragmentViewModel.mDueDateCalendar.value = selectedCalendar
            }else{
                mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value = selectedCalendar

            }

        })
        timePickerDialog.show()

    }

    private fun saveTodo(){


        val taskName = mFragmentCreateEditTodoBinding.taskNameTextInputEditText.text.toString()
        val taskDescription = mFragmentCreateEditTodoBinding.taskDescriptionTextInputEditText.text.toString()
        val repeatEvery = if(mCreateEditTodoFragmentViewModel.mRepeatEvery.value == null) 0 else  mCreateEditTodoFragmentViewModel.mRepeatEvery.value!! //0//if(mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().toInt() else 0
        val repeatEveryType = mCreateEditTodoFragmentViewModel.mRepeatEveryType.value!! //mFragmentCreateEditTodoBinding.repeatSpinner.selectedItem.toString()
        val notifyEvery =  if(mCreateEditTodoFragmentViewModel.mNotifyAt.value != null) mCreateEditTodoFragmentViewModel.mNotifyAt.value!! else 0 //if(mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().toInt() else 0
        val notifyEveryType = if(mCreateEditTodoFragmentViewModel.mNotifyEveryType.value != null) mCreateEditTodoFragmentViewModel.mNotifyEveryType.value!! else requireContext().resources.getString(R.string.none) // mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
        val dueDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.time) else "0000-00-00 00:00:00"

        val isSetInMonday =  isSetInSelectedDay( requireContext().resources.getString(R.string.monday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInTuesday = isSetInSelectedDay( requireContext().resources.getString(R.string.tuesday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInWednesday = isSetInSelectedDay( requireContext().resources.getString(R.string.wednesday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInThursday = isSetInSelectedDay( requireContext().resources.getString(R.string.thursday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInFriday = isSetInSelectedDay( requireContext().resources.getString(R.string.friday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInSaturday = isSetInSelectedDay( requireContext().resources.getString(R.string.saturday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInSunday = isSetInSelectedDay( requireContext().resources.getString(R.string.sunday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)




        var taskUniqueGroupId = UUID.randomUUID().toString()



        if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value == null && repeatEveryType != getString(R.string.none)){
            val repeatUntilDateCalendar = DateUtil.getCustomCalendar()
            repeatUntilDateCalendar.set(Calendar.DAY_OF_MONTH,1)
            repeatUntilDateCalendar.add(Calendar.YEAR,5)
            repeatUntilDateCalendar.set(Calendar.DAY_OF_MONTH,repeatUntilDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value = repeatUntilDateCalendar
        }

        val repeatUntilDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value!= null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value!!.time) else "0000-00-00 00:00:00"

        val todoEntities = arrayListOf<TodoEntity>()
        val todoSubTaskEntities = arrayListOf<TodoChecklistEntity>()
        val dueDate = mCreateEditTodoFragmentViewModel.mDueDateCalendar.value
        val dueDateCopy:Calendar = dueDate?.clone() as Calendar



        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())

        // generate first task item
//        var todoUniqueId = UUID.randomUUID().toString()
//        val todosEntity = TodoEntity(
//            uniqueId = todoUniqueId,
//            groupUniqueId=taskUniqueGroupId,
//            name = taskName,
//            description = taskDescription,
//            dueDate = simpleDateFormat.format(dueDateCopy.time),
//            repeatEvery = repeatEvery,
//            repeatEveryType = repeatEveryType,
//            repeatUntil = repeatUntilDateTimeFormatted,
//            notifyAt = notifyEvery,
//            notifyEveryType = notifyEveryType,
//            itemStatus = TodoEntity.NOT_DELETED_STATUS,
//            uploaded = TodoEntity.NOT_UPLOADED,
//            isFinished = TodoEntity.NOT_FINISHED,
//            datetimeFinished ="",
//            created = currentDatetime,
//            modified = currentDatetime
//        )
//
//        todoEntities.add(todosEntity)
//        mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value!!.forEach { todoSubTaskEntity->
//            var todoSubTaskUniqueId = UUID.randomUUID().toString()
//            val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
//            todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
//            todoSubTaskEntityCopy.todoUniqueId = todoUniqueId
//            todoSubTaskEntityCopy.created = currentDatetime
//            todoSubTaskEntityCopy.modified = currentDatetime
//            todoSubTaskEntities.add(todoSubTaskEntityCopy)
//        }


        when(repeatEveryType){
            requireContext().getString(R.string.day)->{
                do {

                    val hasSelectedDaysOfWeek = mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value!!.isNotEmpty()
                    val doDayExistsInSelectedDayOfWeek:Boolean = mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value!!.contains( DateUtil.getDayOfWeekName(dueDateCopy))

                    if(! doDayExistsInSelectedDayOfWeek && hasSelectedDaysOfWeek){
                        dueDateCopy.add(Calendar.DAY_OF_MONTH,repeatEvery)
                        continue
                    }
                    var taskUniqueId = UUID.randomUUID().toString()
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value!!.forEach { todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }

                    dueDateCopy.add(Calendar.DAY_OF_MONTH,repeatEvery)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value) || dueDateCopy == mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value)
            }
            requireContext().getString(R.string.week)->{
                do {




                       var taskUniqueId = UUID.randomUUID().toString()
                        val todosEntity = TodoEntity(
                            uniqueId = taskUniqueId,
                            groupUniqueId=taskUniqueGroupId,
                            name = taskName,
                            description = taskDescription,
                            dueDate = simpleDateFormat.format(dueDateCopy.time),
                            repeatEvery = repeatEvery,
                            repeatEveryType = repeatEveryType,
                            repeatUntil = repeatUntilDateTimeFormatted,
                            notifyAt = notifyEvery,
                            notifyEveryType = notifyEveryType,
                            isSetInMonday = isSetInMonday,
                            isSetInTuesday = isSetInTuesday,
                            isSetInWednesday = isSetInWednesday,
                            isSetInThursday = isSetInThursday,
                            isSetInFriday = isSetInFriday,
                            isSetInSaturday = isSetInSaturday,
                            isSetInSunday = isSetInSunday,
                            itemStatus = TodoEntity.NOT_DELETED_STATUS,
                            uploaded = TodoEntity.NOT_UPLOADED,
                            isFinished = TodoEntity.NOT_FINISHED,
                            datetimeFinished ="",
                            created = currentDatetime,
                            modified = currentDatetime
                        )

                        todoEntities.add(todosEntity)
                        mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEach { todoSubTaskEntity->
                            var todoSubTaskUniqueId = UUID.randomUUID().toString()
                            val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                            todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                            todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                            todoSubTaskEntityCopy.created = currentDatetime
                            todoSubTaskEntityCopy.modified = currentDatetime
                            todoSubTaskEntities.add(todoSubTaskEntityCopy)
                        }

                    dueDateCopy.add(Calendar.WEEK_OF_MONTH,repeatEvery)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value) || dueDateCopy == mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value)
            }
            requireContext().getString(R.string.month)->{
                do {

                    var taskUniqueId = UUID.randomUUID().toString()
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEach { todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                    dueDateCopy.add(Calendar.MONTH,repeatEvery)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value) || dueDateCopy == mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value)
            }
            requireContext().getString(R.string.end_of_month)->{
                do {

                    var taskUniqueId = UUID.randomUUID().toString()
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEach { todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }

                    dueDateCopy.add(Calendar.MONTH,1)
                    val theMaximumDateOfMonth = dueDateCopy.getActualMaximum(Calendar.DAY_OF_MONTH)
                    dueDateCopy.set(Calendar.DAY_OF_MONTH, theMaximumDateOfMonth)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value) || dueDateCopy == mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value)
            }
            requireContext().getString(R.string.date_of_month)->{
                do {

                    val maxDayOfMonth = dueDateCopy.getActualMaximum(Calendar.DAY_OF_MONTH)

                    if(repeatEvery > maxDayOfMonth ){
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)
                    }else{
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,repeatEvery)
                    }


                    var taskUniqueId = UUID.randomUUID().toString()
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEach { todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                    dueDateCopy.add(Calendar.MONTH,1)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value) || dueDateCopy == mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value)
            }
            requireContext().getString(R.string.year)->{
                do {
                    dueDateCopy.add(Calendar.YEAR,1)
                    val maxDayOfMonth = dueDateCopy.getActualMaximum(Calendar.DAY_OF_MONTH)

                    if(repeatEvery > maxDayOfMonth ){
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)
                    }else{
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,repeatEvery)
                    }


                    var taskUniqueId = UUID.randomUUID().toString()
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = dueDateTimeFormatted,
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEach { todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value) || dueDateCopy == mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value)
            }

        }

        lifecycleScope.launch {

            try {

                    mCreateEditTodoFragmentViewModel.database.withTransaction {
                   val todoIds = mCreateEditTodoFragmentViewModel.saveTodos(todoEntities,todoSubTaskEntities)
                    if(todoIds.isEmpty()){
                        throw Exception("No data was inserted in database")
                    }
                    todoEntities.forEach {todoEntity->
                        val todoEntityWithId = mCreateEditTodoFragmentViewModel.getTodoInformationAndReturn(todoEntity.uniqueId)
                        val todoEntityWithIdNotifyAt = todoEntityWithId.notifyAt
                        val todoEntityWithIdNotifyEveryType =  todoEntityWithId.notifyEveryType
                        val todoEntityWithIdDueDate = todoEntityWithId.dueDate
                        val todoEntityUniqueId = todoEntityWithId.uniqueId
                        val todoEntityName = todoEntityWithId.name
                        val todoEntityId = todoEntityWithId.id

                        val isNeedToCreateAlarm = isNeedToSetAlarmToday(todoEntityWithIdNotifyAt, todoEntityWithIdNotifyEveryType, todoEntityWithIdDueDate)
                        if(isNeedToCreateAlarm){
                            // save alarm information
                            mCreateEditTodoFragmentViewModel.saveTodoAlarmInformation(
                                AlarmRecordsEntity(
                                    id= 0,// in will automatically generated since id is auto increment. @see AlarmRecordsEntity
                                    type = AlarmRecordsEntity.TYPE_GROCERY_TODO,
                                    uniqueId = todoEntityUniqueId,
                                    created = currentDatetime
                                )
                            )
                            createAlarm(todoEntityWithIdNotifyAt, todoEntityWithIdNotifyEveryType,   todoEntityWithIdDueDate, todoEntityUniqueId,todoEntityId,todoEntityName)
                        }
                    }

                    withContext(Dispatchers.Main){

                        mCreateEditTodoFragmentViewModel.mSaveSuccessfully.postValue(true)
                    }


                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(),"${e.message}",Toast.LENGTH_SHORT).show()
            }
        }

//        val intent = Intent()
//        intent.putExtra(TodoFragment.ACTION_TAG, TodoFragment.RELOAD_ACTION_TAG)
//        activity?.setResult(Activity.RESULT_OK, intent)
//        activity?.finish()
    }


    private fun updateTodos(){
//        val taskName = mFragmentCreateEditTodoBinding.taskNameTextInputEditText.text.toString()
//        val taskDescription = mFragmentCreateEditTodoBinding.taskDescriptionTextInputEditText.text.toString()
//        val repeatEvery = 0//if(mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().toInt() else 0
//        val repeatEveryType = ""//mFragmentCreateEditTodoBinding.repeatSpinner.selectedItem.toString()
//        val notifyEvery= 1//if(mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().toInt() else 0
//        val notifyEveryType = ""//mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
//        val dueDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.time) else "0000-00-00 00:00:00"
//

        val taskName = mFragmentCreateEditTodoBinding.taskNameTextInputEditText.text.toString()
        val taskDescription = mFragmentCreateEditTodoBinding.taskDescriptionTextInputEditText.text.toString()
        val repeatEvery = if(mCreateEditTodoFragmentViewModel.mRepeatEvery.value == null) 0 else  mCreateEditTodoFragmentViewModel.mRepeatEvery.value!! //0//if(mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().toInt() else 0
        val repeatEveryType = mCreateEditTodoFragmentViewModel.mRepeatEveryType.value!! //mFragmentCreateEditTodoBinding.repeatSpinner.selectedItem.toString()
        val notifyEvery =  if(mCreateEditTodoFragmentViewModel.mNotifyAt.value != null) mCreateEditTodoFragmentViewModel.mNotifyAt.value!! else 0 //if(mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().toInt() else 0
        val notifyEveryType = if(mCreateEditTodoFragmentViewModel.mNotifyEveryType.value != null) mCreateEditTodoFragmentViewModel.mNotifyEveryType.value!! else requireContext().resources.getString(R.string.none) // mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
        val dueDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.time) else "0000-00-00 00:00:00"

        val isSetInMonday =  isSetInSelectedDay( requireContext().resources.getString(R.string.monday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInTuesday = isSetInSelectedDay( requireContext().resources.getString(R.string.tuesday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInWednesday = isSetInSelectedDay( requireContext().resources.getString(R.string.wednesday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInThursday = isSetInSelectedDay( requireContext().resources.getString(R.string.thursday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInFriday = isSetInSelectedDay( requireContext().resources.getString(R.string.friday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInSaturday = isSetInSelectedDay( requireContext().resources.getString(R.string.saturday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)
        val isSetInSunday = isSetInSelectedDay( requireContext().resources.getString(R.string.sunday),mCreateEditTodoFragmentViewModel.mWeekDaysSelected.value)


        var taskUniqueGroupId = mCreateEditTodoFragmentViewModel.mGroupUniqueId.value
        val taskUniqueId = mCreateEditTodoFragmentViewModel.mTodoUniqueId.value
        if(taskUniqueGroupId == null || taskUniqueGroupId.trim().isEmpty()){

            Toast.makeText(requireContext(),"Failed to update task. Please try again.",Toast.LENGTH_SHORT).show()
            return
        }



        if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value == null && repeatEveryType != getString(R.string.none)){
            val repeatUntilDateCalendar = DateUtil.getCustomCalendar()
            repeatUntilDateCalendar.set(Calendar.DAY_OF_MONTH,1)
            repeatUntilDateCalendar.add(Calendar.YEAR,5)
            repeatUntilDateCalendar.set(Calendar.DAY_OF_MONTH,repeatUntilDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value = repeatUntilDateCalendar
        }

        val repeatUntilDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value!= null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value!!.time) else "0000-00-00 00:00:00"

        val todoEntities = arrayListOf<TodoEntity>()
        val todoSubTaskEntities = arrayListOf<TodoChecklistEntity>()
        val dueDateCopy:Calendar = mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.clone() as Calendar

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        var newTodoUniqueId = ""

        when(repeatEveryType){
            requireContext().getString(R.string.day)->{
                do {


                    val taskUniqueId:String = if (todoEntities.isEmpty()) {
                        newTodoUniqueId = UUID.randomUUID().toString()
                        newTodoUniqueId
                    } else {
                        UUID.randomUUID().toString()
                    }

                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId as String,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                        todoSubTaskEntity.todoUniqueId
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.isFinished = 0
                        todoSubTaskEntityCopy.datetimeFinished =  ""
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }



                    dueDateCopy.add(Calendar.DAY_OF_MONTH,repeatEvery)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value))
            }
            requireContext().getString(R.string.week)->{
                do {

                    val taskUniqueId:String = if (todoEntities.isEmpty()) {
                        newTodoUniqueId = UUID.randomUUID().toString()
                        newTodoUniqueId
                    } else {
                        UUID.randomUUID().toString()
                    }
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.isFinished =  0
                        todoSubTaskEntityCopy.datetimeFinished =  ""
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                    dueDateCopy.add(Calendar.WEEK_OF_MONTH,repeatEvery)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value))
            }
            requireContext().getString(R.string.month)->{
                do {

                    val taskUniqueId:String = if (todoEntities.isEmpty()) {
                        newTodoUniqueId = UUID.randomUUID().toString()
                        newTodoUniqueId
                    } else {
                        UUID.randomUUID().toString()
                    }
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)

                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.isFinished =  0
                        todoSubTaskEntityCopy.datetimeFinished =  ""
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                    dueDateCopy.add(Calendar.MONTH,repeatEvery)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value))
            }
            requireContext().getString(R.string.end_of_month)->{
                do {

                    val taskUniqueId:String = if (todoEntities.isEmpty()) {
                        newTodoUniqueId = UUID.randomUUID().toString()
                        newTodoUniqueId
                    } else {
                        UUID.randomUUID().toString()
                    }
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.isFinished =  0
                        todoSubTaskEntityCopy.datetimeFinished =  ""
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                    dueDateCopy.add(Calendar.MONTH,1)

                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value))
            }
            requireContext().getString(R.string.date_of_month)->{
                do {


                    val taskUniqueId:String = if (todoEntities.isEmpty()) {
                        newTodoUniqueId = UUID.randomUUID().toString()
                        newTodoUniqueId
                    } else {
                        UUID.randomUUID().toString()
                    }
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = simpleDateFormat.format(dueDateCopy.time),
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.isFinished =  0
                        todoSubTaskEntityCopy.datetimeFinished =  ""
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }



                    dueDateCopy.add(Calendar.MONTH,1)
                    val maxDayOfMonth = dueDateCopy.getActualMaximum(Calendar.DAY_OF_MONTH)

                    if(repeatEvery > maxDayOfMonth ){
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)
                    }else{
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,repeatEvery)
                    }


                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value))
            }
            requireContext().getString(R.string.year)->{
                do {



                    val taskUniqueId:String = if (todoEntities.isEmpty()) {
                        newTodoUniqueId = UUID.randomUUID().toString()
                        newTodoUniqueId
                    } else {
                        UUID.randomUUID().toString()
                    }
                    val todosEntity = TodoEntity(
                        uniqueId = taskUniqueId,
                        groupUniqueId=taskUniqueGroupId,
                        name = taskName,
                        description = taskDescription,
                        dueDate = dueDateTimeFormatted,
                        repeatEvery = repeatEvery,
                        repeatEveryType = repeatEveryType,
                        repeatUntil = repeatUntilDateTimeFormatted,
                        notifyAt = notifyEvery,
                        notifyEveryType = notifyEveryType,
                        isSetInMonday = isSetInMonday,
                        isSetInTuesday = isSetInTuesday,
                        isSetInWednesday = isSetInWednesday,
                        isSetInThursday = isSetInThursday,
                        isSetInFriday = isSetInFriday,
                        isSetInSaturday = isSetInSaturday,
                        isSetInSunday = isSetInSunday,
                        itemStatus = TodoEntity.NOT_DELETED_STATUS,
                        uploaded = TodoEntity.NOT_UPLOADED,
                        isFinished = TodoEntity.NOT_FINISHED,
                        datetimeFinished ="",
                        created = currentDatetime,
                        modified = currentDatetime
                    )

                    todoEntities.add(todosEntity)
                    mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                        var todoSubTaskUniqueId = UUID.randomUUID().toString()
                        val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                        todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                        todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                        todoSubTaskEntityCopy.isFinished =  0
                        todoSubTaskEntityCopy.datetimeFinished =  ""
                        todoSubTaskEntityCopy.created = currentDatetime
                        todoSubTaskEntityCopy.modified = currentDatetime
                        todoSubTaskEntities.add(todoSubTaskEntityCopy)
                    }


                    dueDateCopy.add(Calendar.YEAR,1)
                    val maxDayOfMonth = dueDateCopy.getActualMaximum(Calendar.DAY_OF_MONTH)

                    if(repeatEvery > maxDayOfMonth ){
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,maxDayOfMonth)
                    }else{
                        dueDateCopy.set(Calendar.DAY_OF_MONTH,repeatEvery)
                    }
                }while (dueDateCopy.before(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value))
            }
            requireContext().getString(R.string.none)->{

                val taskUniqueId:String = if (todoEntities.isEmpty()) {
                    newTodoUniqueId = UUID.randomUUID().toString()
                    newTodoUniqueId
                } else {
                    UUID.randomUUID().toString()
                }
                val todosEntity = TodoEntity(
                    uniqueId = taskUniqueId,
                    groupUniqueId=taskUniqueGroupId,
                    name = taskName,
                    description = taskDescription,
                    dueDate = simpleDateFormat.format(dueDateCopy.time),
                    repeatEvery = repeatEvery,
                    repeatEveryType = repeatEveryType,
                    repeatUntil = repeatUntilDateTimeFormatted,
                    notifyAt = notifyEvery,
                    notifyEveryType = notifyEveryType,
                    isSetInMonday = isSetInMonday,
                    isSetInTuesday = isSetInTuesday,
                    isSetInWednesday = isSetInWednesday,
                    isSetInThursday = isSetInThursday,
                    isSetInFriday = isSetInFriday,
                    isSetInSaturday = isSetInSaturday,
                    isSetInSunday = isSetInSunday,
                    itemStatus = TodoEntity.NOT_DELETED_STATUS,
                    uploaded = TodoEntity.NOT_UPLOADED,
                    isFinished = TodoEntity.NOT_FINISHED,
                    datetimeFinished ="",
                    created = currentDatetime,
                    modified = currentDatetime
                )

                todoEntities.add(todosEntity)
                mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->
                    var todoSubTaskUniqueId = UUID.randomUUID().toString()
                    val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
                    todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
                    todoSubTaskEntityCopy.todoUniqueId = taskUniqueId
                    todoSubTaskEntityCopy.isFinished =  0
                    todoSubTaskEntityCopy.datetimeFinished =  ""
                    todoSubTaskEntityCopy.created = currentDatetime
                    todoSubTaskEntityCopy.modified = currentDatetime
                    todoSubTaskEntities.add(todoSubTaskEntityCopy)
                }


            }
        }

        MainScope().launch{

            val todos =  withContext(IO){ mCreateEditTodoFragmentViewModel.getSelectedAndFutureTodos(taskUniqueId!!) }
            todos.forEach { todo->
                cancelAlarm(todo.uniqueId, todo.id)
            }

            val todoOriginalDueDate: String = mCreateEditTodoFragmentViewModel.mSelectedTodoDueDate.value!!
            withContext(IO){
                mCreateEditTodoFragmentViewModel.updateTodos1(todoEntities,todoSubTaskEntities,mCreateEditTodoFragmentViewModel.mTodoUniqueId.value!!,mCreateEditTodoFragmentViewModel.mGroupUniqueId.value!!,todoOriginalDueDate)
            }

            todoEntities.forEach {todoEntity->
                val todoEntityWithId =  withContext(IO){ mCreateEditTodoFragmentViewModel.getTodoInformationAndReturn(todoEntity.uniqueId) }
                val todoEntityWithIdNotifyAt = todoEntityWithId.notifyAt
                val todoEntityWithIdNotifyEveryType =  todoEntityWithId.notifyEveryType
                val todoEntityWithIdDueDate = todoEntityWithId.dueDate
                val todoEntityUniqueId = todoEntityWithId.uniqueId
                val todoEntityName = todoEntityWithId.name
                val todoEntityId = todoEntityWithId.id

                val isNeedToCreateAlarm = isNeedToSetAlarmToday(todoEntityWithIdNotifyAt, todoEntityWithIdNotifyEveryType, todoEntityWithIdDueDate)
                if(isNeedToCreateAlarm){
                    withContext(IO){
                        // save alarm information
                        mCreateEditTodoFragmentViewModel.saveTodoAlarmInformation(
                            AlarmRecordsEntity(
                                id= 0,// in will automatically generated since id is auto increment. @see AlarmRecordsEntity
                                type = AlarmRecordsEntity.TYPE_GROCERY_TODO,
                                uniqueId = todoEntityUniqueId,
                                created = currentDatetime
                            )
                        )
                    }

                    createAlarm(todoEntityWithIdNotifyAt, todoEntityWithIdNotifyEveryType,   todoEntityWithIdDueDate, todoEntityUniqueId,todoEntityId,todoEntityName)
                }
            }

            val intent = Intent()
            intent.putExtra(ViewTodoFragment.NEW_UNIQUE_ID_TAG, newTodoUniqueId)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }

    }
    private fun updateTodo(){

//        val taskName = mFragmentCreateEditTodoBinding.taskNameTextInputEditText.text.toString()
//        val taskDescription = mFragmentCreateEditTodoBinding.taskDescriptionTextInputEditText.text.toString()
//        val repeatEvery = 0//if(mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().toInt() else 0
//        val repeatEveryType = ""//mFragmentCreateEditTodoBinding.repeatSpinner.selectedItem.toString()
//        val notifyEvery= 1//if(mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().toInt() else 0
//        val notifyEveryType = ""//mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
//        val dueDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.time) else "0000-00-00 00:00:00"

        val taskName = mFragmentCreateEditTodoBinding.taskNameTextInputEditText.text.toString()
        val taskDescription = mFragmentCreateEditTodoBinding.taskDescriptionTextInputEditText.text.toString()
        val repeatEvery = if(mCreateEditTodoFragmentViewModel.mRepeatEvery.value == null) 0 else  mCreateEditTodoFragmentViewModel.mRepeatEvery.value!! //0//if(mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.text.toString().toInt() else 0
        val repeatEveryType = mCreateEditTodoFragmentViewModel.mRepeatEveryType.value!! //mFragmentCreateEditTodoBinding.repeatSpinner.selectedItem.toString()
        val notifyEvery =  if(mCreateEditTodoFragmentViewModel.mNotifyAt.value != null) mCreateEditTodoFragmentViewModel.mNotifyAt.value!! else 0 //if(mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().trim().isNotEmpty()) mFragmentCreateEditTodoBinding.notifyTextInputEditText.text.toString().toInt() else 0
        val notifyEveryType = if(mCreateEditTodoFragmentViewModel.mNotifyEveryType.value != null) mCreateEditTodoFragmentViewModel.mNotifyEveryType.value!! else requireContext().resources.getString(R.string.none) // mFragmentCreateEditTodoBinding.notifyEveryTypeSpinner.selectedItem.toString()
        val dueDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value != null) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.time) else "0000-00-00 00:00:00"




        var taskUniqueGroupId = mCreateEditTodoFragmentViewModel.mGroupUniqueId.value
        var todoUniqueId = mCreateEditTodoFragmentViewModel.mTodoUniqueId.value as String
        var todoId:Int = mCreateEditTodoFragmentViewModel.mTodoId.value!!


        if(taskUniqueGroupId == null || taskUniqueGroupId.trim().isEmpty()){

            Toast.makeText(requireContext(),"Failed to update task. Please try again.",Toast.LENGTH_SHORT).show()
            return
        }



        if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value == null && repeatEveryType != getString(R.string.none)){
            val repeatUntilDateCalendar = DateUtil.getCustomCalendar()
            repeatUntilDateCalendar.set(Calendar.DAY_OF_MONTH,1)
            repeatUntilDateCalendar.add(Calendar.YEAR,5)
            repeatUntilDateCalendar.set(Calendar.DAY_OF_MONTH,repeatUntilDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value = repeatUntilDateCalendar
        }


        val repeatUntilDateTimeFormatted = if(mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value!= null ) SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( mCreateEditTodoFragmentViewModel.mRepeatUntilCalendar.value!!.time) else "0000-00-00 00:00:00"
        val todoSubTaskEntities = arrayListOf<TodoChecklistEntity>()
        val dueDateCopy:Calendar = mCreateEditTodoFragmentViewModel.mDueDateCalendar.value?.clone() as Calendar
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDatetime: String = simpleDateFormat.format(Date())
        val dueDateString = simpleDateFormat.format(dueDateCopy.time)



        mCreateEditTodoFragmentViewModel.mTodoSubTask.value!!.forEachIndexed{index, todoSubTaskEntity->

            var todoSubTaskUniqueId = UUID.randomUUID().toString()
            val todoSubTaskEntityCopy = todoSubTaskEntity.copy()
            todoSubTaskEntityCopy.uniqueId = todoSubTaskUniqueId
            todoSubTaskEntityCopy.todoUniqueId = todoUniqueId
            todoSubTaskEntityCopy.isFinished = 0
            todoSubTaskEntityCopy.datetimeFinished =  ""
            todoSubTaskEntityCopy.created = currentDatetime
            todoSubTaskEntityCopy.modified = currentDatetime
            todoSubTaskEntities.add(todoSubTaskEntityCopy)
        }

        //val todoDueDate: String = simpleDateFormat.format(mCreateEditTodoFragmentViewModel.mDueDateCalendar.value!!.time)
        // save record
        mCreateEditTodoFragmentViewModel.updateTodo(todoUniqueId,taskName ,taskDescription , dueDateString, repeatEvery,repeatEveryType,
            repeatUntilDateTimeFormatted,notifyEvery,notifyEveryType, TodoEntity.NOT_FINISHED, "",todoSubTaskEntities)
        //cancel alarm
        cancelAlarm(mCreateEditTodoFragmentViewModel.mTodoUniqueId.value!!,mCreateEditTodoFragmentViewModel.mTodoId.value!!)
        //check if alarm need to be created
        val isNeedToCreateAlarm = isNeedToSetAlarmToday(notifyEvery, notifyEveryType, dueDateString)
        if(isNeedToCreateAlarm){

            createAlarm(notifyEvery, notifyEveryType,   dueDateString, todoUniqueId,todoId,taskName)
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    // save alarm information
                    mCreateEditTodoFragmentViewModel.saveTodoAlarmInformation(
                        AlarmRecordsEntity(
                            id = 0,// in will automatically generated since id is auto increment. @see AlarmRecordsEntity
                            type = AlarmRecordsEntity.TYPE_GROCERY_TODO,
                            uniqueId = todoUniqueId,
                            created = currentDatetime
                        )
                    )
                }
            }
        }


        val intent = Intent()
        intent.putExtra(ViewTodoFragment.NEW_UNIQUE_ID_TAG, todoUniqueId)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()

    }
   private fun isSetInSelectedDay(dayName:String,selectedDays:List<String>?):Int{

       if(selectedDays == null){
           return TodoEntity.NOT_SET
       }
       return if(selectedDays.contains(dayName)) TodoEntity.SET else TodoEntity.NOT_SET


   }
    private fun isNeedToSetAlarmToday(notifyAt: Int, notifyType: String, dueDateTimeFormatted: String):Boolean {

        if(dueDateTimeFormatted.isEmpty() || dueDateTimeFormatted == "0000-00-00 00:00:00"){

            return false
        }
        val formatter: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val dueDateTime:DateTime = formatter.parseDateTime(dueDateTimeFormatted)
        val currentDateTime = DateTime.now()
        val nextDayDateTime = currentDateTime.plusDays(1).withTimeAtStartOfDay()


        when(notifyType){
            resources.getString(R.string.grocery_notification_none) -> {
                return false
            }
            resources.getString(R.string.grocery_notification_same_day_and_time) -> {

                return currentDateTime <= dueDateTime && dueDateTime < nextDayDateTime
            }
            resources.getString(R.string.grocery_notification_minute_before) -> {

                return currentDateTime <= dueDateTime.minusMinutes(notifyAt)  && dueDateTime.minusMinutes(notifyAt) < nextDayDateTime
            }
            resources.getString(R.string.grocery_notification_hour_before) -> {

                return currentDateTime <= dueDateTime.minusHours(notifyAt)  && dueDateTime.minusHours(notifyAt) < nextDayDateTime
            }
            resources.getString(R.string.grocery_notification_day_before) -> {

                return currentDateTime <= dueDateTime.minusDays(notifyAt)  && dueDateTime.minusDays(notifyAt) < nextDayDateTime
            }else -> {
                return false
            }
        }


    }
    private fun createAlarm(notifyValue: Int, notifyType: String, dueDateTimeFormatted: String, todoUniqueId: String,todoEntityId:Int, todoName : String){
        CustomAlarmManager.createAlarmForTodo(requireContext(),notifyValue,notifyType,dueDateTimeFormatted, todoUniqueId, todoEntityId,todoName)
    }
    private fun cancelAlarm(todoEntityUniqueId: String, todoEntityId: Int) {

        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        intent.action = NotificationReceiver.TODO_NOTIFICATION_ACTION
        intent.putExtra(NotificationReceiver.TODO_UNIQUE_ID, todoEntityUniqueId) // Use the same identifier used when setting the alarm


        CustomAlarmManager.cancelAlarm(requireContext(),todoEntityId,intent)
    }

    private val repeatSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            var selectedRepeat = requireContext().resources.getStringArray(R.array.todo_recurring)[position]
            when(selectedRepeat){
                requireContext().getString(R.string.year)->{

//                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
//                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.date_of_month)->{

                    var dateInMonthDialogFragment = DateInMonthDialogFragment()
                    dateInMonthDialogFragment.setDateSelectedListener(object:DateInMonthDialogFragment.DateSelectedListener{
                        override fun dateSelected(date: String) {
//                            mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
//                            mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
//
//                            mFragmentCreateEditTodoBinding.repeatEveryTextInputEditText.setText(date)
                        }

                    })
                    dateInMonthDialogFragment.show(childFragmentManager,"DateInMonthDialogFragment")

                }
                requireContext().getString(R.string.end_of_month)->{

//                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
//                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.month)->{

//                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
//                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.week)->{

//                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
//                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.day)->{

//                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.VISIBLE
//                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.VISIBLE
                }
                requireContext().getString(R.string.none)->{
//                    mFragmentCreateEditTodoBinding.repeatTextInputLayout.visibility = View.GONE
//                    mFragmentCreateEditTodoBinding.repeatUntilImageView.visibility = View.GONE


                }

            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }

    inner class TodoSubTaskListRecyclerviewAdapter(): RecyclerView.Adapter<TodoSubTaskListRecyclerviewAdapter.ItemViewHolder>() {

        var todoSubTasksEntities = emptyList<TodoChecklistEntity>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val todoItemSubTaskBinding = TodoItemSubTaskBinding.inflate(layoutInflater, parent, false)
            todoItemSubTaskBinding.checkBox3.isEnabled = false
            return ItemViewHolder(todoItemSubTaskBinding)
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val todoSubTasksEntity = todoSubTasksEntities[position]
            holder.todoItemSubTaskBinding.todoSubTasksEntity = todoSubTasksEntity
            holder.todoItemSubTaskBinding.editImageView.visibility = View.VISIBLE
            holder.todoItemSubTaskBinding.deleteImageView.visibility = View.VISIBLE
            holder.todoItemSubTaskBinding.root.setOnClickListener(holder)
            holder.todoItemSubTaskBinding.deleteImageView.setOnClickListener(holder)
            holder.todoItemSubTaskBinding.editImageView.setOnClickListener(holder)
            holder.todoItemSubTaskBinding.executePendingBindings()
        }
        override fun getItemCount(): Int {
            return todoSubTasksEntities.size
        }
        private fun itemClicked(itemPosition:Int){
            Toast.makeText(requireContext(),"Position ${itemPosition}",Toast.LENGTH_SHORT).show()
        }
        private fun deleteClicked(itemPosition:Int){



            mCreateEditTodoFragmentViewModel.mTodoSubTask.value?.removeAt(itemPosition)

            mCreateEditTodoFragmentViewModel.mTodoSubTask.postValue(mCreateEditTodoFragmentViewModel.mTodoSubTask.value)
        }
        private fun editClicked(itemPosition:Int){
            mCreateEditTodoFragmentViewModel.mTodoSubTask.value?.let {
                val todoSubTasksEntity = it[itemPosition]
                val addSubTaskDialogFragment = AddEditSubTaskDialogFragment(object: OnSubTaskSavedListener{
                    override fun onSubTaskSaved(subTask: String) {

                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val currentDatetime: String = simpleDateFormat.format(Date())
                        var uniqueId = UUID.randomUUID().toString()

                        todoSubTasksEntity.name = subTask
                        todoSubTasksEntity.modified = currentDatetime
                        todoSubTasksEntity.uploaded = 0

                        mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value?.removeAt(itemPosition)
                        mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value?.add(itemPosition,todoSubTasksEntity)
                        mCreateEditTodoFragmentViewModel.mTodoSubTask.postValue(mCreateEditTodoFragmentViewModel.mTodoSubTask.value)

                        (mFragmentCreateEditTodoBinding.todoSubTaskListRecyclerview.adapter as TodoSubTaskListRecyclerviewAdapter).setData( mCreateEditTodoFragmentViewModel.mTodoSubTask!!.value!!)

                    }
                },todoSubTasksEntity)
                addSubTaskDialogFragment.isCancelable = false
                addSubTaskDialogFragment.show(requireActivity().supportFragmentManager,"AddSubTaskDialogFragment")
            }


        }
        fun setData(updatedSubTodoEntities:List<TodoChecklistEntity>){
            val diffUtil = CustomDiffUtil(todoSubTasksEntities,updatedSubTodoEntities)
            val diffResult = DiffUtil.calculateDiff(diffUtil)
            todoSubTasksEntities = updatedSubTodoEntities

            diffResult.dispatchUpdatesTo(this)

        }

        inner class  ItemViewHolder(var todoItemSubTaskBinding: TodoItemSubTaskBinding): RecyclerView.ViewHolder(todoItemSubTaskBinding.root),View.OnClickListener{
            override fun onClick(view: View?) {

                when (view?.id){
                    R.id.deleteImageView->{
                        deleteClicked(adapterPosition)
                    }
                    R.id.editImageView->{
                        editClicked(adapterPosition)

                    }
                    else->{
                        itemClicked(adapterPosition)
                    }
                }

            }

        }
    }

    class CustomDiffUtil(val oldTodoSubtasks:List<TodoChecklistEntity>, val newTodoSubTasks:List<TodoChecklistEntity>): DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldTodoSubtasks.size
        }

        override fun getNewListSize(): Int {
            return newTodoSubTasks.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldTodoSubtasks[oldItemPosition].uniqueId == newTodoSubTasks[newItemPosition].uniqueId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return when{
                oldTodoSubtasks[oldItemPosition].uniqueId != newTodoSubTasks[newItemPosition].uniqueId->{
                    false
                }
                oldTodoSubtasks[oldItemPosition].name != newTodoSubTasks[newItemPosition].name->{
                    false
                }
                oldTodoSubtasks[oldItemPosition].itemStatus != newTodoSubTasks[newItemPosition].itemStatus->{
                    false
                }
                oldTodoSubtasks[oldItemPosition].uploaded != newTodoSubTasks[newItemPosition].uploaded->{
                    false
                }
                oldTodoSubtasks[oldItemPosition].created != newTodoSubTasks[newItemPosition].created->{
                    false
                }
                oldTodoSubtasks[oldItemPosition].modified != newTodoSubTasks[newItemPosition].modified->{
                    false
                }
                else -> {
                    true
                }
            }
        }

    }
}

