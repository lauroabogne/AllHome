package com.example.allhome.data.entities

import android.net.Uri
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.*
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.storage.StorageUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = StorageEntity.TABLE_NAME)
data class StorageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = COLUMN_UNIQUE_ID) var uniqueId:String,
    @ColumnInfo(name = COLUMN_NAME) var name:String,
    @ColumnInfo(name = COLUMN_DESCRIPTION) var description:String,
    @ColumnInfo(name = COLUMN_IMAGE_NAME) var imageName:String,
    @ColumnInfo(name = COLUMN_ITEM_STATUS,defaultValue = "0") var itemStatus:Int,
    @ColumnInfo(name = COLUMN_CREATED,defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = COLUMN_MODIFIED,defaultValue = "CURRENT_TIMESTAMP") var modified:String

):Parcelable{
    companion object{
        const val TABLE_NAME = "storage"
        const val COLUMN_UNIQUE_ID ="unique_id"
        const val COLUMN_NAME ="name"
        const val COLUMN_DESCRIPTION ="description"
        const val COLUMN_IMAGE_NAME ="image_name"
        const val COLUMN_ITEM_STATUS ="item_status"
        const val COLUMN_CREATED ="created"
        const val COLUMN_MODIFIED ="modified"
    }
}
class StorageEntityValues{
    companion object{
        const val DELETED_STATUS = 1
        const val NOT_DELETED_STATUS = 0


    }
}
data class StorageEntityWithExtraInformation(
    @Embedded val storageEntity: StorageEntity,
    val itemCount:Int,
    val noStockItemCount:Int,
    val lowStockItemCount:Int,
    val highStockItemCount:Int,
    val expiredItemCount:Int,
    val itemToExpireDayCount:Int
)
@Parcelize
data class StorageEntityWithStorageItemInformation(
    @Embedded val storageEntity: StorageEntity,
    val storageItemName:String,
    val storageItemUnit:String,
    val storageItemStockWeight:Int,
    val storageItemQuantity:Int
):Parcelable

@BindingAdapter(value=["bind:previousImageUri","bind:currentImageUri"],requireAll = false)
fun setImageToImageViewForCreatingStorage(view: View, previousImageUri: Uri?, currentImageUri: Uri?){

    if(currentImageUri !=null){
        (view as ImageView).setImageURI(currentImageUri)
    }else if(currentImageUri ==null && previousImageUri !=null){
        (view as ImageView).setImageURI(previousImageUri)
    }
}

@BindingAdapter("android:setImageStorageImage")
fun setImageStorageImage(view: View,imageName:String){

    val uri:Uri? = StorageUtil.getImageUriFromPath(view.context,StorageUtil.STORAGE_IMAGES_FINAL_LOCATION,imageName)

    uri?.apply {
        (view as ImageView).setImageURI(uri)
        view.visibility = View.VISIBLE
    }?:run {
        view.visibility = View.GONE
    }

}

@BindingAdapter("android:setCollapseImageStorageImage")
fun setCollapseImageStorageImage(view: View,imageName:String){

    val uri:Uri? = StorageUtil.getImageUriFromPath(view.context,StorageUtil.STORAGE_IMAGES_FINAL_LOCATION,imageName)

    uri?.apply {
        (view as ImageView).setImageURI(uri)
        view.visibility = View.VISIBLE
    }?:run {
        view.visibility = View.GONE
    }

}

@BindingAdapter("android:setSoonToExpireItemText")
fun setSoonToExpireItemText(view: TextView, itemToExpireInDays:Int){
    if(itemToExpireInDays <=0 || itemToExpireInDays >31){
        view.visibility = View.GONE
        return
    }
    view.setText("Some item will expire within $itemToExpireInDays days")


}