package com.example.allhome.data.DAO

import androidx.room.*
import com.example.allhome.data.entities.*
import com.example.allhome.data.entities.ExpensesEntityWithItemNameAndType

@Dao
interface BillPaymentDAO {
    @Insert
     fun saveBillPayment(billPaymentEntity:BillPaymentEntity):Long
    @Query("SELECT SUM(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT}) from ${BillPaymentEntity.TABLE_NAME} WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID}=:billUniqueId AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}")
     fun getTotalPayment(billUniqueId:String):Double
    @Query("SELECT * FROM ${BillPaymentEntity.TABLE_NAME} WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID} = :billUniqueId AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}")
     fun getBillPayments(billUniqueId:String):List<BillPaymentEntity>
    @Query("UPDATE ${BillPaymentEntity.TABLE_NAME} SET ${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT} = :paymentAmount," +
            " ${BillPaymentEntity.COLUMN_PAYMENT_DATE} = :paymentDate, ${BillPaymentEntity.COLUMN_PAYMENT_NOTE} =:paymentNote," +
            " ${BillPaymentEntity.COLUMN_IMAGE_NAME} = :imageName, ${BillPaymentEntity.COLUMN_MODIFIED} =:modified," +
            " ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}, ${BillPaymentEntity.COLUMN_UPLOADED} = ${BillPaymentEntity.NOT_UPLOADED}" +
            " WHERE ${BillPaymentEntity.COLUMN_UNIQUE_ID} = :uniqueId")
     fun updatePayment(uniqueId: String,paymentAmount:Double,paymentDate:String,paymentNote:String,imageName:String,modified:String):Int

    @Query("UPDATE ${BillPaymentEntity.TABLE_NAME} SET ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.DELETED_STATUS}" +
            " WHERE ${BillPaymentEntity.COLUMN_UNIQUE_ID} = :uniqueId")
     fun updatePaymentAsDeleted(uniqueId:String):Int

    @Query(
        " SELECT " +
                "RANDOM() as unique_id, bill_payments.payment_date as expense_date, TOTAL(bill_payments.payment_amount) as amount , bills.name as  item_name,'bill_payments' as expense_type  " +
                " FROM bill_payments " +
                " LEFT JOIN bills " +
                " ON bills.unique_id =  bill_payments.bill_unique_id " +
                " WHERE payment_date >= :fromDate AND payment_date <=:toDate" +
                " GROUP BY name"
    )
    fun getBillPaymentExpenses(fromDate: String, toDate: String): List<ExpensesEntityWithItemNameAndType>

    @Query(
        " SELECT " +
                "TOTAL(bill_payments.payment_amount)" +
                " FROM bill_payments " +
                " LEFT JOIN bills " +
                " ON bills.unique_id =  bill_payments.bill_unique_id " +
                " WHERE payment_date >= :fromDate AND payment_date <=:toDate" +
                " GROUP BY name"
    )
    fun getTotalBillPaymentExpenses(fromDate: String, toDate: String): Double


    @Query("SELECT ${BillPaymentEntity.COLUMN_UNIQUE_ID} FROM ${BillPaymentEntity.TABLE_NAME} WHERE ${BillPaymentEntity.COLUMN_UPLOADED} = :notUpload ORDER BY created")
    fun getUniqueIdsToUpload(notUpload: Int): List<String>
    @Query("SELECT * FROM ${BillPaymentEntity.TABLE_NAME} WHERE ${BillPaymentEntity.COLUMN_UNIQUE_ID} = :uniqueId")
    suspend fun getBillPaymentByUniqueId(uniqueId: String): BillPaymentEntity?
    @Query("UPDATE ${BillPaymentEntity.TABLE_NAME} SET ${BillPaymentEntity.COLUMN_UPLOADED} = :uploaded WHERE ${BillPaymentEntity.COLUMN_UNIQUE_ID} = :uniqueId")
    suspend fun updateBillPaymentAsUploaded(uniqueId: String, uploaded: Int):Int

}