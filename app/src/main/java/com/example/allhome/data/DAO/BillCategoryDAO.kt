package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillCategoryEntity
import com.example.allhome.data.entities.BillEntity

@Dao
interface BillCategoryDAO {
    @Insert
    fun saveCategory(bill:BillCategoryEntity):Long
    @Query("Select * from ${BillCategoryEntity.TABLE_NAME} WHERE ${BillCategoryEntity.COLUMN_NAME} = :name LIMIT 1")
     fun getCategory(name:String):BillCategoryEntity
    @Query("SELECT DISTINCT ${BillCategoryEntity.COLUMN_NAME} FROM ${BillCategoryEntity.TABLE_NAME} WHERE ${BillCategoryEntity.COLUMN_NAME} LIKE '%' || :searchTerm || '%' LIMIT 20")
    fun searchDistinctNamesCaseSensitive(searchTerm: String): List<String>
}