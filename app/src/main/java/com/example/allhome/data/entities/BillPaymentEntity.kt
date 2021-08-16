package com.example.allhome.data.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = BillPaymentEntity.TABLE_NAME)
data class BillPaymentEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_BILL_UNIQUE_ID) var billUniqueId:String,
    @ColumnInfo(name= COLUMN_GROUP_UNIQUE_ID) var billGroupUniqueId:String,
    @ColumnInfo(name= COLUMN_PAYMENT_AMOUNT) var paymentAmount:String,
    @ColumnInfo(name= COLUMN_PAYMENT_DATE) var paymentDate:String,
    @ColumnInfo(name= COLUMN_PAYMENT_NOTE) var paymentNote:String,
    @ColumnInfo(name= COLUMN_IMAGE_NAME) var imageName:String,
    @ColumnInfo(name= COLUMN_STATUS,defaultValue="${NOT_DELETED_STATUS}") var status:Int,
    @ColumnInfo(name= COLUMN_UPLOADED,defaultValue="${NOT_UPLOADED}") var uploaded:Int,
    @ColumnInfo(name= COLUMN_CREATED,defaultValue="CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name= COLUMN_MODIFIED,defaultValue="CURRENT_TIMESTAMP") var modified:String
): Parcelable {
    companion object{
        const val NOT_DELETED_STATUS = 0
        const val DELETED_STATUS = 1
        const val NOT_UPLOADED = 0
        const val UPLOADED = 1
        const val TABLE_NAME ="bill_payments"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_BILL_UNIQUE_ID ="bill_unique_id"
        const val COLUMN_GROUP_UNIQUE_ID ="group_unique_id"
        const val COLUMN_PAYMENT_AMOUNT ="payment_amount"
        const val COLUMN_PAYMENT_DATE ="payment_date"
        const val COLUMN_PAYMENT_NOTE ="payment_note"
        const val COLUMN_IMAGE_NAME ="image_name"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}
