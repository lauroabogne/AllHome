package com.example.allhome.data.DAO

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount

@Dao
interface TodosDAO {

    @Insert
    fun save(todoEntity: TodoEntity):Long
    @Insert
    fun saveMany(todoEntities:ArrayList<TodoEntity>):List<Long>
    @Query("SELECT *,(SELECT count(*) from todo_subtasks WHERE todo_unique_id = todos.unique_id) as totalSubTaskCount  FROM todos WHERE item_status = ${TodoEntity.NOT_DELETED_STATUS}")
    fun selectTodos():List<TodosWithSubTaskCount>
    @Query("SELECT *,(SELECT count(*) from todo_subtasks WHERE todo_unique_id = todos.unique_id) as totalSubTaskCount  FROM todos WHERE item_status = ${TodoEntity.NOT_DELETED_STATUS} AND date(due_date) =:date")
    fun getTodosByDueDate(date: String): List<TodosWithSubTaskCount>
    @Query("SELECT * FROM todos WHERE  unique_id=:uniqueId AND item_status =${TodoEntity.NOT_DELETED_STATUS} LIMIT 1")
    fun getTodo(uniqueId:String):TodoEntity
    @Query("SELECT COUNT(*) FROM todos WHERE group_unique_id =:groupUniqueId")
    fun getTodoCountByGroupUniqueId(groupUniqueId:String):Integer
    @Query("UPDATE todos SET uploaded= ${TodoEntity.NOT_UPLOADED}, item_status =${TodoEntity.DELETED_STATUS} WHERE unique_id=:uniqueId")
    fun updateAsDeleted(uniqueId: String):Int
    @Query("UPDATE todos SET modified=  datetime('now'), uploaded= ${TodoEntity.NOT_UPLOADED}, item_status =${TodoEntity.DELETED_STATUS} " +
            " WHERE group_unique_id=:groupUniqueId AND due_date >= :dueDate")
    fun updateAsDeletedByGroupIdAndDueDate(groupUniqueId: String,dueDate:String):Int
    @Query("UPDATE  todos  SET item_status = ${TodoEntity.DELETED_STATUS}" +
            " WHERE " +
            " group_unique_id = (SELECT group_unique_id FROM todos WHERE unique_id=:uniqueId)" +
            " AND " +
            " due_date >= (SELECT due_date FROM todos WHERE unique_id=:uniqueId)")
    fun updateSelectedAndFutureTodoAsDeleted(uniqueId: String):Int

    @Query("DELETE FROM todos WHERE group_unique_id=:groupUniqueId AND uploaded = ${TodoEntity.NOT_UPLOADED} AND created = modified")
    fun deleteByGroupIdAndDueDate(groupUniqueId: String):Int

    @Query("UPDATE todos SET is_finished = :isFinished, datetime_finished = :currentDatetime WHERE unique_id = :todoUniqueId")
    fun updateSelectedTodoAsFinished(todoUniqueId:String, currentDatetime:String, isFinished:Int)
    @Query(" UPDATE todos SET  name =:name, due_date = :dueDate, repeat_every = :repeatEvery, repeat_every_type=:repeatEveryType, repeat_until= :repeatUntil," +
            " notify_at =:notifyAt, notify_every_type =:notifyEveryType, uploaded= ${TodoEntity.NOT_UPLOADED}, item_status =${TodoEntity.NOT_DELETED_STATUS}, " +
            " is_finished =:isFinished, datetime_finished =:datetimeFinished, modified = datetime('now') " +
            "  WHERE unique_id=:uniqueId")
    fun updateATodo(uniqueId:String,name:String , dueDate:String, repeatEvery:Int,repeatEveryType:String,
                    repeatUntil:String,notifyAt:Int,notifyEveryType:String, isFinished:Int,
                    datetimeFinished:String):Int

}