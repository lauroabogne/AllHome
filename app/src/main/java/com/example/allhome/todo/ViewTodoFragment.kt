package com.example.allhome.todo

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
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
import com.example.allhome.NotificationReceiver
import com.example.allhome.R
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.databinding.FragmentViewTodoBinding
import com.example.allhome.databinding.TodoItemSubTaskBinding
import com.example.allhome.todo.viewmodel.ViewTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.ViewTodoFragmentViewModelFactory
import com.example.allhome.utils.CustomAlarmManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList


private const val TODO_UNIQUE_ID_TAG = "param1"
private const val TAG = "ViewTodoFragment"



class ViewTodoFragment : Fragment() {


    private val addEditTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->

        if(activityResult.resultCode == Activity.RESULT_OK){

            val newTodoUniqueId = activityResult.data?.getStringExtra(NEW_UNIQUE_ID_TAG)
            todoUniqueId = newTodoUniqueId.toString()
            mViewTodoFragmentViewModel.mLoadData.value = true
            mTodoEdited = true

        }
    }



    private val mViewTodoFragmentViewModel: ViewTodoFragmentViewModel by viewModels{
        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        val todoSubTasksDAO = (context?.applicationContext as AllHomeBaseApplication).todoSubTasksDAO
        ViewTodoFragmentViewModelFactory(todosDAO,todoSubTasksDAO)

    }
    companion object {
        const val TODO_UNIQUE_ID_TAG = "TODO_UNIQUE_ID_TAG"
        const val NEW_UNIQUE_ID_TAG = "NEW_UNIQUE_ID_TAG"

        @JvmStatic fun newInstance(todoUniqueId:String) =
            ViewTodoFragment().apply {
                arguments = Bundle().apply {
                    putString(TODO_UNIQUE_ID_TAG, todoUniqueId)

                }
            }
    }

    private var mDeleteTodoOptionDialogFragment:DeleteTodoOptionDialogFragment? = null
    private lateinit var todoUniqueId: String
    private var mTodoEdited = false
    lateinit var mFragmentViewTodoBinding:FragmentViewTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            todoUniqueId = it.getString(TODO_UNIQUE_ID_TAG)!!

        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mFragmentViewTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_todo,null,false)

        val toolbar = mFragmentViewTodoBinding.toolbar
        toolbar.title = "Todo"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener {
            if(mTodoEdited){
                val intent = Intent()
                intent.putExtra(TodoFragment.ACTION_TAG, TodoFragment.RELOAD_ACTION_TAG)
                activity?.setResult(Activity.RESULT_OK, intent)

            }
            activity?.finish()
        }
        toolbar?.inflateMenu(R.menu.view_todo_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.view_todo_edit_menu -> {
                   // Toast.makeText(requireContext(),"Edit test",Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
                    intent.putExtra(CreateEditTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
                    intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.CREATE_TODO_FRAGMENT)
                    addEditTodoListResultContract.launch(intent)

                }

                R.id.view_todo_delete_menu -> {

                    mViewTodoFragmentViewModel.checkIfTodoIsRecurring(mViewTodoFragmentViewModel.mTodoEntity.value!!.groupUniqueId)

                }
                R.id.view_todo_cancel_alarm_menu->{
                    Toast.makeText(context, "Cancel alarm",Toast.LENGTH_SHORT).show()
                    mViewTodoFragmentViewModel.mTodoEntity.value?.let { todoEntity -> cancelAlarm(todoEntity.uniqueId, todoEntity.id) }
                }
            }
            true
        }

        mFragmentViewTodoBinding.todoCheckbox.setOnClickListener {


            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val currentDatetime = currentDateTime.format(formatter)
            val isFinished = if (mFragmentViewTodoBinding.todoCheckbox.isChecked) TodoEntity.FINISHED else TodoEntity.NOT_FINISHED

            mViewTodoFragmentViewModel.updateTodoAsFinished(todoUniqueId, currentDatetime,isFinished)

            mTodoEdited = true

        }



        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentViewTodoBinding.subTodoTaskRecyclerView.addItemDecoration(decorator)

        val subTodoTaskRecyclerViewAdapter = SubTodoTaskRecyclerViewAdapter(arrayListOf())
        mFragmentViewTodoBinding.subTodoTaskRecyclerView.adapter = subTodoTaskRecyclerViewAdapter


        mViewTodoFragmentViewModel.mLoadData.observe(viewLifecycleOwner){loadData->
            mViewTodoFragmentViewModel.getTodo(todoUniqueId)
            mViewTodoFragmentViewModel.getSubTask(todoUniqueId)

        }
        mViewTodoFragmentViewModel.mTodoEntity.observe(viewLifecycleOwner){todoEntity->
            mFragmentViewTodoBinding.todoEntity = todoEntity
        }
        mViewTodoFragmentViewModel.mTodoSubTasksEntities.observe(viewLifecycleOwner){ todoSubTasksEntities->
            val subTodoTaskRecyclerViewAdapter = mFragmentViewTodoBinding.subTodoTaskRecyclerView.adapter as SubTodoTaskRecyclerViewAdapter
            subTodoTaskRecyclerViewAdapter.todoSubTasksEntities = todoSubTasksEntities as ArrayList<TodoSubTasksEntity>
            subTodoTaskRecyclerViewAdapter.notifyDataSetChanged()

        }
        mViewTodoFragmentViewModel.mDeleteSelectedTask.observe(viewLifecycleOwner){deleteSelectedTask->

            mViewTodoFragmentViewModel.mTodoEntity.value?.uniqueId?.let { uniqueId ->
                cancelAlarm(uniqueId, mViewTodoFragmentViewModel.mTodoEntity.value!!.id)
                mViewTodoFragmentViewModel.updateSelectedTodoAndSubTodoTaskAsDeleted(uniqueId)
            }

        }

        mViewTodoFragmentViewModel.mTodoEntitiesToCancelAlarm.observe(viewLifecycleOwner){todos->

            todos.forEach { todo->
                cancelAlarm(todo.uniqueId, todo.id)
            }

            mViewTodoFragmentViewModel.updateSelectedAndFutureTodoAndSubTaskAsDeleted(mViewTodoFragmentViewModel.mTodoEntity.value!!.uniqueId)

        }

        mViewTodoFragmentViewModel.mCancelRecurringTodosAlarm.observe(viewLifecycleOwner){deleteAlarm->


            mViewTodoFragmentViewModel.getSelectedAndFutureTodos(mViewTodoFragmentViewModel.mTodoEntity.value!!.uniqueId)

        }

        mViewTodoFragmentViewModel.mDeleteSelectedAndFutureTask.observe(viewLifecycleOwner){deleteSelecedTask->
            mViewTodoFragmentViewModel.mTodoEntity.value?.uniqueId?.let {
                mViewTodoFragmentViewModel.updateSelectedAndFutureTodoAndSubTaskAsDeleted(it)
            }

        }
        mViewTodoFragmentViewModel.mDoTaskUpdatedAsDeletedSuccesfully.observe(viewLifecycleOwner){ isDeletedSuccessfully->

            mDeleteTodoOptionDialogFragment?.let {
                it.dismiss()
            }

            val intent = Intent()
            intent.putExtra(TodoFragment.ACTION_TAG, TodoFragment.RELOAD_ACTION_TAG)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()

        }
        mViewTodoFragmentViewModel.mDoTaskNeedToDeleteIsRecurring.observe(viewLifecycleOwner){ doTaskNeedToDeleteIsRecurring ->

            mDeleteTodoOptionDialogFragment = DeleteTodoOptionDialogFragment("","Selected task is recurring. What you want to delete?")
            mDeleteTodoOptionDialogFragment?.let {deleteTodoOptionDialogFragment->
                deleteTodoOptionDialogFragment.setClickListener { view ->
                  //  mDeleteTodoOptionDialogFragment?.dismiss()

                    val selectedRadioBtn =  mDeleteTodoOptionDialogFragment?.getDeleteTodoDialogFragmentLayoutBinding()?.radioButtonGroup?.checkedRadioButtonId
                    when (view?.id) {
                        DeleteTodoOptionDialogFragment.POSITIVE_BTN_ID -> {
                            when(selectedRadioBtn){
                                R.id.selectedTaskOnlyBtn->{
                                    mViewTodoFragmentViewModel.mDeleteSelectedTask.value = true
                                }
                                R.id.selectedAndAlsoFutureTaskBtn->{
                                    mViewTodoFragmentViewModel.mCancelRecurringTodosAlarm.value = true
                                }
                            }

                        }
                    }
                }
                deleteTodoOptionDialogFragment.show(childFragmentManager,"DeleteTodoOptionDialogFragment")
            }
        }

        mViewTodoFragmentViewModel.mLoadData.value = true

        return mFragmentViewTodoBinding.root
    }

    private fun cancelAlarm(todoEntityUniqueId: String, todoEntityId: Int) {

        val intent = Intent(requireContext(), NotificationReceiver::class.java)
            intent.action = NotificationReceiver.TODO_NOTIFICATION_ACTION
            intent.putExtra(NotificationReceiver.TODO_UNIQUE_ID, todoEntityUniqueId) // Use the same identifier used when setting the alarm


        CustomAlarmManager.cancelAlarm(requireContext(),todoEntityId,intent)
    }
    private fun createPendingIntent(intent: Intent,todoEntityId:Int): PendingIntent {

        return PendingIntent.getBroadcast(requireContext(), todoEntityId, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
    }


    inner class SubTodoTaskRecyclerViewAdapter(var todoSubTasksEntities: ArrayList<TodoSubTasksEntity>): RecyclerView.Adapter<SubTodoTaskRecyclerViewAdapter.ItemViewHolder>() {

        private val itemOnCheckChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            val itemPosition = buttonView?.tag

            val selectedSubtask = todoSubTasksEntities[itemPosition as Int]

            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val currentDatetime = currentDateTime.format(formatter)
            val isFinished = if (isChecked) TodoSubTasksEntity.FINISHED else TodoSubTasksEntity.NOT_FINISHED

            mViewTodoFragmentViewModel.updateSubtaskAsFinished(selectedSubtask.uniqueId, selectedSubtask.todoUniqueId,currentDatetime, isFinished)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val todoItemSubTaskBinding = TodoItemSubTaskBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(todoItemSubTaskBinding)
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val todoSubTasksEntity = todoSubTasksEntities[position]

            holder.todoItemSubTaskBinding.todoSubTasksEntity = todoSubTasksEntity
            holder.todoItemSubTaskBinding.checkBox3.visibility = View.VISIBLE
            holder.todoItemSubTaskBinding.checkBox3.tag = position
            holder.todoItemSubTaskBinding.checkBox3.setOnCheckedChangeListener(itemOnCheckChangeListener)
            holder.todoItemSubTaskBinding.checkBox3.isChecked = todoSubTasksEntity.isFinished == TodoSubTasksEntity.FINISHED
            holder.todoItemSubTaskBinding.root.setOnClickListener(holder)
            holder.todoItemSubTaskBinding.executePendingBindings()
        }
        override fun getItemCount(): Int {
            Log.e("THE_COUNT","${todoSubTasksEntities.size}")
            return todoSubTasksEntities.size
        }
        private fun itemClicked(itemPosition:Int, view: View){
        //           Toast.makeText(requireContext(),"Working here",Toast.LENGTH_SHORT).show()
        //            val todoUniqueId = todosWithSubTaskCount[itemPosition].todoEntity.uniqueId
        //            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
        //            intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.VIEW_TODO_FRAGMENT)
        //            intent.putExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
        //            addTodoListResultContract.launch(intent)
        }
        inner class  ItemViewHolder(var todoItemSubTaskBinding: TodoItemSubTaskBinding): RecyclerView.ViewHolder(todoItemSubTaskBinding.root),View.OnClickListener{
            override fun onClick(view: View?) {
                if (view != null) {
                    todoItemSubTaskBinding.checkBox3.isChecked = !todoItemSubTaskBinding.checkBox3.isChecked
                    itemClicked(adapterPosition, view)
                }
            }

        }
    }
}