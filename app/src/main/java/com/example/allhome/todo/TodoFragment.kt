package com.example.allhome.todo

import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.MainActivity
import com.example.allhome.NotificationReceiver
import com.example.allhome.R
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoChecklistEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import com.example.allhome.databinding.FragmentTodoBinding
import com.example.allhome.databinding.TodoItemBinding
import com.example.allhome.todo.calendar.TodoCalendarViewFragment
import com.example.allhome.todo.viewmodel.TodoFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoFragmentViewModelFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TodoFragment : Fragment() {
    private var currentDate = LocalDate.now()
    private var selectedDate = LocalDate.now()
    private var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var readableDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private var selectedFilter:Int = R.id.todoToday
    private var parentActivity:Int = MAIN_ACTIVITY
    private var hasChanges = false
    private var viewing = LIST_VIEW

    private val mTodoFragmentViewModel: TodoFragmentViewModel by viewModels{

        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        val logsDAO = (context?.applicationContext as AllHomeBaseApplication).logsDAO
        TodoFragmentViewModelFactory(todosDAO,logsDAO)

    }
    private val addOrUpdateTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){

            val action = activityResult.data?.getIntExtra(ACTION_TAG,0)
            if(action == RELOAD_ACTION_TAG){
                hasChanges = true
                getTodos()

                if(parentFragment != null && parentFragment is TodoFragmentCommunication){
                    val todoFragmentCommunication  = parentFragment as TodoFragmentCommunication
                    todoFragmentCommunication.onDataChanged()
                }

            }

        }
    }
    private val viewTodoResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){
            val action = activityResult.data?.getIntExtra(ACTION_TAG,0)
            if(action == RELOAD_ACTION_TAG){
                hasChanges = true
                getTodos()
            }
        }
    }
    lateinit var mFragmentTodoBinding:FragmentTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity().title = "To Do List"

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

        mFragmentTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_todo,null,false)
        parentActivity = arguments?.getInt(PARENT_ACTIVITY_TAG)!!
        viewing = arguments!!.getInt(VIEWING_TAG)

//        if(parentActivity == OTHER_ACTIVITY){
//
//            val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
//            toolbar.visibility = View.VISIBLE
//
//            val activity = requireActivity() as AppCompatActivity
//            activity.setSupportActionBar(toolbar)
//            //show menu icon on action bar
//            activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//
//            val selectedDateString = arguments?.getString(SELECTED_DATE_TAG)
//            selectedDate = SimpleDateFormat("yyyy-MM-dd").parse(selectedDateString).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//
//            getTodos()
//
//            selectedFilter = R.id.todoCustomDate
//            setTodDateLabel()
//
//        }

        val selectedDateString = arguments?.getString(SELECTED_DATE_TAG)
        selectedDate = SimpleDateFormat("yyyy-MM-dd").parse(selectedDateString).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        getTodos()

        selectedFilter = R.id.todoCustomDate
        setTodDateLabel()

        //val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        //mFragmentTodoBinding.todoListRecyclerview.addItemDecoration(decorator)

        val todoListRecyclerviewViewAdapter = TodoListRecyclerviewViewAdapter(arrayListOf())
        mFragmentTodoBinding.todoListRecyclerview.adapter = todoListRecyclerviewViewAdapter
        mFragmentTodoBinding.fab.setOnClickListener {

            /**
             * @todo Remove this code below in production 2023-08-06
             */
           // triggerNotificationTest();


//            val formatter: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
//            //val dueDateTime:DateTime = formatter.parseDateTime(dueDateTimeFormatted)
//            val currentDateTime = DateTime.now()
//            currentDateTime.plusSeconds(2)
//            val dueDateTime:String = formatter.print(currentDateTime)
//
//
//            createTestTodoAlarm(1, "Minute before", dueDateTime,"ffc2f4e8-739c-42ff-a594-fbafc402e5b5", "Todo 1")
//
//            return@setOnClickListener;

            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
            intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.CREATE_TODO_FRAGMENT)
            addOrUpdateTodoListResultContract.launch(intent)
        }

        mTodoFragmentViewModel.mLoadData.observe(viewLifecycleOwner) { loadData->
            if(loadData == false){
                return@observe
            }

            val todoListRecyclerviewViewAdapter = mFragmentTodoBinding.todoListRecyclerview.adapter as TodoListRecyclerviewViewAdapter
            todoListRecyclerviewViewAdapter.todosWithSubTaskCount = mTodoFragmentViewModel.mTodoEntities as ArrayList<TodosWithSubTaskCount>
            todoListRecyclerviewViewAdapter.notifyDataSetChanged()

            setTodDateLabel()
            hideOrShowGroceryNoItemTextView()

        }


        mFragmentTodoBinding.swipeRefresh.setOnRefreshListener {
            getTodos()
            mFragmentTodoBinding.swipeRefresh.isRefreshing = false
        }

        getTodos()
        return mFragmentTodoBinding.root
    }
    private fun hideOrShowGroceryNoItemTextView(){
        if(mTodoFragmentViewModel.mTodoEntities.isEmpty()){
            mFragmentTodoBinding.noTodoTextView.visibility = View.VISIBLE
        }else{
            mFragmentTodoBinding.noTodoTextView.visibility = View.GONE
        }

    }
    private fun triggerNotificationTest(){
        /**
         * @todo Remove this code below in production 2023-08-06
         */
        val alarmDateTimeMilli = DateTime.now().plusSeconds(1).millis
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent1 = Intent(context, NotificationReceiver::class.java)
        intent1.apply {
            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)



            val pendingIntent = PendingIntent.getBroadcast(context, NotificationReceiver.NOTIFICATION_REQUEST_CODE, intent1, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDateTimeMilli, pendingIntent)
        }

    }
    private fun createTestTodoAlarm(notifyValue: Int, notifyType: String, dueDateTimeFormatted: String, todoUniqueId: String, todoName : String){

        val notificationDatetime  = generatedAlarmDatetime(notifyValue, notifyType,dueDateTimeFormatted)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)

        intent.apply {
            action = NotificationReceiver.TODO_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            intent.putExtra(NotificationReceiver.TODO_UNIQUE_ID, todoUniqueId)
            intent.putExtra(NotificationReceiver.TODO_NAME, todoName)

        }

        val pendingIntent = createPendingIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }else{

            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }

    }
    private fun generatedAlarmDatetime(notifyValue: Int, notifyType: String, dueDateTimeFormatted: String):Long{

        if(dueDateTimeFormatted.isEmpty() || dueDateTimeFormatted == "0000-00-00 00:00:00"){

            return 0
        }
        val formatter: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val datetime:DateTime = formatter.parseDateTime(dueDateTimeFormatted)

        when(notifyType){
            resources.getString(R.string.grocery_notification_none) -> {
                return 0
            }
            resources.getString(R.string.grocery_notification_same_day_and_time) -> {

                return datetime.millis
            }
            resources.getString(R.string.grocery_notification_minute_before) -> {

                return datetime.minusMinutes(notifyValue).millis
            }
            resources.getString(R.string.grocery_notification_hour_before) -> {

                return datetime.minusHours(notifyValue).millis
            }
            resources.getString(R.string.grocery_notification_day_before) -> {

                return datetime.minusDays(notifyValue).millis
            }
        }
        return 0
    }
    private fun createPendingIntent(intent: Intent): PendingIntent {

        return PendingIntent.getBroadcast(requireContext(), 123, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val todoTodayMenu = menu.findItem(R.id.todoToday)
        val todoTomorrowMenu = menu.findItem(R.id.todoTomorrow)
        val todoCustomDateMenu = menu.findItem(R.id.todoCustomDate)
        val todoOverdueMenu = menu.findItem(R.id.todoOverdue)
        val todoYesterdaymenu = menu.findItem(R.id.todoYesterday)

      //  Log.e("menu", todoYesterdaymenu.toString());

        val todoListViewMenu = menu.findItem(R.id.todoListViewMenu)
        val todoCalendarViewMenu = menu.findItem(R.id.todoCalendarViewMenu)

        Log.e("VIEWING","${viewing}")

        if(viewing == CALENDAR_VIEW){
            todoListViewMenu.isVisible = true
            todoCalendarViewMenu.isVisible = false

            todoTodayMenu.isVisible = false
            todoTomorrowMenu.isVisible = false
            todoCustomDateMenu.isVisible = false
            todoOverdueMenu.isVisible = false
            todoYesterdaymenu.isVisible = false
        }
        if(viewing == LIST_VIEW){
            todoListViewMenu.isVisible = false
            todoCalendarViewMenu.isVisible = true
            todoListViewMenu
        }


    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.todo_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        selectedFilter = item.itemId

        when(item.itemId){
            R.id.todoToday->{
                selectedDate = currentDate
                getTodos()
                setTodDateLabel()
            }
            R.id.todoTomorrow->{
                selectedDate = currentDate.plusDays(1)
                getTodos()
                setTodDateLabel()
            }
            R.id.todoYesterday->{
                selectedDate= currentDate.minusDays(1)
                getTodos()
                setTodDateLabel()
            }
            R.id.todoOverdue->{
                setTodDateLabel()
                getTodos()
            }
            R.id.todoCustomDate->{
                showCalendar()

            }
            R.id.todoListViewMenu->{

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val stringDate = dateFormat.format(Date().time)
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.home_fragment_container,TodoFragment.newInstance(TodoFragment.MAIN_ACTIVITY, stringDate))
                    commit()
                }

            }
            R.id.todoCalendarViewMenu->{

                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.home_fragment_container,TodoCalendarViewFragment())
                    commit()
                }
            }
            android.R.id.home->{


            }
        }
        return true
    }
    private fun setTodDateLabel(){
        when (selectedFilter){
            R.id.todoToday->{
                mFragmentTodoBinding.todoDateLabel.text = "Today - ${readableDateFormatter.format(selectedDate)}"
            }
            R.id.todoTomorrow->{
                mFragmentTodoBinding.todoDateLabel.text = "Tomorrow - ${readableDateFormatter.format(selectedDate)}"
            }
            R.id.todoYesterday->{
                mFragmentTodoBinding.todoDateLabel.text = "Yesterday - ${readableDateFormatter.format(selectedDate)}"
            }
            R.id.todoOverdue->{
                mFragmentTodoBinding.todoDateLabel.text = "Overdue"
            }
            R.id.todoCustomDate->{

                val currentDate = LocalDate.now()

                if (selectedDate.isEqual(currentDate)) {
                    mFragmentTodoBinding.todoDateLabel.text = "Today -  ${readableDateFormatter.format(selectedDate)}"
                } else if (selectedDate.isEqual(currentDate.minusDays(1))) {
                    mFragmentTodoBinding.todoDateLabel.text = "Yesterday -  ${readableDateFormatter.format(selectedDate)}"
                } else if (selectedDate.isEqual(currentDate.plusDays(1))) {
                    mFragmentTodoBinding.todoDateLabel.text = "Tomorrow -  ${readableDateFormatter.format(selectedDate)}"
                } else {
                    mFragmentTodoBinding.todoDateLabel.text = "${readableDateFormatter.format(selectedDate)}"
                }


            }
        }
    }
    private fun showCalendar(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val pattern = "yyyy-M-d"
            val simpleDateFormat = SimpleDateFormat(pattern)
            val date: Date = simpleDateFormat.parse(year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth)
            val readableDate = SimpleDateFormat("MMMM d,yyyy").format(date)

            selectedDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            getTodos()

            selectedFilter = R.id.todoCustomDate
            setTodDateLabel()

        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }
    private fun getTodos(){

        when(selectedFilter){
            R.id.todoOverdue->{
                Toast.makeText(requireContext(),"Overdue", Toast.LENGTH_SHORT).show()
                mTodoFragmentViewModel.getOverdueTodos(selectedDate.format(dateFormatter))
            }else->{
                mTodoFragmentViewModel.getTodos(selectedDate.format(dateFormatter))
            }
        }

    }
    companion object {
        const val ACTION_TAG = "ACTION_TAG"
        const val SELECTED_DATE_TAG = "SELECTED_DATE_TAG"
        const val PARENT_ACTIVITY_TAG = "PARENT_ACTIVITY_TAG"
        const val VIEWING_TAG = "VIEWING_TAG"
        const val MAIN_ACTIVITY = 0
        const val OTHER_ACTIVITY = 1
        const val CALENDAR_VIEW = 1
        const val LIST_VIEW = 2

        const val RELOAD_ACTION_TAG = 1
        @JvmStatic fun newInstance(parentActivity : Int = MAIN_ACTIVITY , selectedDateString: String, viewing : Int = LIST_VIEW) =
            TodoFragment().apply {
                Log.e("viewing","the viewing ${viewing}")
                arguments = Bundle().apply {
                    putInt(PARENT_ACTIVITY_TAG, parentActivity)
                    putString(SELECTED_DATE_TAG, selectedDateString)
                    putInt(VIEWING_TAG,viewing)
                }
            }
    }
    inner class TodoListRecyclerviewViewAdapter(var todosWithSubTaskCount:ArrayList<TodosWithSubTaskCount>): RecyclerView.Adapter<TodoListRecyclerviewViewAdapter.ItemViewHolder>() {

        private val itemOnCheckChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val itemPosition = buttonView?.tag

             val selectedTodo = todosWithSubTaskCount[itemPosition as Int]

            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val currentDatetime = currentDateTime.format(formatter)
            val isFinished = if (isChecked) TodoEntity.FINISHED else TodoEntity.NOT_FINISHED

            mTodoFragmentViewModel.updateTodoAsFinished(selectedTodo.todoEntity.uniqueId, currentDatetime, isFinished )

            if(parentFragment != null && parentFragment is TodoFragmentCommunication){
                val todoFragmentCommunication  = parentFragment as TodoFragmentCommunication
                todoFragmentCommunication.onDataChanged()
            }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val expensesMonthlyItemBinding = TodoItemBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(expensesMonthlyItemBinding)
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val todoWithSubTaskCount = todosWithSubTaskCount[position]
            holder.todoItemBinding.todosWithSubTaskCount = todoWithSubTaskCount
            holder.todoItemBinding.checkBox2.tag = position
            holder.todoItemBinding.checkBox2.isChecked = todoWithSubTaskCount.todoEntity.isFinished == TodoChecklistEntity.FINISHED
            holder.todoItemBinding.checkBox2.setOnCheckedChangeListener(itemOnCheckChangeListener)
            holder.todoItemBinding.root.setOnClickListener(holder)
            holder.todoItemBinding.executePendingBindings()
        }
        override fun getItemCount(): Int {
            return todosWithSubTaskCount.size
        }
        private fun itemClicked(itemPosition:Int){

            val todoUniqueId = todosWithSubTaskCount[itemPosition].todoEntity.uniqueId
            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
            intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.VIEW_TODO_FRAGMENT)
            intent.putExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
            viewTodoResultContract.launch(intent)
        }
        inner class  ItemViewHolder(var todoItemBinding: TodoItemBinding): RecyclerView.ViewHolder(todoItemBinding.root),View.OnClickListener{
            override fun onClick(view: View?) {

                itemClicked(adapterPosition)
            }

        }
    }
    interface TodoFragmentCommunication {
        fun onDataChanged()
    }
}