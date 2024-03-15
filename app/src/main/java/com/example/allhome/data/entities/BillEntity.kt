package com.example.allhome.data.entities

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allhome.R
import com.example.allhome.bill.BillsFragment
import com.example.allhome.bill.viewmodel.BillViewModel
import com.example.allhome.grocerylist.GroceryUtil
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue



@Entity(tableName = BillEntity.TABLE_NAME)
@Parcelize
data class BillEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name= COLUMN_GROUP_UNIQUE_ID) var groupUniqueId:String,
    @ColumnInfo(name= COLUMN_AMOUNT) var amount:Double,
    @ColumnInfo(name= COLUMN_NAME) var name:String,
    @ColumnInfo(name= COLUMN_CATEGORY) var category:String,
    @ColumnInfo(name= COLUMN_DUE_DATE) var dueDate:String,
    @ColumnInfo(name= COLUMN_IS_RECURRING) var isRecurring:Int,
    @ColumnInfo(name= COLUMN_REPEAT_EVERY) var repeatEvery:Int,
    @ColumnInfo(name= COLUMN_REPEAT_BY) var repeatBy:String,
    @ColumnInfo(name= COLUMN_REPEAT_UNTIL) var repeatUntil:String,
    @ColumnInfo(name= COLUMN_REPEAT_COUNT) var repeatCount:Int,
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
        const val NOT_RECURRING = 0
        const val RECURRING = 1

        const val TABLE_NAME ="bills"
        const val COLUMN_GROUP_UNIQUE_ID ="group_unique_id"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_AMOUNT ="amount"
        const val COLUMN_NAME ="name"
        const val COLUMN_CATEGORY ="category"
        const val COLUMN_DUE_DATE ="due_date"
        const val COLUMN_IS_RECURRING ="is_recurring"
        const val COLUMN_REPEAT_EVERY ="repeat_every"
        const val COLUMN_REPEAT_BY ="repeat_by"
        const val COLUMN_REPEAT_UNTIL ="repeat_until"
        const val COLUMN_REPEAT_COUNT ="repeat_count"
        const val COLUMN_IMAGE_NAME ="image_name"
        const val COLUMN_STATUS ="status"
        const val COLUMN_UPLOADED ="uploaded"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }



}
@Parcelize
data class BillEntityWithTotalPayment(
    @Embedded val billEntity: BillEntity,
    var totalPayment:Double = 0.0
):Parcelable {

}

@BindingAdapter("android:setDueDateWithNumberOfDays")
fun setDueDateWithNumberOfDays(textView: TextView, dueDateString:String){

    val currentDate = DateTime.parse(DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now()), DateTimeFormat.forPattern("yyyy-MM-dd"))
    val dueDate = DateTime.parse(dueDateString, DateTimeFormat.forPattern("yyyy-MM-dd"))

    val days = Days.daysBetween(currentDate, dueDate).days
    val dueDateFormated = DateTimeFormat.forPattern("MMMM d, Y").print(dueDate)
    if(days <=0){
        textView.text = "Due date: ${dueDateFormated} (${days} day)"
    }else{
        textView.text = "Due date: ${dueDateFormated} (${days} day)"
    }
}
@BindingAdapter("android:setBillStatusIndicator")
fun setBillStatusIndicator(textView:TextView,billEntityWithTotalPayment: BillEntityWithTotalPayment){

    val currentDate = DateTime.parse(DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now()), DateTimeFormat.forPattern("yyyy-MM-dd"))
    val dueDate = DateTime.parse(billEntityWithTotalPayment.billEntity.dueDate, DateTimeFormat.forPattern("yyyy-MM-dd"))
    val days = Days.daysBetween(currentDate, dueDate).days
    val isOverdue = days < 0

    val dueAmount = billEntityWithTotalPayment.billEntity.amount
    val totalPayment = billEntityWithTotalPayment.totalPayment

    if(dueAmount > totalPayment && totalPayment > 0){

        if(isOverdue){
            textView.visibility = View.VISIBLE
            textView.setTextColor(Color.WHITE)
            textView.background = ContextCompat.getDrawable(textView.context,R.drawable.overdue_bill_status_bg)
            textView.setText("OVERDUE")
        }else{
            textView.visibility = View.VISIBLE
            textView.setTextColor(Color.BLACK)
            textView.background = ContextCompat.getDrawable(textView.context,R.drawable.partial_bill_status_bg)
            textView.setText("PARTIAL")
        }

        return;
    }

    if(totalPayment <=0 ){
        if(isOverdue){
            textView.visibility = View.VISIBLE
            textView.setTextColor(Color.WHITE)
            textView.background = ContextCompat.getDrawable(textView.context,R.drawable.overdue_bill_status_bg)
            textView.setText("OVERDUE")
        }else{

            textView.visibility = View.GONE
        }
        return;
    }

    if(totalPayment >= dueAmount){
        textView.visibility = View.VISIBLE
        textView.setTextColor(Color.BLACK)
        textView.background = ContextCompat.getDrawable(textView.context,R.drawable.paid_bill_status_bg)
        textView.setText("PAID")
        return
    }

}


@BindingAdapter("android:setBillDateViewing")
fun setBillDateViewing(textView: TextView, mBillViewModel: BillViewModel){

    //if(mBillViewModel.mVIEWING == BillsFragment.MONTH_VIEWING){
        val startingDateString = SimpleDateFormat("MMM d, yyyy").format(mBillViewModel.mStartingCalendar.time)
        val endingDateString = SimpleDateFormat("MMM d, yyyy").format(mBillViewModel.mEndingCalendar.time)
        textView.setText("${startingDateString} - ${endingDateString}")
    /*}else if(mBillViewModel.mVIEWING == BillsFragment.WEEK_VIEWING){

        val startingDateString = SimpleDateFormat("MMM dd, yyyy").format(mBillViewModel.mStartingCalendar.time)
        val endingDateString = SimpleDateFormat("MMM dd, yyyy").format(mBillViewModel.mEndingCalendar.time)
        textView.setText("${startingDateString} - ${endingDateString}")
    }*/

}

