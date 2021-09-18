package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.*

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

    @Query("SELECT *," +
            " ( " +
            " SELECT SUM(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT}) FROM ${BillPaymentEntity.TABLE_NAME} " +
            "  WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID} = ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_UNIQUE_ID}" +
            "  AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}" +
            " )  AS totalPayment " +
            " FROM ${BillEntity.TABLE_NAME} WHERE ${BillEntity.COLUMN_DUE_DATE} >= :startDate AND ${BillEntity.COLUMN_DUE_DATE} <= :endDate AND ${BillEntity.COLUMN_STATUS}= ${BillEntity.NOT_DELETED_STATUS} ORDER BY ${BillEntity.COLUMN_DUE_DATE} ASC")
    fun getBillsByDateRange(startDate:String,endDate:String):List<BillEntityWithTotalPayment>

    @Query("SELECT *," +
            " ( " +
            " SELECT SUM(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT}) FROM ${BillPaymentEntity.TABLE_NAME} " +
            "  WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID} = ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_UNIQUE_ID}" +
            "  AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}" +
            " )  AS totalPayment " +
            " FROM ${BillEntity.TABLE_NAME} WHERE ${BillEntity.COLUMN_UNIQUE_ID} =:uniqueId LIMIT 1")
    fun getBillWithTotalPayment(uniqueId:String):BillEntityWithTotalPayment
    @Query("SELECT COUNT(${BillEntity.COLUMN_GROUP_UNIQUE_ID}) FROM ${BillEntity.TABLE_NAME} WHERE ${BillEntity.COLUMN_STATUS} = ${BillEntity.NOT_DELETED_STATUS} AND " +
            " ${BillEntity.COLUMN_GROUP_UNIQUE_ID} =:groupUniqueId")
    suspend fun getRecordCountByGroupId(groupUniqueId:String):Int
    @Query("UPDATE ${BillEntity.TABLE_NAME} SET ${BillEntity.COLUMN_STATUS} = ${BillEntity.DELETED_STATUS}," +
            " ${BillEntity.COLUMN_UPLOADED} = ${BillEntity.NOT_UPLOADED} WHERE ${BillEntity.COLUMN_GROUP_UNIQUE_ID} =:groupUniqueId" +
            " AND ${BillEntity.COLUMN_DUE_DATE} >= :selectedBillDueDate ")
    suspend fun updateSelectedAndFutureBillAsDeleted(groupUniqueId:String,selectedBillDueDate:String):Int
    @Query("UPDATE ${BillEntity.TABLE_NAME} SET ${BillEntity.COLUMN_STATUS} = ${BillEntity.DELETED_STATUS}," +
            " ${BillEntity.COLUMN_UPLOADED} = ${BillEntity.NOT_UPLOADED} WHERE ${BillEntity.COLUMN_UNIQUE_ID} =:billUniqueId")
    suspend fun updateSelectedBillAsDeleted(billUniqueId:String):Int
    @Query("SELECT TOTAL(${BillEntity.COLUMN_AMOUNT})  FROM ${BillEntity.TABLE_NAME} WHERE ${BillEntity.COLUMN_DUE_DATE} >=:startDate AND ${BillEntity.COLUMN_DUE_DATE} <=:endDate  AND ${BillEntity.COLUMN_STATUS} = ${BillEntity.NOT_DELETED_STATUS}")
    suspend fun getTotalAmountDue(startDate:String,endDate:String):Double
    @Query("SELECT TOTAL(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT})  FROM ${BillEntity.TABLE_NAME} " +
            " LEFT JOIN ${BillPaymentEntity.TABLE_NAME} " +
            " ON ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_UNIQUE_ID} = ${BillPaymentEntity.TABLE_NAME}.${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID}" +
            " AND ${BillPaymentEntity.TABLE_NAME}.${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}" +
            " WHERE ${BillEntity.COLUMN_DUE_DATE} >=:startDate AND ${BillEntity.COLUMN_DUE_DATE} <= :endDate  AND ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_STATUS} = ${BillEntity.NOT_DELETED_STATUS} ")
    suspend fun getTotalPaymentAmount(startDate:String,endDate:String):Double
    @Query("" +
            " SELECT TOTAL(total_amount) AS total_amount FROM ( " +
            "    SELECT TOTAL((quantity*price_per_unit)) as total_amount FROM grocery_items " +
            "    WHERE bought = 1 AND datetime_modified >= :fromDate AND  datetime_modified <= :toDate AND grocery_items.item_status = 0 " +
            "    GROUP BY item_name " +
            "    UNION " +
            "    SELECT  TOTAL(payment_amount)  as total_amount FROM bill_payments " +
            "    LEFT JOIN bills ON bills.unique_id = bill_payments.bill_unique_id " +
            "    WHERE payment_date >= :fromDate AND  payment_date <= :toDate AND  bill_payments.status = 0 " +
            "    GROUP BY name " +
            " ) ")
    suspend fun getExpenses(fromDate:String,toDate:String): ExpensesEntity


}