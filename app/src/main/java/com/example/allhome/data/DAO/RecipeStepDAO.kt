package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.allhome.data.entities.IngredientEntity
import com.example.allhome.data.entities.RecipeStepEntity

@Dao
interface RecipeStepDAO {

    @Insert
     fun saveSteps(steps:List<RecipeStepEntity>):List<Long>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun saveOrUpdateSteps(steps:List<RecipeStepEntity>):List<Long>

    @Query("SELECT * FROM recipe_steps WHERE recipe_unique_id = :recipeUniqueId AND status  =  ${RecipeStepEntity.NOT_DELETED_STATUS}")
     fun getStepsByRecipeUniqueId(recipeUniqueId:String):List<RecipeStepEntity>

    @Query("UPDATE recipe_steps SET status=${RecipeStepEntity.DELETED_STATUS} WHERE recipe_unique_id=:recipeUniqueId")
     fun updateStepsByRecipeUniqueIdAsDeleted(recipeUniqueId:String):Int
}