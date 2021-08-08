package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import com.example.allhome.data.entities.BillEntity

@Dao
interface BillDAO {
    @Insert
    fun saveBills(bills:ArrayList<BillEntity>):List<Long>
    @Insert
    fun saveBill(bill:BillEntity):Long
}