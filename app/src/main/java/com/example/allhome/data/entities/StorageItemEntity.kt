package com.example.allhome.data.entities

import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import com.example.allhome.R
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.view.marginLeft
import androidx.databinding.BindingAdapter
import androidx.room.*
import com.example.allhome.storage.StorageUtil
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import kotlinx.android.parcel.Parcelize
import java.text.DecimalFormat

@Parcelize
@Entity(tableName = StorageItemEntity.TABLE_NAME)
data class StorageItemEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name = COLUMN_STORAGE_UNIQUE_ID) var storageUniqueId:String,
    @ColumnInfo(name = COLUMN_NAME) var name:String,
    @ColumnInfo(name = COLUMN_QUANTITY) var quantity:Double,
    @ColumnInfo(name = COLUMN_UNIT) var unit:String,
    @ColumnInfo(name=COLUMN_STOCK_WEIGHT,defaultValue = StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT.toString()) var stockWeight:Int = StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT,
    @ColumnInfo(name=COLUMN_CATEGORY) var category:String,
    @ColumnInfo(name=COLUMN_STORAGE) var storage:String,
    @ColumnInfo(name = COLUMN_NOTES) var notes:String,
    @ColumnInfo(name = COLUMN_IMAGE_NAME) var imageName:String,
    @ColumnInfo(name = COLUMN_ITEM_STATUS,defaultValue = "0") var itemStatus:Int,
    @ColumnInfo(name = COLUMN_CREATED,defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = COLUMN_MODIFIED,defaultValue = "CURRENT_TIMESTAMP") var modified:String
):Parcelable{
    companion object{
        const val TABLE_NAME = "storage_items"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_STORAGE_UNIQUE_ID ="storage_unique_id"
        const val COLUMN_NAME ="name"
        const val COLUMN_QUANTITY ="quantity"
        const val COLUMN_UNIT ="unit"
        const val COLUMN_STOCK_WEIGHT ="stock_weight"
        const val COLUMN_CATEGORY ="category"
        const val COLUMN_STORAGE ="storage"
        const val COLUMN_NOTES ="notes"
        const val COLUMN_IMAGE_NAME ="image_name"
        const val COLUMN_ITEM_STATUS ="item_status"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"

    }
}
@Parcelize
data class StorageItemWithExpirations(
    var storageItemEntity: StorageItemEntity,
    var expirations:List<StorageItemExpirationEntity> = arrayListOf()
):Parcelable
@Parcelize
data class StorageItemWithExpirationsAndStorages(
    var storageItemEntity: StorageItemEntity,
    var expirations:List<StorageItemExpirationEntity> = arrayListOf(),
    var storages:List<StorageEntityWithStorageItemInformation> = arrayListOf(),
):Parcelable

class StorageItemEntityValues{
    companion object{
        const val NO_QUANTITY_INPUT = -1
        const val NO_STOCK_WEIGHT_INPUT = -1
        const val NO_STOCK = 0
        const val LOW_STOCK = 1
        const val HIGH_STOCK = 2
        const val EXPIRED = 3

        const val NO_STOCK_WEIGHT_INPUT_STRING = ""
        const val NO_STOCK_STRING = "No stock"
        const val LOW_STOCK_STRING = "Low"
        const val HIGH_STOCK_STRING = "High"
        const val DELETED_STATUS = 1
        const val NOT_DELETED_STATUS = 0


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
    val uri = StorageUtil.getStorageItemImageUriFromPath(view.context,imageName)
    uri?.apply {
        (view as ImageView).setImageURI(uri)
        view.visibility = View.VISIBLE
    }?:run {
        view.visibility = View.GONE
    }
}
@BindingAdapter(value=["android:setStockWeight"])
fun setStockWeight(view: TextView, storageItemEntity: StorageItemEntity){
    if(storageItemEntity.stockWeight <= StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT && storageItemEntity.quantity < 0 ){
        view.visibility = View.GONE
        return
    }

    view.visibility = View.VISIBLE

    if(storageItemEntity.stockWeight == StorageItemEntityValues.NO_STOCK ){

        view.setText("Stock Weight : "+StorageItemEntityValues.NO_STOCK_STRING)

    }else if(storageItemEntity.stockWeight == StorageItemEntityValues.LOW_STOCK){

        if(storageItemEntity.quantity <= StorageItemEntityValues.NO_QUANTITY_INPUT){
            view.setText("Stock Weight : "+StorageItemEntityValues.LOW_STOCK_STRING)
        }else{

            view.setText("Stock Weight : "+StorageItemEntityValues.LOW_STOCK_STRING+" ("+StorageUtil.displayQuantity(storageItemEntity.quantity)+" "+storageItemEntity.unit+")")
        }


    }else if(storageItemEntity.stockWeight == StorageItemEntityValues.HIGH_STOCK){


        if(storageItemEntity.quantity <= StorageItemEntityValues.NO_QUANTITY_INPUT){
            view.setText("Stock Weight : "+StorageItemEntityValues.HIGH_STOCK_STRING)
        }else{
            view.setText("Stock Weight : "+StorageItemEntityValues.HIGH_STOCK_STRING+" ("+StorageUtil.displayQuantity(storageItemEntity.quantity)+" "+storageItemEntity.unit+")")
        }
    }else if(storageItemEntity.stockWeight == StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT){

        view.setText("Stock : "+StorageUtil.displayQuantity(storageItemEntity.quantity)+" "+storageItemEntity.unit)

    }

}

@BindingAdapter(value=["android:setStocks"])
fun setStocks(view: TextView, storageItemEntity: StorageItemEntity){

    if(storageItemEntity.quantity <= StorageItemEntityValues.NO_QUANTITY_INPUT ){
        view.setText("Stock: ")
    }else if(storageItemEntity.quantity == 0.0){
        view.setText("Stock: 0 "+storageItemEntity.unit)
    }else{
        view.setText("Stock: "+ DecimalFormat("#,###").format(storageItemEntity.quantity)+" "+storageItemEntity.unit)
    }


}

@BindingAdapter(value=["android:addStorages"])
fun addStorages(flexboxLayout: FlexboxLayout,storageItemWithExpirationsAndStorages:StorageItemWithExpirationsAndStorages){
    flexboxLayout.removeAllViews()

    storageItemWithExpirationsAndStorages.storages.forEach {

        //val chip = LayoutInflater.from(flexboxLayout.context).inflate(R.layout.custom_chip_layout,null,false) as TextView
        val chip:Chip = LayoutInflater.from(flexboxLayout.context).inflate(R.layout.chip_layout,null,false) as Chip

        chip.setText(it.storageEntity.name+" ("+it.storageItemQuantity+" "+it.storageItemUnit+")")
        chip.setTag(it.storageEntity)
        flexboxLayout.addView(chip)
        val divider = TextView(flexboxLayout.context)
        divider.isEnabled = false
        divider.width = 20
        flexboxLayout.addView(divider)
    }


}

