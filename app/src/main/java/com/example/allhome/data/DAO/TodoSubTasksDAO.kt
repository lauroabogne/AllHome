package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity

@Dao
interface TodoSubTasksDAO {

    @Insert
    fun saveMany(todoSubTasksEntities: List<TodoSubTasksEntity>):List<Long>
    @Query("SELECT * FROM todo_subtasks WHERE todo_unique_id= :todoUniqueId AND item_status= ${TodoSubTasksEntity.NOT_DELETED_STATUS}")
    fun getSubTasks(todoUniqueId:String):List<TodoSubTasksEntity>
    @Query("UPDATE todo_subtasks SET item_status= ${TodoSubTasksEntity.DELETED_STATUS} WHERE todo_unique_id = :todoUniqueId")
    fun updateSubTasksAsDeleted(todoUniqueId:String):Int
    @Query("UPDATE  todo_subtasks  SET item_status = ${TodoSubTasksEntity.DELETED_STATUS}" +
            " WHERE " +
            " todo_unique_id  IN " +
            " ( " +
            " SELECT unique_id FROM todos " +
            " WHERE " +
            " group_unique_id = (SELECT group_unique_id FROM todos WHERE unique_id=:todoUniqueId) " +
            " AND " +
            " due_date >= (SELECT due_date FROM todos WHERE unique_id=:todoUniqueId) " +
            " )")
    fun updateSelectedTodoAndFutureSubTasksAsDeleted(todoUniqueId:String)

    @Query("UPDATE  todo_subtasks SET item_status = ${TodoSubTasksEntity.DELETED_STATUS}, modified=  datetime('now')  WHERE " +
            " item_status = ${TodoSubTasksEntity.NOT_DELETED_STATUS} AND  " +
            " todo_unique_id IN ( " +
            " SELECT  unique_id FROM todos WHERE group_unique_id=:todoGroupUniqueId  AND item_status = ${TodoEntity.NOT_DELETED_STATUS}" +
            " AND due_date >=:todoDueDate" +
            ")"
            )
    fun updateSelectedTodoAndFutureSubTasksAsDeleted(todoGroupUniqueId:String,todoDueDate:String):Int
    @Query("UPDATE  todo_subtasks SET item_status = ${TodoSubTasksEntity.DELETED_STATUS}, modified=  datetime('now')  WHERE " +
            " item_status = ${TodoSubTasksEntity.NOT_DELETED_STATUS} AND  " +
            " todo_unique_id = :unique_id"
    )
    fun updateSelectedTodoAsDeleted(unique_id:String):Int

    @Query("UPDATE todo_subtasks SET is_finished = :isFinished, datetime_finished = :currentDatetime WHERE unique_id = :subTaskUniqueId AND todo_unique_id = :todoUniqueId")
    fun updateSelectedSubTaskAsFinished(subTaskUniqueId:String, todoUniqueId:String, currentDatetime:String, isFinished:Int)

    @Query("DELETE FROM todo_subtasks WHERE  created = modified AND todo_unique_id=:todoGroupUniqueId AND uploaded=${TodoSubTasksEntity.NOT_UPLOADED}")
    fun deleteSelectedTodoAndFutureSubTasksAsDeleted(todoGroupUniqueId:String):Int

}