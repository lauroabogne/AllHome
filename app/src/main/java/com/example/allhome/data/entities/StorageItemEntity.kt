package com.example.allhome.data.entities

import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import com.example.allhome.R
import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import androidx.room.*
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.storage.StorageUtil

@Entity(tableName = "storage_items")
data class StorageItemEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "quantity") var quantity:Double,
    @ColumnInfo(name = "unit") var unit:String,
    @ColumnInfo(name="stock_weight",defaultValue = StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT.toString()) var stockWeight:Int = StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT ,
    @ColumnInfo(name="category") var category:String,
    @ColumnInfo(name="storage") var storage:String,
    @ColumnInfo(name = "notes") var notes:String,
    @ColumnInfo(name = "image_name") var imageName:String,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String


)

data class StorageItemWithExpirations(
    var storageItemEntity: StorageItemEntity,
    var expirations:List<StorageItemExpirationEntity> = arrayListOf()
)

class StorageItemEntityValues{
    companion object{
        val NO_QUANTITY_INPUT = -1
        const val NO_STOCK_WEIGHT_INPUT = -1
        val NO_STOCK = 0
        val LOW_STOCK = 1
        val HIGH_STOCK = 2

        val NO_STOCK_WEIGHT_INPUT_STRING = ""
        val NO_STOCK_STRING = "No stock"
        val LOW_STOCK_STRING = "Low"
        val HIGH_STOCK_STRING = "High"
    }
}

@BindingAdapter("android:setStockWeight")
fun setStockWeight(view:RadioGroup,stockWeight:Int){

    when(stockWeight){

        StorageItemEntityValues.NO_STOCK->{
            view.findViewById<RadioButton>( R.id.pantryNoStockRadioButton).isChecked = true
        }
        StorageItemEntityValues.LOW_STOCK->{
            view.findViewById<RadioButton>(R.id.pantryLowStockRadioButton).isChecked = true
        }
        StorageItemEntityValues.HIGH_STOCK->{
            view.findViewById<RadioButton>( R.id.pantryHightStockRadioButton).isChecked = true
        }
    }
}
@BindingAdapter(value=["bind:previousImageUri","bind:currentImageUri"],requireAll = false)
fun setImageToImageViewForAddingStorageItem(view:View, previousImageUri: Uri?, currentImageUri:Uri?){

    if(currentImageUri !=null){
        (view as ImageView).setImageURI(currentImageUri)
    }else if(currentImageUri ==null && previousImageUri !=null){
        (view as ImageView).setImageURI(previousImageUri)
    }
}
@BindingAdapter(value=["android:setImageForViewingStorageItem"],requireAll = false)
fun setImageForViewingStorageItem(view:View,imageName:String){

    if(imageName.isEmpty()){
        view.visibility = View.GONE
        return
    }

    val uri = StorageUtil.getImageUriFromPath(view.context,imageName)
    uri?.apply {
        (view as ImageView).setImageURI(uri)
        view.visibility = View.VISIBLE
    }?:run {
        view.visibility = View.GONE
    }


}