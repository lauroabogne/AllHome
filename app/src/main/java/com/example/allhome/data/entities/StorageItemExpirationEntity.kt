package com.example.allhome.data.entities

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import kotlin.math.absoluteValue


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
)

class StorageItemExpirationEntityValues{
    companion object{

        const val DELETED_STATUS = 0
        const val NOT_DELETED_STATUS = 1


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
