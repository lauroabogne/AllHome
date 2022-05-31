package com.example.allhome.todo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.withTransaction
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.BillDAO
import com.example.allhome.data.DAO.ExpensesDAO
import com.example.allhome.data.DAO.TodoSubTasksDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import com.example.allhome.expenses.viewmodel.ExpensesFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CreateEditTodoFragmentViewModel(private val database: AllHomeDatabase, private val todosDAO:TodosDAO, private val todoSubTasksDAO: TodoSubTasksDAO): ViewModel() {

    var mTodoUniqueId:MutableLiveData<String> = MutableLiveData()
    var mTodoName:MutableLiveData<String> = MutableLiveData()
    var mDueDateCalendar: MutableLiveData<Calendar> = MutableLiveData()
    var mRepeatUntilCalendar: MutableLiveData<Calendar> = MutableLiveData()
    var mNotify:MutableLiveData<Int> = MutableLiveData(0)
    var mNotifyType:MutableLiveData<String> = MutableLiveData("none")
    var mNotifyEvery:MutableLiveData<Int> = MutableLiveData(0)
    var mNotifyEveryType:MutableLiveData<String> = MutableLiveData("none")
    var mTodoSubTask:MutableList<TodoSubTasksEntity> = mutableListOf()
    var mSaveSuccessfully:MutableLiveData<Boolean> = MutableLiveData(false)


    fun saveTodo(todoEntity: TodoEntity){
         viewModelScope.launch {

             database.withTransaction {
                 val id = todosDAO.save(todoEntity)
                 val isSuccessfullySaved = id > 0

                 if(isSuccessfullySaved && mTodoSubTask.size <=0){
                     withContext(IO){
                         mSaveSuccessfully.postValue(true)
                     }
                 }else if(isSuccessfullySaved && mTodoSubTask.size >0){
                     val ids = todoSubTasksDAO.saveMany(mTodoSubTask)
                     if(ids.isEmpty()){
                         withContext(IO){
                             mSaveSuccessfully.postValue(false)
                         }
                     }else{
                         withContext(IO){
                             mSaveSuccessfully.postValue(true)
                         }
                     }
                 }else if(id<=0){
                     withContext(IO){
                         mSaveSuccessfully.postValue(false)
                     }
                 }
             }


        }
    }


}
class CreateEditTodoFragmentViewModelFactory(private val database: AllHomeDatabase,private val todosDAO: TodosDAO,private val todoSubTasksDAO: TodoSubTasksDAO) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateEditTodoFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateEditTodoFragmentViewModel(database,todosDAO,todoSubTasksDAO) as T

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
