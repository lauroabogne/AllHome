package com.example.allhome.todo.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.allhome.AllHomeBaseApplication
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.TodoSubTasksDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewTodoFragmentViewModel( private val todosDAO: TodosDAO,val todoSubTasksDAO: TodoSubTasksDAO):ViewModel() {
    var mLoadData:MutableLiveData<Boolean> = MutableLiveData()
    var mTodoEntity:MutableLiveData<TodoEntity> = MutableLiveData()
    var mTodoSubTasksEntities:MutableLiveData<List<TodoSubTasksEntity>> = MutableLiveData(arrayListOf())

    fun getTodo(uniqueId:String){
        viewModelScope.launch {

            mTodoEntity.value = withContext(IO){
                todosDAO.getTodo(uniqueId)
            }

        }
    }
    fun getSubTask(uniqueId:String){
        Log.e("UNIQUE_ID",uniqueId)
        viewModelScope.launch {
            mTodoSubTasksEntities.value = withContext(IO){
                val a = todoSubTasksDAO.getSubTasks(uniqueId)
                Log.e("COUNT","${a.size}")
                a
            }
        }
    }
}
class ViewTodoFragmentViewModelFactory( private val todosDAO: TodosDAO,private val todoSubTasksDAO: TodoSubTasksDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewTodoFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewTodoFragmentViewModel(todosDAO,todoSubTasksDAO) as T

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}