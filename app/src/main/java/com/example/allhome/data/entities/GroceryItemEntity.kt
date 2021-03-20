package com.example.allhome.data.entities

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.grocerylist.viewmodel.GroceryListViewModel

@Entity(tableName = "grocery_items")
 data class GroceryItemEntity (
    @ColumnInfo(name = "grocery_list_unique_id") val groceryListUniqueId:String,
    @ColumnInfo(name = "sequence") val sequence:Int = 0,
    @ColumnInfo(name = "item_name") var itemName:String,
    @ColumnInfo(name = "quantity") var quantity:Double,
    @ColumnInfo(name = "unit") val unit:String,
    @ColumnInfo(name = "price_per_unit") val pricePerUnit:Double,
    @ColumnInfo(name="category") val category:String,
    @ColumnInfo(name = "notes") val notes:String,
    @ColumnInfo(name = "image_name") var imageName:String,
    @ColumnInfo(name = "bought",defaultValue = "0") var bought:Int

     ){
    @PrimaryKey(autoGenerate = true) var id:Int  = 0
    @Ignore
    var index:Int = 0
    @Ignore
    var forCategoryDivider = false


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

    view.setBackgroundColor(Color.RED)

}