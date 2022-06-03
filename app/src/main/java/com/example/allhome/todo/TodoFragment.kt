package com.example.allhome.todo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import com.example.allhome.databinding.FragmentTodoBinding
import com.example.allhome.databinding.TodoItemBinding
import com.example.allhome.todo.viewmodel.TodoFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoFragmentViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TodoFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null


    private val mTodoFragmentViewModel: TodoFragmentViewModel by viewModels{

        val todosDAO = (context?.applicationContext as AllHomeBaseApplication).todosDAO
        TodoFragmentViewModelFactory(todosDAO)

    }

    private val addTodoListResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){

        }
    }

    private val viewTodoResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult->
        if(activityResult.resultCode == Activity.RESULT_OK){

        }
    }

    lateinit var mFragmentTodoBinding:FragmentTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
        requireActivity().title = "To Do List"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        mFragmentTodoBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_todo,null,false)


        val decorator = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        mFragmentTodoBinding.todoListRecyclerview.addItemDecoration(decorator)

        val todoListRecyclerviewViewAdapter = TodoListRecyclerviewViewAdapter(arrayListOf())
        mFragmentTodoBinding.todoListRecyclerview.adapter = todoListRecyclerviewViewAdapter


        mFragmentTodoBinding.fab.setOnClickListener {
            val intent = Intent(requireContext(), TodoFragmentContainerActivity::class.java)
            intent.putExtra(TodoFragmentContainerActivity.FRAGMENT_NAME_TAG,TodoFragmentContainerActivity.CREATE_TODO_FRAGMENT)
            addTodoListResultContract.launch(intent)
        }

        mTodoFragmentViewModel.mLoadData.observe(viewLifecycleOwner) { loadData->
            if(loadData == false){
                return@observe
            }
            Toast.makeText(requireContext(),"Load data ${mTodoFragmentViewModel.mTodoEntities.size}",Toast.LENGTH_SHORT).show()

            val todoListRecyclerviewViewAdapter = mFragmentTodoBinding.todoListRecyclerview.adapter as TodoListRecyclerviewViewAdapter
            todoListRecyclerviewViewAdapter.todosWithSubTaskCount = mTodoFragmentViewModel.mTodoEntities as ArrayList<TodosWithSubTaskCount>
            todoListRecyclerviewViewAdapter.notifyDataSetChanged()
            //mFragmentExpensesBinding.invalidateAll()


        }

        mTodoFragmentViewModel.getTodos()


        return mFragmentTodoBinding.root
    }

    companion object {

        @JvmStatic fun newInstance(param1: String, param2: String) =
            TodoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    inner class TodoListRecyclerviewViewAdapter(var todosWithSubTaskCount:ArrayList<TodosWithSubTaskCount>): RecyclerView.Adapter<TodoListRecyclerviewViewAdapter.ItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val expensesMonthlyItemBinding = TodoItemBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(expensesMonthlyItemBinding)
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val todoWithSubTaskCount = todosWithSubTaskCount[position]
            holder.todoItemBinding.todosWithSubTaskCount = todoWithSubTaskCount
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
            addTodoListResultContract.launch(intent)
        }
        inner class  ItemViewHolder(var todoItemBinding: TodoItemBinding): RecyclerView.ViewHolder(todoItemBinding.root),View.OnClickListener{
            override fun onClick(view: View?) {

                itemClicked(adapterPosition)
            }

        }
    }
}