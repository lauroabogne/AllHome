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
@Entity(tableName = "grocery_items")
 data class GroceryItemEntity constructor (
    @ColumnInfo(name = "grocery_list_unique_id") var groceryListUniqueId:String,
    @ColumnInfo(name = "sequence") val sequence:Int = 0,
    @ColumnInfo(name = "item_name") var itemName:String = "",
    @ColumnInfo(name = "quantity") var quantity:Double = 0.0,
    @ColumnInfo(name = "unit") val unit:String = "",
    @ColumnInfo(name = "price_per_unit") val pricePerUnit:Double= 0.0,
    @ColumnInfo(name="category") var category:String = "",
    @ColumnInfo(name = "notes") val notes:String = "",
    @ColumnInfo(name = "image_name") var imageName:String = "",
    @ColumnInfo(name = "bought",defaultValue = "0") var bought:Int = 0,
    @ColumnInfo(name = "item_status",defaultValue = "0") var itemStatus:Int = 0,
    @ColumnInfo(name = "datetime_created",defaultValue = "CURRENT_TIMESTAMP") var datetimeCreated:String = "",
    @ColumnInfo(name = "datetime_modified",defaultValue = "CURRENT_TIMESTAMP") var datetimeModified:String = ""
     ):Parcelable{
    @PrimaryKey(autoGenerate = true) var id:Int  = 0
    @Ignore
    var index:Int = 0
    @Ignore
    var forCategoryDivider = false


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

   if(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri != null && groceryListViewModel?.selectedGroceryItemEntityNewImageUri !=null){
      (view as ImageView).setImageURI(groceryListViewModel?.selectedGroceryItemEntityNewImageUri)
   }


    if(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri == null && groceryListViewModel?.selectedGroceryItemEntityNewImageUri != null){
       (view as ImageView).setImageURI(groceryListViewModel?.selectedGroceryItemEntityNewImageUri)
        return
    }
    if(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri != null && groceryListViewModel?.selectedGroceryItemEntityNewImageUri == null){
        (view as ImageView).setImageURI(groceryListViewModel?.selectedGroceryItemEntityCurrentImageUri)
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