package com.example.allhome.data.entities

import android.net.Uri
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.allhome.grocerylist.GroceryUtil
import com.example.allhome.storage.StorageUtil
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "storage")
data class StorageEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "unique_id") var uniqueId:String,
    @ColumnInfo(name = "name") var name:String,
    @ColumnInfo(name = "description") var description:String,
    @ColumnInfo(name = "image_name") var imageName:String,
    @ColumnInfo(name = "item_status",defaultValue = "0") var itemStatus:Int,
    @ColumnInfo(name = "created",defaultValue = "CURRENT_TIMESTAMP") var created:String,
    @ColumnInfo(name = "modified",defaultValue = "CURRENT_TIMESTAMP") var modified:String

):Parcelable
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

@BindingAdapter("android:setSoonToExpireItemText")
fun setSoonToExpireItemText(view: TextView, itemToExpireInDays:Int){
    if(itemToExpireInDays <=0 || itemToExpireInDays >31){
        view.visibility = View.GONE
        return
    }
    view.setText("Some item will expire within $itemToExpireInDays days")


}