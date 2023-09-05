package com.example.allhome.todo.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.allhome.data.DAO.TodoSubTasksDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoChecklistEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewTodoFragmentViewModel( private val todosDAO: TodosDAO,val todoSubTasksDAO: TodoSubTasksDAO):ViewModel() {
    var mLoadData:MutableLiveData<Boolean> = MutableLiveData()
    var mDeleteSelectedTask:MutableLiveData<Boolean> = MutableLiveData()
    var mDeleteSelectedAndFutureTask:MutableLiveData<Boolean>  = MutableLiveData()
    var mDoTaskUpdatedAsDeletedSuccesfully:MutableLiveData<Boolean> = MutableLiveData()
    var mTodoEntity:MutableLiveData<TodoEntity> = MutableLiveData()
    var mTodoSubTasksEntities:MutableLiveData<List<TodoChecklistEntity>> = MutableLiveData(arrayListOf())
    var mDoTaskNeedToDeleteIsRecurring:MutableLiveData<Boolean> = MutableLiveData()


    var mTodoEntitiesToCancelAlarm:MutableLiveData<List<TodoEntity>> = MutableLiveData()
    var mCancelRecurringTodosAlarm:MutableLiveData<Boolean> = MutableLiveData()

    fun getTodo(uniqueId:String){
        viewModelScope.launch {

            mTodoEntity.value = withContext(IO){
                todosDAO.getTodo(uniqueId)
            }
        }
    }
    fun getSubTask(uniqueId:String){

        viewModelScope.launch {
            mTodoSubTasksEntities.value = withContext(IO){
                val count = todoSubTasksDAO.getSubTasks(uniqueId)
                Log.e("COUNT","${count.size}")
                count
            }
        }
    }
    fun updateSelectedTodoAndSubTodoTaskAsDeleted(uniqueId:String){
        viewModelScope.launch {

            withContext(IO){
                val todoSubTaskAffected = todoSubTasksDAO.updateSubTasksAsDeleted(uniqueId)
                val todoAffected = todosDAO.updateAsDeleted(uniqueId)
                mDoTaskUpdatedAsDeletedSuccesfully.postValue(todoAffected >0)
            }

        }
    }


    fun updateSelectedAndFutureTodoAndSubTaskAsDeleted(uniqueId:String){
        viewModelScope.launch {
            withContext(IO){
                todoSubTasksDAO.updateSelectedTodoAndFutureSubTasksAsDeleted(uniqueId)
                val todoAffected = todosDAO.updateSelectedAndFutureTodoAsDeleted(uniqueId)
                mDoTaskUpdatedAsDeletedSuccesfully.postValue(todoAffected>0)
            }
        }
    }

    fun getSelectedAndFutureTodos(uniqueId:String){
        viewModelScope.launch {
            withContext(IO){
                mTodoEntitiesToCancelAlarm.postValue( todosDAO.getSelectedAndFutureTodoAsDeleted(uniqueId))
            }
        }

    }
    fun checkIfTodoIsRecurring(todoGroupUniqueId:String){
        viewModelScope.launch {
            mDoTaskNeedToDeleteIsRecurring.value =  withContext(IO){
                todosDAO.getTodoCountByGroupUniqueId(todoGroupUniqueId) > 1
            }
        }
    }

    fun updateSubtaskAsFinished(subTaskUniqueId:String, todoUniqueId:String, currentDatetime:String,isFinished:Int){

        viewModelScope.launch {
            withContext(IO){
                todoSubTasksDAO.updateSelectedSubTaskAsFinished(subTaskUniqueId,todoUniqueId,currentDatetime, isFinished)
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
class ViewTodoFragmentViewModelFactory( private val todosDAO: TodosDAO,private val todoSubTasksDAO: TodoSubTasksDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewTodoFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ViewTodoFragmentViewModel(todosDAO,todoSubTasksDAO) as T

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}