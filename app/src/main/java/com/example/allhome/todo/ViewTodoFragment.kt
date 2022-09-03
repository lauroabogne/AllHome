package com.example.allhome.todo

import android.app.Activity
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
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.databinding.FragmentViewTodoBinding
import com.example.allhome.databinding.TodoItemSubTaskBinding
import com.example.allhome.todo.viewmodel.ViewTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.ViewTodoFragmentViewModelFactory
import java.util.ArrayList


private const val TODO_UNIQUE_ID_TAG = "param1"
private const val TAG = "ViewTodoFragment"



class ViewTodoFragment : Fragment() {


    private val addEditTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){

        }
    }

    private val mViewTodoFragmentViewModel: ViewTodoFragmentViewModel by viewModels{
        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        val todoSubTasksDAO = (context?.applicationContext as AllHomeBaseApplication).todoSubTasksDAO
        ViewTodoFragmentViewModelFactory(todosDAO,todoSubTasksDAO)

    }
    companion object {
        const val TODO_UNIQUE_ID_TAG = "TODO_UNIQUE_ID_TAG"
        @JvmStatic fun newInstance(todoUniqueId:String) =
            ViewTodoFragment().apply {
                arguments = Bundle().apply {
                    putString(TODO_UNIQUE_ID_TAG, todoUniqueId)

                }
            }
    }

    private var mDeleteTodoOptionDialogFragment:DeleteTodoOptionDialogFragment? = null
    private lateinit var todoUniqueId: String
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
            activity?.finish()
        }
        toolbar?.inflateMenu(R.menu.view_todo_menu)

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.view_todo_edit_menu -> {
                    Toast.makeText(requireContext(),"Edit",Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
                    intent.putExtra(CreateEditTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
                    intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.CREATE_TODO_FRAGMENT)
                    addEditTodoListResultContract.launch(intent)

                }

                R.id.view_todo_delete_menu -> {

                    mViewTodoFragmentViewModel.checkIfTodoIsRecurring(todoUniqueId)

                }
            }
            true
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

            mViewTodoFragmentViewModel.mTodoEntity.value?.uniqueId?.let {
                mViewTodoFragmentViewModel.updateSelectedTodoAndSubTodoTaskAsDeleted(it)
            }

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
            Toast.makeText(requireContext(),"Selected task deleted successfully.",Toast.LENGTH_SHORT).show()
        }

        mViewTodoFragmentViewModel.mDoTaskNeedToDeleteIsRecurring.observe(viewLifecycleOwner){ doTaskNeedToDeleteIsRecurring ->
            mDeleteTodoOptionDialogFragment = DeleteTodoOptionDialogFragment("","Selected task is recurring. What you want to delete?")
            mDeleteTodoOptionDialogFragment?.let {deleteTodoOptionDialogFragment->
                deleteTodoOptionDialogFragment.setClickListener( object:View.OnClickListener{
                    override fun onClick(view: View?) {
                        when(view?.id){
                            R.id.selectedTaskOnlyBtn->{
                                mViewTodoFragmentViewModel.mDeleteSelectedTask.value = true
                            }
                            R.id.selectedAndAlsoFutureTaskBtn->{
                                mViewTodoFragmentViewModel.mDeleteSelectedAndFutureTask.value = true
                            }
                        }
                    }
                })
                deleteTodoOptionDialogFragment.show(childFragmentManager,"DeleteTodoOptionDialogFragment")
            }
        }

        mViewTodoFragmentViewModel.mLoadData.value = true

        return mFragmentViewTodoBinding.root
    }


    inner class SubTodoTaskRecyclerViewAdapter(var todoSubTasksEntities: ArrayList<TodoSubTasksEntity>): RecyclerView.Adapter<SubTodoTaskRecyclerViewAdapter.ItemViewHolder>() {

        val itemOnCheckChangeListener = object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val itemPosition = buttonView?.tag
                Toast.makeText(requireContext(),"${itemPosition}",Toast.LENGTH_SHORT).show()

            }

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
            holder.todoItemSubTaskBinding.root.setOnClickListener(holder)
            holder.todoItemSubTaskBinding.executePendingBindings()
        }
        override fun getItemCount(): Int {
            Log.e("THE_COUNT","${todoSubTasksEntities.size}")
            return todoSubTasksEntities.size
        }
        private fun itemClicked(itemPosition:Int){
            Toast.makeText(requireContext(),"Working here",Toast.LENGTH_SHORT).show()

//            val todoUniqueId = todosWithSubTaskCount[itemPosition].todoEntity.uniqueId
//            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
//            intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.VIEW_TODO_FRAGMENT)
//            intent.putExtra(ViewTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)
//            addTodoListResultContract.launch(intent)
        }
        inner class  ItemViewHolder(var todoItemSubTaskBinding: TodoItemSubTaskBinding): RecyclerView.ViewHolder(todoItemSubTaskBinding.root),View.OnClickListener{
            override fun onClick(view: View?) {

                itemClicked(adapterPosition)
            }

        }
    }
}