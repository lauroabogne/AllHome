package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.RecipeCategoryAssignmentEntity
import com.example.allhome.data.entities.RecipeCategoryEntity

@Dao
interface RecipeCategoryAssignmentDAO {

    @Insert
    fun saveRecipeCategoryAssignments(recipeCategoryAssignmentEntity:ArrayList<RecipeCategoryAssignmentEntity>):List<Long>

    @Query("SELECT recipe_categories.* FROM recipe_categories " +
            "JOIN recipe_category_assignments " +
            "ON recipe_categories.unique_id = recipe_category_assignments.recipe_category_unique_id " +
            "WHERE recipe_category_assignments.recipe_unique_id = :recipeUniqueId " +
            "AND recipe_category_assignments.status = 0 " +
            "AND recipe_categories.status = 0")
    fun getRecipeCategories(recipeUniqueId:String):List<RecipeCategoryEntity>

    @Query("UPDATE recipe_category_assignments SET status = ${RecipeCategoryAssignmentEntity.DELETED_STATUS}, modified=:datetimeDeleted WHERE recipe_unique_id=:recipeUniqueId")
    fun setRecipeCategoryAssignmentAsDeleted(recipeUniqueId:String,datetimeDeleted:String)
}