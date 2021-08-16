package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillEntity

@Dao
interface BillDAO {
    @Insert
    fun saveBills(bills:ArrayList<BillEntity>):List<Long>
    @Insert
    fun saveBill(bill:BillEntity):Long
    @Query("SELECT * FROM ${BillEntity.TABLE_NAME} WHERE strftime('%Y-%m',${BillEntity.COLUMN_DUE_DATE}) = :yearMonth AND ${BillEntity.COLUMN_STATUS}= ${BillEntity.NOT_DELETED_STATUS} ORDER BY ${BillEntity.COLUMN_DUE_DATE} ASC")
    fun getBillsInMonth(yearMonth:String):List<BillEntity>
}