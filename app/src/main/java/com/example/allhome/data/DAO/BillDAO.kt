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
     fun getRecordCountByGroupId(groupUniqueId:String):Int
    @Query("UPDATE ${BillEntity.TABLE_NAME} SET ${BillEntity.COLUMN_STATUS} = ${BillEntity.DELETED_STATUS}," +
            " ${BillEntity.COLUMN_UPLOADED} = ${BillEntity.NOT_UPLOADED} WHERE ${BillEntity.COLUMN_GROUP_UNIQUE_ID} =:groupUniqueId" +
            " AND ${BillEntity.COLUMN_DUE_DATE} >= :selectedBillDueDate ")
     fun updateSelectedAndFutureBillAsDeleted(groupUniqueId:String,selectedBillDueDate:String):Int
    @Query("UPDATE ${BillEntity.TABLE_NAME} SET ${BillEntity.COLUMN_STATUS} = ${BillEntity.DELETED_STATUS}," +
            " ${BillEntity.COLUMN_UPLOADED} = ${BillEntity.NOT_UPLOADED} WHERE ${BillEntity.COLUMN_UNIQUE_ID} =:billUniqueId")
     fun updateSelectedBillAsDeleted(billUniqueId:String):Int
    @Query("SELECT TOTAL(${BillEntity.COLUMN_AMOUNT})  FROM ${BillEntity.TABLE_NAME} WHERE ${BillEntity.COLUMN_DUE_DATE} >=:startDate AND ${BillEntity.COLUMN_DUE_DATE} <=:endDate  AND ${BillEntity.COLUMN_STATUS} = ${BillEntity.NOT_DELETED_STATUS}")
     fun getTotalAmountDue(startDate:String,endDate:String):Double
    @Query("SELECT TOTAL(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT})  FROM ${BillEntity.TABLE_NAME} " +
            " LEFT JOIN ${BillPaymentEntity.TABLE_NAME} " +
            " ON ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_UNIQUE_ID} = ${BillPaymentEntity.TABLE_NAME}.${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID}" +
            " AND ${BillPaymentEntity.TABLE_NAME}.${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}" +
            " WHERE ${BillEntity.COLUMN_DUE_DATE} >=:startDate AND ${BillEntity.COLUMN_DUE_DATE} <= :endDate  AND ${BillEntity.TABLE_NAME}.${BillEntity.COLUMN_STATUS} = ${BillEntity.NOT_DELETED_STATUS} ")
     fun getTotalPaymentAmount(startDate:String,endDate:String):Double
    @Query(" SELECT TOTAL(total_amount) AS total_amount,expense_date FROM ( " +
            "    SELECT TOTAL((quantity*price_per_unit)) as total_amount,strftime('%Y-%m',datetime_modified) as expense_date FROM grocery_items " +
            "    WHERE bought = 1 AND strftime('%Y-%m-%d',datetime_modified) >= :fromDate AND  strftime('%Y-%m-%d',datetime_modified) <= :toDate AND grocery_items.item_status = 0 " +
            "    GROUP BY item_name " +
            "    UNION " +
            "    SELECT  TOTAL(payment_amount)  as total_amount,strftime('%Y-%m',payment_date) as expense_date FROM bill_payments " +
            "    LEFT JOIN bills ON bills.unique_id = bill_payments.bill_unique_id " +
            "    WHERE strftime('%Y-%m-%d',payment_date) >= :fromDate AND  strftime('%Y-%m-%d',payment_date) <= :toDate AND  bill_payments.status = 0 AND bills.status = 0" +
            "    GROUP BY name " +
            " ) ")
     fun getExpenses(fromDate:String,toDate:String): ExpensesEntity

    @Query("SELECT TOTAL(total_amount) AS total_amount,expense_date FROM " +
            "(SELECT  strftime('%Y-%m',datetime_modified) AS expense_date,TOTAL(price_per_unit * quantity) total_amount FROM grocery_items " +
            "   WHERE bought = 1 AND datetime_modified >= :fromDate AND  datetime_modified <= :toDate AND grocery_items.item_status = 0  " +
            "   GROUP BY strftime('%Y-%m',datetime_modified) " +
            "   UNION " +
            "   SELECT  strftime('%Y-%m',payment_date) AS expense_date,TOTAL(payment_amount) as total_amount  FROM bill_payments " +
            "       LEFT JOIN bills ON bills.unique_id = bill_payments.bill_unique_id " +
            "       WHERE payment_date >= :fromDate AND  payment_date <= :toDate AND  bill_payments.status = 0 AND bills.status = 0 " +
            "       GROUP BY strftime('%Y-%m',payment_date) " +
            "      ) " +
            "      GROUP BY  strftime('%Y-%m',expense_date) ORDER BY expense_date ASC")
     fun getExpensesPerMonth(fromDate:String,toDate:String):List<ExpensesEntity>

    @Query("SELECT  expense_date, TOTAL(total_amount) AS total_amount FROM " +
            "(SELECT  strftime('%Y-%m',datetime_modified) AS expense_date,TOTAL(price_per_unit * quantity) total_amount FROM grocery_items " +
            "   WHERE bought = 1 AND strftime('%Y-%m',datetime_modified) = :month AND grocery_items.item_status = 0  " +
            "   GROUP BY strftime('%Y-%m',datetime_modified) " +
            "   UNION " +
            "   SELECT  strftime('%Y-%m',payment_date) AS expense_date,TOTAL(payment_amount) as total_amount  FROM bill_payments " +
            "       LEFT JOIN bills ON bills.unique_id = bill_payments.bill_unique_id " +
            "       WHERE strftime('%Y-%m',payment_date) = :month  AND bills.status = 0 AND bill_payments.status = 0 " +
            "       GROUP BY strftime('%Y-%m',payment_date) " +
            "      ) " +
            "      GROUP BY  strftime('%Y-%m',expense_date) ORDER BY expense_date ASC")
     fun getExpensesInMonth(month:String):ExpensesEntity


    @Query(" SELECT * FROM " +
            " (SELECT  'grocery item' as expense_type,item_name as item_name ,strftime('%Y-%m-%d',datetime_modified) as expense_date, total(quantity * price_per_unit)  as total_amount FROM grocery_items " +
            "  WHERE bought = 1 AND datetime_modified >=  :fromDate AND  datetime_modified <=  :toDate  AND grocery_items.item_status = 0  " +
            "  AND quantity > 0 AND price_per_unit > 0 " +
            "  GROUP BY item_name " +
            "  UNION  " +
            "  SELECT  'bill payment' as expense_type,name as item_name,strftime('%Y-%m-%d',payment_date) AS expense_date, TOTAL(payment_amount) AS total_amount  FROM bill_payments " +
            "  LEFT JOIN bills ON bills.unique_id = bill_payments.bill_unique_id  " +
            "  WHERE payment_date >= :fromDate AND  payment_date <= :toDate AND  bill_payments.status = 0 AND bills.status = 0 " +
            "  GROUP BY name)" +
            " ORDER BY total_amount DESC")
     fun getExpensesWithItemNameAndType(fromDate:String,toDate:String): List<ExpensesEntityWithItemNameAndType>


}