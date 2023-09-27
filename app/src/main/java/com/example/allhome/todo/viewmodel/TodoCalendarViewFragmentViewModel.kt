package com.example.allhome.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.allhome.data.DAO.LogsDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.TodosWithSubTaskCount
import kotlinx.coroutines.*

class TodoCalendarViewFragmentViewModel(private val todosDAO: TodosDAO, private val logsDao: LogsDAO): ViewModel() {

    val mCoroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("TodoCalendarViewFragmentViewModel"))

    fun getTaskCount(dueDate:String):Int{
        return todosDAO.getTodoCountByDate(dueDate)

    }
    fun getOverdueTaskCount(dueDate:String):Int{

        return todosDAO.getTodoOverdueCountByDate(dueDate)
    }

    fun getAccomplishedTaskCount(dueDate:String):Int{

        return todosDAO.getTodoAccomplishedCountByDate(dueDate)
    }


}

class TodoCalendarViewFragmentViewModelFactory( private val todosDAO: TodosDAO, private val logsDao: LogsDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoCalendarViewFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoCalendarViewFragmentViewModel(todosDAO,logsDao) as T

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}