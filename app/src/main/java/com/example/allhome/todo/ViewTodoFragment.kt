package com.example.allhome.todo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.R
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import com.example.allhome.databinding.FragmentViewTodoBinding
import com.example.allhome.databinding.TodoItemBinding
import com.example.allhome.databinding.TodoItemSubTaskBinding
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.CreateEditTodoFragmentViewModelFactory
import com.example.allhome.todo.viewmodel.ViewTodoFragmentViewModel
import com.example.allhome.todo.viewmodel.ViewTodoFragmentViewModelFactory
import okhttp3.internal.notifyAll
import java.util.ArrayList


private const val TODO_UNIQUE_ID_TAG = "param1"
private const val TAG = "ViewTodoFragment"



class ViewTodoFragment : Fragment() {


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


            Toast.makeText(requireContext(),"Toast ${todoSubTasksEntities.size}",Toast.LENGTH_SHORT).show()
        }

        mViewTodoFragmentViewModel.mLoadData.value = true

        return mFragmentViewTodoBinding.root
    }


    inner class SubTodoTaskRecyclerViewAdapter(var todoSubTasksEntities: ArrayList<TodoSubTasksEntity>): RecyclerView.Adapter<SubTodoTaskRecyclerViewAdapter.ItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val todoItemSubTaskBinding = TodoItemSubTaskBinding.inflate(layoutInflater, parent, false)
            return ItemViewHolder(todoItemSubTaskBinding)
        }
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val todoSubTasksEntity = todoSubTasksEntities[position]
            Log.e("NAME","${todoSubTasksEntity.name}")
            holder.todoItemSubTaskBinding.todoSubTasksEntity = todoSubTasksEntity
            holder.todoItemSubTaskBinding.root.setOnClickListener(holder)
            holder.todoItemSubTaskBinding.executePendingBindings()
        }
        override fun getItemCount(): Int {
            Log.e("THE_COUNT","${todoSubTasksEntities.size}")
            return todoSubTasksEntities.size
        }
        private fun itemClicked(itemPosition:Int){

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