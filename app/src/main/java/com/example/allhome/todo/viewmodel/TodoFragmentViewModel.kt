package com.example.allhome.todo.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.LogsDAO
import com.example.allhome.data.DAO.TodoSubTasksDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoFragmentViewModel( private val todosDAO: TodosDAO, private val logsDao:LogsDAO):ViewModel() {
    var mTodoEntities:MutableList<TodosWithSubTaskCount> = mutableListOf()
    var mLoadData:MutableLiveData<Boolean> = MutableLiveData()


    fun getTodos(){
        viewModelScope.launch {
            mTodoEntities = withContext(IO){
                todosDAO.selectTodos() as MutableList<TodosWithSubTaskCount>
            }
            withContext(IO){
                mLoadData.postValue(true)
            }
        }
    }
    fun getTodos(dueDateString:String){

        viewModelScope.launch {
            mTodoEntities = withContext(IO){
                todosDAO.getTodosByDueDate(dueDateString) as MutableList<TodosWithSubTaskCount>
            }
            withContext(IO){
                mLoadData.postValue(true)
            }
        }
    }
    fun getOverdueTodos(currentDateString:String){

        viewModelScope.launch {
            mTodoEntities = withContext(IO){
                todosDAO.getOverdueTodosByDueDate(currentDateString) as MutableList<TodosWithSubTaskCount>
            }
            withContext(IO){
                mLoadData.postValue(true)
            }
        }
    }

    fun updateTodoAsFinished(todoUniqueId:String, currentDatetime:String,isFinished:Int){

        viewModelScope.launch {
            withContext(IO){
                todosDAO.updateSelectedTodoAsFinished(todoUniqueId,currentDatetime,isFinished)
            }
        }

    }

}

class TodoFragmentViewModelFactory( private val todosDAO: TodosDAO, private val logsDao: LogsDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoFragmentViewModel(todosDAO,logsDao) as T

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}