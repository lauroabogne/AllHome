package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodosWithSubTaskCount

@Dao
interface TodosDAO {

    @Insert
    fun save(todoEntity: TodoEntity):Long

    @Query("SELECT *,(SELECT count(*) from todo_subtasks WHERE todo_unique_id = todos.unique_id) as totalSubTaskCount  FROM todos")
    fun selectTodos():List<TodosWithSubTaskCount>
}