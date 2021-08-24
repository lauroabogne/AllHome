package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillEntity
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.data.entities.BillPaymentEntity

@Dao
interface BillDAO {
    @Insert
    fun saveBills(bills:ArrayList<BillEntity>):List<Long>
    @Insert
    fun saveBill(bill:BillEntity):Long
    @Query("SELECT *," +
            " ( " +
            " SELECT SUM(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT}) FROM ${BillPaymentEntity.TABLE_NAME} " +
            "  WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID} = ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_UNIQUE_ID}" +
            "  AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}" +
            " )  AS totalPayment " +
            " FROM ${BillEntity.TABLE_NAME} WHERE strftime('%Y-%m',${BillEntity.COLUMN_DUE_DATE}) = :yearMonth AND ${BillEntity.COLUMN_STATUS}= ${BillEntity.NOT_DELETED_STATUS} ORDER BY ${BillEntity.COLUMN_DUE_DATE} ASC")
    fun getBillsInMonth(yearMonth:String):List<BillEntityWithTotalPayment>
}