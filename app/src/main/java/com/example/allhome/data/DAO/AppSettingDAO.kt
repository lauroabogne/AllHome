package com.example.allhome.data.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.AppSettingEntity
import com.example.allhome.data.entities.GroceryItemEntity

@Dao
interface AppSettingDAO {

    @Insert
    fun insert(appSettingEntity: AppSettingEntity): Long
    @Query("UPDATE ${AppSettingEntity.TABLE_NAME} SET ${AppSettingEntity.COLUMN_VALUE} = :viewingValue, ${AppSettingEntity.COLUMN_MODIFIED} = :modifiedDate, " +
            " ${AppSettingEntity.COLUMN_STATUS} = ${AppSettingEntity.NOT_DELETED_STATUS}, ${AppSettingEntity.COLUMN_UPLOADED} = ${AppSettingEntity.NOT_UPLOADED} " +
            " WHERE ${AppSettingEntity.COLUMN_NAME} = '${AppSettingEntity.RECIPE_COLUMN_NAME}'")
    fun updateRecipeViewingSetting(viewingValue:String,modifiedDate:String)
    @Query("SELECT ${AppSettingEntity.COLUMN_VALUE} from ${AppSettingEntity.TABLE_NAME} WHERE ${AppSettingEntity.COLUMN_NAME} = '${AppSettingEntity.RECIPE_COLUMN_NAME}' AND ${AppSettingEntity.COLUMN_STATUS} = ${AppSettingEntity.NOT_DELETED_STATUS} LIMIT 1")
    fun getRecipeViewing(): String
    @Query("SELECT * from ${AppSettingEntity.TABLE_NAME} WHERE ${AppSettingEntity.COLUMN_NAME} = '${AppSettingEntity.RECIPE_COLUMN_NAME}' AND ${AppSettingEntity.COLUMN_STATUS} = ${AppSettingEntity.NOT_DELETED_STATUS} LIMIT 1")
    fun getRecipeViewingAppSettingEntity(): AppSettingEntity
}