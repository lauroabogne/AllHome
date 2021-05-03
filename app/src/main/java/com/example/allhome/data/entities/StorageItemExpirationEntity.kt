package com.example.allhome.data.entities

import android.graphics.Color
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allhome.databinding.PantrySimpleExpirationLayoutBinding
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import kotlin.math.absoluteValue


@Parcelize
@Entity(tableName = "storage_item_expirations")
data class StorageItemExpirationEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "storage_item_unique_id") var storageItemUniqueId:String,
    @ColumnInfo(name = "item_name") var storageItemName:String,
    @ColumnInfo(name = "storage") var storage:String,
    @ColumnInfo(name = "deleted",defaultValue = "0") var deleted:Int,
    @ColumnInfo(name = "expiration_date") var expirationDate:String,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String
):Parcelable

class StorageItemExpirationEntityValues{
    companion object{

        const val DELETED_STATUS = 0
        const val NOT_DELETED_STATUS = 1


    }
}

@BindingAdapter("android:appendExpirationDates")
fun appendExpirationDates(linearLayout: LinearLayout, expirations:List<StorageItemExpirationEntity>){

    linearLayout.removeAllViews()
    val layoutInflater = LayoutInflater.from(linearLayout.context)
    expirations.forEach {
        val pantrySimpleExpirationLayoutBinding = PantrySimpleExpirationLayoutBinding.inflate(layoutInflater,linearLayout,true)
        pantrySimpleExpirationLayoutBinding.pantryItemExpirationEntity = it
        pantrySimpleExpirationLayoutBinding.executePendingBindings()
    }

}
@BindingAdapter("android:setExpirationTextWithNumberOfDays")
fun setExpirationTextWithNumberOfDays(textView: TextView, expirationDateString:String){

    val currentDate = DateTime.parse(DateTimeFormat.forPattern("yyyy-MM-dd").print(DateTime.now()), DateTimeFormat.forPattern("yyyy-MM-dd"))
    val expirationDate = DateTime.parse(expirationDateString, DateTimeFormat.forPattern("yyyy-MM-dd"))

    val days = Days.daysBetween(currentDate, expirationDate).days
    val expirationDateFormated = DateTimeFormat.forPattern("MMMM d, Y").print(expirationDate)
    if(days <=0){
        textView.setText("${expirationDateFormated} (${days.absoluteValue} day)")
        textView.setTextColor(Color.RED)
    }else{
        textView.setText("${expirationDateFormated} (${days.absoluteValue} day)")
    }
}


