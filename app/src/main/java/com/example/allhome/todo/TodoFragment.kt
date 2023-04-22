package com.example.allhome.todo

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.bill.BillCustomDateRangeDialogFragment
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import com.example.allhome.databinding.FragmentTodoBinding
import com.example.allhome.databinding.TodoItemBinding
import com.example.allhome.todo.viewmodel.TodoFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoFragmentViewModelFactory
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TodoFragment : Fragment() {
    private var currentDate = LocalDate.now()
    private var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var selectedDateString:String = currentDate.format(dateFormatter)

    private val mTodoFragmentViewModel: TodoFragmentViewModel by viewModels{

        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        TodoFragmentViewModelFactory(todosDAO)

    }

    private val addOrUpdateTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){

            val action = activityResult.data?.getIntExtra(ACTION_TAG,0)
            if(action == RELOAD_ACTION_TAG){
                mTodoFragmentViewModel.getTodos(selectedDateString)
            }

        }
    }

    private val viewTodoResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){
            val action = activityResult.data?.getIntExtra(ACTION_TAG,0)
            if(action == RELOAD_ACTION_TAG){
                mTodoFragmentViewModel.getTodos(selectedDateString)
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


        //val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        //mFragmentTodoBinding.todoListRecyclerview.addItemDecoration(decorator)

        val todoListRecyclerviewViewAdapter = TodoListRecyclerviewViewAdapter(arrayListOf())
        mFragmentTodoBinding.todoListRecyclerview.adapter = todoListRecyclerviewViewAdapter


        mFragmentTodoBinding.fab.setOnClickListener {
            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
            intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.CREATE_TODO_FRAGMENT)
            addOrUpdateTodoListResultContract.launch(intent)
        }

        mTodoFragmentViewModel.mLoadData.observe(viewLifecycleOwner) { loadData->
            if(loadData == false){
                return@observe
            }
            Toast.makeText(requireContext(),"Load data ${mTodoFragmentViewModel.mTodoEntities.size}",Toast.LENGTH_SHORT).show()

            val todoListRecyclerviewViewAdapter = mFragmentTodoBinding.todoListRecyclerview.adapter as TodoListRecyclerviewViewAdapter
            todoListRecyclerviewViewAdapter.todosWithSubTaskCount = mTodoFragmentViewModel.mTodoEntities as ArrayList<TodosWithSubTaskCount>
            todoListRecyclerviewViewAdapter.notifyDataSetChanged()


        }


        mFragmentTodoBinding.todoTabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {

                val selectedTabText = tab?.text.toString()
                when (selectedTabText) {
                    resources.getString(R.string.yesterday_todo) -> {

                        selectedDateString= currentDate.minusDays(1).format(dateFormatter)
                        mTodoFragmentViewModel.getTodos(selectedDateString)
                        
                    }
                    resources.getString(R.string.today_todo) -> {

                        selectedDateString = currentDate.format(dateFormatter)
                        mTodoFragmentViewModel.getTodos(selectedDateString)

                    }
                    resources.getString(R.string.tomorrow_todo) -> {

                        selectedDateString = currentDate.plusDays(1).format(dateFormatter)
                        mTodoFragmentViewModel.getTodos(selectedDateString)

                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        val customViewTabItem = mFragmentTodoBinding.todoTabLayout.getTabAt(3);
        customViewTabItem?.view?.setOnClickListener {
            showCalendar()
        }


        mFragmentTodoBinding.swipeRefresh.setOnRefreshListener {
            mTodoFragmentViewModel.getTodos(selectedDateString)
            mFragmentTodoBinding.swipeRefresh.isRefreshing = false
        }
        mFragmentTodoBinding.todoTabLayout.getTabAt(1)?.select()

        return mFragmentTodoBinding.root
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

            selectedDateString= SimpleDateFormat("yyyy-MM-dd").format(date)
            mTodoFragmentViewModel.getTodos(selectedDateString)

        }

        val datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
        datePickerDialog.show()

    }

    companion object {
        const val ACTION_TAG = "ACTION_TAG"
        const val RELOAD_ACTION_TAG = 1
        @JvmStatic fun newInstance(param1: String, param2: String) =
            TodoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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
            holder.todoItemBinding.checkBox2.setOnCheckedChangeListener(itemOnCheckChangeListener)
            holder.todoItemBinding.checkBox2.isChecked = todoWithSubTaskCount.todoEntity.isFinished == TodoSubTasksEntity.FINISHED
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
}