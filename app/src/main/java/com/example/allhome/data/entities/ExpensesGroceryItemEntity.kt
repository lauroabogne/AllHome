package com.example.allhome.data.entities

import android.graphics.Color
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.room.*
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "expenses_grocery_items")
 data class ExpensesGroceryItemEntity constructor (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= "unique_id") var uniqueId:String  = "",
    @ColumnInfo(name = "expenses_grocery_list_unique_id") var expensesGroceryListUniqueId:String,
    @ColumnInfo(name = "grocery_list_unique_id") var groceryListUniqueId:String,
    @ColumnInfo(name = "sequence") val sequence:Int = 0,
    @ColumnInfo(name = "item_name") var itemName:String = "",
    @ColumnInfo(name = "quantity") var quantity:Double = 0.0,
    @ColumnInfo(name = "unit") var unit:String = "",
    @ColumnInfo(name = "price_per_unit") var pricePerUnit:Double= 0.0,
    @ColumnInfo(name="category") var category:String = "",
    @ColumnInfo(name = "notes") var notes:String = "",
    @ColumnInfo(name = "image_name") var imageName:String = "",
    @ColumnInfo(name = "bought",defaultValue = "0") var bought:Int = 0,
    @ColumnInfo(name = "item_status",defaultValue = "0") var itemStatus:Int = 0,
    @ColumnInfo(name = "datetime_created",defaultValue = "CURRENT_TIMESTAMP") var datetimeCreated:String = "",
    @ColumnInfo(name = "datetime_modified",defaultValue = "CURRENT_TIMESTAMP") var datetimeModified:String = ""
     ):Parcelable{
    @Ignore
    var index:Int = 0
    @Ignore
    var forCategoryDivider = false


}
