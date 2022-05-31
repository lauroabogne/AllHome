package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.data.entities.TodoSubTasksEntity

@Dao
interface TodoSubTasksDAO {

    @Insert
    fun saveMany(todoSubTasksEntities: List<TodoSubTasksEntity>):List<Long>
}