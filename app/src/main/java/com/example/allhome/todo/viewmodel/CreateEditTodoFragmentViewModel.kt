package com.example.allhome.todo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.TodoSubTasksDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CreateEditTodoFragmentViewModel(private val database: AllHomeDatabase, private val todosDAO:TodosDAO, private val todoSubTasksDAO: TodoSubTasksDAO): ViewModel() {

    var mTodoUniqueId:MutableLiveData<String> = MutableLiveData()
    var mGroupUniqueId:MutableLiveData<String> = MutableLiveData()
    var mTodoName:MutableLiveData<String> = MutableLiveData()
    var mTodoDescription:MutableLiveData<String> = MutableLiveData()
    var mDueDateCalendar: MutableLiveData<Calendar> = MutableLiveData()
    var mRepeatUntilCalendar: MutableLiveData<Calendar> = MutableLiveData()
    var mRepeatEvery:MutableLiveData<Int> = MutableLiveData()
    var mRepeatEveryType:MutableLiveData<String> = MutableLiveData()
    var mNotifyAt:MutableLiveData<Int> = MutableLiveData()
    var mNotifyEveryType:MutableLiveData<String> = MutableLiveData()
    var mTodoSubTask:MutableLiveData<MutableList<TodoSubTasksEntity>> = MutableLiveData(mutableListOf())
    var mSaveSuccessfully:MutableLiveData<Boolean> = MutableLiveData()
    var mUpdateTask:MutableLiveData<Boolean> = MutableLiveData()
    var mDoTaskNeedToUpdateIsRecurring:MutableLiveData<Boolean> = MutableLiveData()
    var mUpdateSelectedTask:MutableLiveData<Boolean> = MutableLiveData(false)
    var mUpdateFutureAndSelectedTask:MutableLiveData<Boolean> = MutableLiveData(false)

    fun saveTodo(todoEntity: TodoEntity){
         viewModelScope.launch {

             database.withTransaction {
                 val id = todosDAO.save(todoEntity)
                 val isSuccessfullySaved = id > 0

                 if(isSuccessfullySaved && mTodoSubTask.value!!.size <=0){
                     withContext(IO){
                         mSaveSuccessfully.postValue(true)
                     }
                 }else if(isSuccessfullySaved && mTodoSubTask.value!!.size >0){
                     val ids = todoSubTasksDAO.saveMany(mTodoSubTask.value!!)
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

    fun saveTodos(todoEntities:ArrayList<TodoEntity>,todoSunEntities:ArrayList<TodoSubTasksEntity>){
        viewModelScope.launch {
            database.withTransaction {
                todosDAO.saveMany(todoEntities)
                todoSubTasksDAO.saveMany(todoSunEntities)
            }
        }
    }
    fun updateTodos(todoEntities:ArrayList<TodoEntity>,todoSunEntities:ArrayList<TodoSubTasksEntity>,todoUniqueId:String,todoGroupUniqueId:String,selectedTodoDueDate:String){
        viewModelScope.launch {
            database.withTransaction {

               // todoSubTasksDAO.deleteSelectedTodoAndFutureSubTasksAsDeleted(todoGroupUniqueId)
               // todosDAO.deleteByGroupIdAndDueDate(todoGroupUniqueId)

                //todoSubTasksDAO.updateSubTasksAsDeleted(todoUniqueId)
                //todosDAO.updateAsDeleted(todoUniqueId)

                todoSubTasksDAO.updateSelectedTodoAndFutureSubTasksAsDeleted(todoGroupUniqueId,selectedTodoDueDate)
                todosDAO.updateAsDeletedByGroupIdAndDueDate(todoGroupUniqueId, selectedTodoDueDate )

                todosDAO.saveMany(todoEntities)
                todoSubTasksDAO.saveMany(todoSunEntities)
            }
        }
    }

    fun updateTodo(uniqueId:String,name:String,description:String , dueDate:String, repeatEvery:Int,repeatEveryType:String,
                   repeatUntil:String,notifyAt:Int,notifyEveryType:String, isFinished:Int,
                   datetimeFinished:String,
                   todoSunEntities:ArrayList<TodoSubTasksEntity>){

        viewModelScope.launch {
            database.withTransaction {
                todosDAO.updateATodo(uniqueId,name , description,dueDate, repeatEvery,repeatEveryType, repeatUntil,notifyAt,notifyEveryType, isFinished, datetimeFinished)
                todoSubTasksDAO.updateSelectedTodoAsDeleted(uniqueId)
                todoSubTasksDAO.saveMany(todoSunEntities)

            }
        }


    }
    fun getTodoInformation(todoUniqueId:String){
        viewModelScope.launch {
            withContext(IO){
                val todoEntity = todosDAO.getTodo(todoUniqueId)
                todoEntity.let {
                    mTodoName.postValue(it.name)
                    mTodoDescription.postValue(it.description)
                    mGroupUniqueId.postValue(it.groupUniqueId)
                    mDueDateCalendar.postValue(dueDateStringToCalendar(it.dueDate))
                    mRepeatEvery.postValue(it.repeatEvery)
                    mRepeatEveryType.postValue(it.repeatEveryType)
                    mRepeatUntilCalendar.postValue(repeatUntilDateStringToCalendar(it.repeatUntil))
                    mNotifyAt.postValue(it.notifyAt)
                    mNotifyEveryType.postValue(it.notifyEveryType)
                    mTodoSubTask.postValue(todoSubTasksDAO.getSubTasks(todoUniqueId) as MutableList<TodoSubTasksEntity>?)


                }
            }
        }

    }


    private fun dueDateStringToCalendar(stringDueDate:String):Calendar{
        val calendar = Calendar.getInstance()
        if(stringDueDate.contains(" 00:00:00")){
            val stringDate = stringDueDate.replace(" 00:00:00","")
            calendar.time = SimpleDateFormat("yyyy-MM-dd").parse(stringDate)
        }else{
            calendar.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringDueDate)
        }
        return  calendar
    }
    private fun repeatUntilDateStringToCalendar(stringRepeatUntil:String):Calendar{
        val calendar = Calendar.getInstance()
        if(stringRepeatUntil.contains(" 00:00:00")){
            val stringDate = stringRepeatUntil.replace(" 00:00:00","")
            calendar.time = SimpleDateFormat("yyyy-MM-dd").parse(stringDate)
        }else{
            calendar.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringRepeatUntil)
        }
        return  calendar
    }

    fun checkIfTodoIsRecurring(todoGroupUniqueId:String){
        viewModelScope.launch {
            mDoTaskNeedToUpdateIsRecurring.value =  withContext(IO){
                todosDAO.getTodoCountByGroupUniqueId(todoGroupUniqueId) > 0
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
