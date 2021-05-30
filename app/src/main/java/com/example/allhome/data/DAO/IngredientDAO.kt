package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.IngredientEntity

@Dao
interface IngredientDAO{

    @Insert
    suspend fun saveIngredients(ingredients:List<IngredientEntity>):List<Long>

}
