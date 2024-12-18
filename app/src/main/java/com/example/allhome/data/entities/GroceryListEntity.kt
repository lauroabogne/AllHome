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
@Entity(tableName = GroceryListEntity.TABLE_NAME)
data class GroceryListEntity(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = COLUMN_AUTO_GENERATED_UNIQUE_ID,index = true) var autoGeneratedUniqueId:String,
        @ColumnInfo(name = COLUMN_NAME) var name:String,
        @ColumnInfo(name = COLUMN_DATETIME_CREATED,defaultValue = "CURRENT_TIMESTAMP") var datetimeCreated:String,
        @ColumnInfo(name = COLUMN_SHOPPING_DATETIME) var shoppingDatetime:String,
        @ColumnInfo(name = COLUMN_LOCATION) var location:String,
        @ColumnInfo(name = COLUMN_LONGITUDE) var longitude:Double,
        @ColumnInfo(name = COLUMN_LATITUDE) var latitude:Double,
        @ColumnInfo(name = COLUMN_VIEWING_TYPE,defaultValue="0") var viewingType:Int,
        @ColumnInfo(name = COLUMN_NOTIFY,defaultValue="0") var notify:Int,
        @ColumnInfo(name = COLUMN_NOTIFY_TYPE,defaultValue="none") var notifyType:String,
        @ColumnInfo(name = COLUMN_ITEM_STATUS,defaultValue="0") var itemStatus:Int,//0 active,1=deleted,2=permanently deleted
        @ColumnInfo(name = COLUMN_DATETIME_STATUS_UPDATED,defaultValue = "CURRENT_TIMESTAMP") var datetimeStatusUpdated:String,
        @ColumnInfo(name = COLUMN_UPLOADED,defaultValue="0") var uploaded:Int //0=not yet uploaded,1=uploaded

):Parcelable{
    companion object{
        const val TABLE_NAME ="grocery_lists"
        const val COLUMN_AUTO_GENERATED_UNIQUE_ID ="auto_generated_unique_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DATETIME_CREATED = "datetime_created"
        const val COLUMN_SHOPPING_DATETIME = "shopping_datetime"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_VIEWING_TYPE = "viewing_type"
        const val COLUMN_NOTIFY = "notify"
        const val COLUMN_NOTIFY_TYPE = "notify_type"
        const val COLUMN_ITEM_STATUS = "item_status"
        const val COLUMN_DATETIME_STATUS_UPDATED = "datetime_status_updated"
        const val COLUMN_UPLOADED = "uploaded"

        const val UPLOADED = 1
        const val NOT_YET_UPLOADED = 0
        const val ACTIVE_STATUS = 0
        const val ARCHIVE = 1
        const val PERMANENTLY_DELETED_STATUS = 2
    }

}

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




