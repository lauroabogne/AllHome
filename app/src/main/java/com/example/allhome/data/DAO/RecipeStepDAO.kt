package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.RecipeStepEntity

@Dao
interface RecipeStepDAO {

    @Insert
    suspend fun saveSteps(steps:List<RecipeStepEntity>):List<Long>
}