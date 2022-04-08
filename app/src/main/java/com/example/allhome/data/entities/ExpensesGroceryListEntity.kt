package com.example.allhome.data.entities

import android.os.Parcelable
import android.view.View
import android.widget.Spinner
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allhome.R
import kotlinx.android.parcel.Parcelize
import java.io.Serializable



@Entity(tableName = "expenses_grocery_lists")
data class ExpensesGroceryListEntity(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "auto_generated_unique_id",index = true) var autoGeneratedUniqueId:String,
        @ColumnInfo(name = "grocery_list_auto_generated_unique_id") var groceryListAutoGeneratedUniqueId:String,
        @ColumnInfo(name = "name") var name:String,
        @ColumnInfo(name = "datetime_created",defaultValue = "CURRENT_TIMESTAMP") var datetimeCreated:String,
        @ColumnInfo(name = "shopping_datetime") var shoppingDatetime:String,
        @ColumnInfo(name = "location") var location:String,
        @ColumnInfo(name = "longitude") var longitude:Double,
        @ColumnInfo(name = "latitude") var latitude:Double,
        @ColumnInfo(name = "item_status",defaultValue="0") var itemStatus:Int,//0 active,1=deleted,2=permanently deleted
        @ColumnInfo(name = "datetime_status_updated",defaultValue = "CURRENT_TIMESTAMP") var datetimeStatusUpdated:String,
        @ColumnInfo(name = "uploaded",defaultValue="0") var uploaded:Int //0=not yet uploaded,1=uploaded
)