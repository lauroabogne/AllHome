package com.example.allhome.data.entities

import android.os.Parcelable
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allhome.R
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "grocery_lists")
data class GroceryListEntity(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "auto_generated_unique_id",index = true) var autoGeneratedUniqueId:String,
        @ColumnInfo(name = "name") var name:String,
        @ColumnInfo(name = "datetime_created",defaultValue = "CURRENT_TIMESTAMP") var datetimeCreated:String,
        @ColumnInfo(name = "shopping_datetime") var shoppingDatetime:String,
        @ColumnInfo(name = "location") var location:String,
        @ColumnInfo(name = "longitude") var longitude:Double,
        @ColumnInfo(name = "latitude") var latitude:Double,
        @ColumnInfo(name = "viewing_type",defaultValue="0") var viewingType:Int,
        @ColumnInfo(name = "notify",defaultValue="0") var notify:Int,
        @ColumnInfo(name = "notify_type",defaultValue="none") var notifyType:String,
        @ColumnInfo(name = "item_status",defaultValue="0") var itemStatus:Int,//0 active,1=deleted,2=permanently deleted
        @ColumnInfo(name = "datetime_status_updated",defaultValue = "CURRENT_TIMESTAMP") var datetimeStatusUpdated:String,
        @ColumnInfo(name = "uploaded",defaultValue="0") var uploaded:Int //0=not yet uploaded,1=uploaded

):Parcelable

@Parcelize
data class GroceryListWithItemCount(
    @Embedded val groceryListEntity: GroceryListEntity,
    val itemCount:Int,
    val itemBought:Int
):Parcelable

class GroceryListEntityValues{
    companion object{
        const val UPLOADED = 1
        const val NOT_YET_UPLOADED = 0
        const val ACTIVE_STATUS = 0
        const val ARCHIVE = 1
        const val PERMANENTLY_DELETED_STATUS = 2
    }
}

@BindingAdapter("android:setSelectedNotificationType")
fun setSelectedNotificationType(view: Spinner, groceryListEntity:GroceryListEntity?){

    if(groceryListEntity == null){
        return
    }
    val index =  view.context.resources.getStringArray(R.array.grocery_alarm_options).indexOf(groceryListEntity.notifyType)
    view.setSelection(index)

}




