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
@Entity(tableName = GroceryItemEntity.TABLE_NAME)
 data class GroceryItemEntity constructor (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name= COLUMN_UNIQUE_ID) var uniqueId:String  = "",
    @ColumnInfo(name = COLUMN_GROCERY_LIST_UNIQUE_ID) var groceryListUniqueId:String,
    @ColumnInfo(name = COLUMN_SEQUENCE) val sequence:Int = 0,
    @ColumnInfo(name = COLUMN_ITEM_NAME) var itemName:String = "",
    @ColumnInfo(name = COLUMN_QUANTITY) var quantity:Double = 0.0,
    @ColumnInfo(name = COLUMN_UNIT) var unit:String = "",
    @ColumnInfo(name = COLUMN_PRICE_PER_UNIT) var pricePerUnit:Double= 0.0,
    @ColumnInfo(name=COLUMN_CATEGORY) var category:String = "",
    @ColumnInfo(name = COLUMN_NOTES) var notes:String = "",
    @ColumnInfo(name = COLUMN_IMAGE_NAME) var imageName:String = "",
    @ColumnInfo(name = COLUMN_BOUGHT,defaultValue = "0") var bought:Int = 0,
    @ColumnInfo(name = COLUMN_ITEM_STATUS,defaultValue = "0") var itemStatus:Int = 0,
    @ColumnInfo(name = COLUMN_UPLOADED,defaultValue = "$NOT_YET_UPLOADED") var uploaded:Int = 0,
    @ColumnInfo(name = COLUMN_DATETIME_CREATED,defaultValue = "CURRENT_TIMESTAMP") var datetimeCreated:String = "",
    @ColumnInfo(name = COLUMN_DATETIME_MODIFIED,defaultValue = "CURRENT_TIMESTAMP") var datetimeModified:String = ""
     ):Parcelable{
        @Ignore
        var index:Int = 0
        @Ignore
        var forCategoryDivider = false

    companion object{
        const val TABLE_NAME ="grocery_items"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_GROCERY_LIST_UNIQUE_ID = "grocery_list_unique_id"
        const val COLUMN_SEQUENCE = "sequence"
        const val COLUMN_ITEM_NAME = "item_name"
        const val COLUMN_QUANTITY = "quantity"
        const val COLUMN_UNIT = "unit"
        const val COLUMN_PRICE_PER_UNIT = "price_per_unit"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_IMAGE_NAME = "image_name"
        const val COLUMN_BOUGHT = "bought"
        const val COLUMN_ITEM_STATUS = "item_status"
        const val COLUMN_DATETIME_CREATED = "datetime_created"
        const val COLUMN_DATETIME_MODIFIED = "datetime_modified"

        const val COLUMN_UPLOADED = "uploaded"

        const val UPLOADED = 1
        const val NOT_YET_UPLOADED = 0
        const val ACTIVE_STATUS = 0
    }

}
data class GroceryItemEntityForAutoSuggest(
    @Embedded val groceryItemEntity: GroceryItemEntity,
    var itemInListCount: Int = 0
)
class GroceryItemEntityValues{
    companion object{
        val ACTIVE_STATUS = 0
        val DELETED_STATUS = 1
    }
}

@BindingAdapter("android:productImage")
fun setImageToImageView(view: View, groceryListItemEntity: GroceryItemEntity?){

   if(groceryListItemEntity?.imageName == null)return
   val uri = GroceryUtil.getImageFromPath(view.context,groceryListItemEntity.imageName)
   uri?.apply {
      (view as ImageView).setImageURI(uri)
      view.visibility = View.VISIBLE
   }?:run {
      view.visibility = View.GONE
   }

}

@BindingAdapter("android:setImageToImageViewForAddingItem")
fun setImageToImageViewForAddingItem(view: View, groceryListViewModel: GroceryListViewModel?){

   if(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri != null && groceryListViewModel.selectedGroceryItemEntityNewImageUri !=null){
      (view as ImageView).setImageURI(groceryListViewModel.selectedGroceryItemEntityNewImageUri)
   }


    if(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri == null && groceryListViewModel?.selectedGroceryItemEntityNewImageUri != null){
       (view as ImageView).setImageURI(groceryListViewModel.selectedGroceryItemEntityNewImageUri)
        return
    }
    if(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri != null && groceryListViewModel.selectedGroceryItemEntityNewImageUri == null){
        (view as ImageView).setImageURI(groceryListViewModel.selectedGroceryItemEntityCurrentImageUri)
        return
    }


}
@BindingAdapter("android:setCardElevation")
fun setCardElevation(view: View,groceryItemEntity:GroceryItemEntity){


    if(groceryItemEntity.bought == 1 && !groceryItemEntity.forCategoryDivider){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            (view as CardView).cardElevation = 0F
        }
    }else{
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            (view as CardView).cardElevation = 10F
        }
    }


}