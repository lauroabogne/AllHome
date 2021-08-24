package com.example.allhome.data.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.allhome.data.entities.BillEntityWithTotalPayment
import com.example.allhome.data.entities.BillPaymentEntity
import com.example.allhome.data.entities.RecipeEntity

@Dao
interface BillPaymentDAO {
    @Insert
    suspend fun saveBillPayment(billPaymentEntity:BillPaymentEntity):Long
    @Query("SELECT SUM(${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT}) from ${BillPaymentEntity.TABLE_NAME} WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID}=:billUniqueId AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}")
    suspend fun getTotalPayment(billUniqueId:String):Double
    @Query("SELECT * FROM ${BillPaymentEntity.TABLE_NAME} WHERE ${BillPaymentEntity.COLUMN_BILL_UNIQUE_ID} = :billUniqueId AND ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}")
    suspend fun getBillPayments(billUniqueId:String):List<BillPaymentEntity>
    @Query("UPDATE ${BillPaymentEntity.TABLE_NAME} SET ${BillPaymentEntity.COLUMN_PAYMENT_AMOUNT} = :paymentAmount," +
            " ${BillPaymentEntity.COLUMN_PAYMENT_DATE} = :paymentDate, ${BillPaymentEntity.COLUMN_PAYMENT_NOTE} =:paymentNote," +
            " ${BillPaymentEntity.COLUMN_IMAGE_NAME} = :imageName, ${BillPaymentEntity.COLUMN_MODIFIED} =:modified," +
            " ${BillPaymentEntity.COLUMN_STATUS} = ${BillPaymentEntity.NOT_DELETED_STATUS}, ${BillPaymentEntity.COLUMN_UPLOADED} = ${BillPaymentEntity.NOT_UPLOADED}" +
            " WHERE ${BillPaymentEntity.COLUMN_UNIQUE_ID} = :uniqueId")
    suspend fun updatePayment(uniqueId: String,paymentAmount:Double,paymentDate:String,paymentNote:String,imageName:String,modified:String):Int


}