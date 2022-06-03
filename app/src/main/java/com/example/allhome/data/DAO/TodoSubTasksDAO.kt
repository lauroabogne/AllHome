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
}