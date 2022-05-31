package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.ExpensesEntity
import com.example.allhome.data.entities.TodoEntity

@Dao
interface TodosDAO {

    @Insert
    fun save(todoEntity: TodoEntity):Long

    @Query("SELECT * FROM todos")
    fun selectTodos():List<TodoEntity>
}