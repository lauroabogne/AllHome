package com.example.allhome.data.entities

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Embedded
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
    @ColumnInfo(name= COLUMN_PAYMENT_AMOUNT) var paymentAmount:Double,
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


@BindingAdapter(value=["bind:paymentOldImageUri","bind:paymentNewImageUri"],requireAll = false)
fun setImageInImageView(view: View, paymentOldImageUri: Uri?, paymentNewImageUri: Uri?){

    if(paymentNewImageUri !=null){
        (view as ImageView).setImageURI(paymentNewImageUri)
    }else if(paymentNewImageUri ==null && paymentOldImageUri !=null){
        (view as ImageView).setImageURI(paymentOldImageUri)
    }
}
@BindingAdapter(value = ["billPaymentImage","noImageDrawable"],requireAll = false)
fun billPaymentImage(view: View, billPaymentImage: Uri?,noImageDrawable: Drawable?){

    if(billPaymentImage !=null){
        (view as ImageView).setImageURI(billPaymentImage)
    }else{

        (view as ImageView).setImageDrawable(noImageDrawable)
    }

}
