package com.example.allhome.grocerylist

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import com.example.allhome.R
import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


object GroceryUtil {
    val FINAL_IMAGES_LOCATION = "item_images"
    val TEMPORARY_IMAGES_LOCATION = "temporary_images";
    val withCommaAndWithoutDecimalFormater = DecimalFormat("#,###")
    val withCommaAndWithDecimalFormater = DecimalFormat("#,###.00")
    val withoutCommaAndWithoutDecimalFormater = DecimalFormat("####")
    val withoutCommaAndWithDecimalFormater = DecimalFormat("####.00")

    fun quantityPriceAndTotalPerItemUtil(groceryItemEntity: GroceryItemEntity):String{

        if(groceryItemEntity.quantity <=0 ){

            return ""
        }

        if(groceryItemEntity.quantity > 0 && groceryItemEntity.unit.trim().length > 0 && groceryItemEntity.pricePerUnit > 0){

            return formatNumber(groceryItemEntity.quantity)+" "+groceryItemEntity.unit+" x "+formatNumber(groceryItemEntity.pricePerUnit)+" = ₱ "+formatNumber(groceryItemEntity.quantity * groceryItemEntity.pricePerUnit)
        }
        if(groceryItemEntity.quantity <= 0 && groceryItemEntity.unit.trim().length > 0 && groceryItemEntity.pricePerUnit > 0){
            return formatNumber(groceryItemEntity.quantity)+" "+groceryItemEntity.unit+" x "+formatNumber(groceryItemEntity.pricePerUnit)+" = ₱ "+formatNumber(groceryItemEntity.quantity * groceryItemEntity.pricePerUnit)
        }

        if(groceryItemEntity.quantity > 0 && groceryItemEntity.unit.trim().length > 0 && groceryItemEntity.pricePerUnit <= 0){
            return formatNumber(groceryItemEntity.quantity)+" "+groceryItemEntity.unit
        }

        if(groceryItemEntity.quantity > 0 && groceryItemEntity.unit.trim().length <= 0 && groceryItemEntity.pricePerUnit <= 0){

            return formatNumber(groceryItemEntity.quantity)
        }


        return "test";

    }

    fun displayItemSubInformation(groceryItemEntity: GroceryItemEntity):Boolean{

        return groceryItemEntity.quantity > 0
    }

    fun formatNumber(anyNumber: Double) : String{

        return if(anyNumber % 1 == 0.0 ) withCommaAndWithoutDecimalFormater.format(anyNumber) else withCommaAndWithDecimalFormater.format(anyNumber)

    }
    fun formatNumberToStringForEditing(anyNumber: Double):String{
        if(anyNumber <=0){
            return ""
        }
        return if(anyNumber % 1 == 0.0 ) withoutCommaAndWithoutDecimalFormater.format(anyNumber) else withoutCommaAndWithDecimalFormater.format(anyNumber)
    }
    fun isBought(groceryItemEntity: GroceryItemEntity):Boolean{

        if(groceryItemEntity.bought == 1){
            return true
        }

        return false
    }
    fun margin(groceryItemEntity: GroceryItemEntity):String{
        if(groceryItemEntity.bought == 1){
            return "0dp"
        }

        return "10dp"

    }
    fun totalItemsToBuyCount(totalItemCount: Int):String{

        return "TO EXPENSE ("+totalItemCount+")"
    }
    fun totalItemsBoughtCount(totalItemCount: Int):String{

        return "IN CART ("+totalItemCount+")"
    }
    fun formatNumberToStringForMoneyWithCommanAndDecimal(moneySign: String, anyNumber: Double):String{
        if(anyNumber == 0.0){
            return moneySign+" 0"
        }
       return moneySign+" "+withCommaAndWithDecimalFormater.format(anyNumber)
    }

    fun concatTotalItemAndTotalBoughtItem(totalItem: Int, totalBoughtItem: Int):String{
        return totalBoughtItem.toString()+"/"+totalItem.toString()
    }
    fun formatGroceryScheduledDate(stringDate: String?):String {
        if(stringDate == null || stringDate.trim().length <=0 || stringDate.trim().equals("0000-00-00 00:00:00") ){
            return ""
        }
        val splitedStringDateTime = stringDate.split(" ")
        if(splitedStringDateTime[1].equals("00:00:00")){
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd h:mm:ss")
            val date: Date? = simpleDateFormat.parse(stringDate)
            return SimpleDateFormat("MMMM d, Y").format(date)
        }else{
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd h:mm:ss")
            val date: Date? = simpleDateFormat.parse(stringDate)
            return SimpleDateFormat("MMMM d, Y h:mm a").format(date)
        }
    }

    fun formatGroceryScheduledDateForGroceryListViewing(stringDate: String?):String?{

        if(stringDate == null || stringDate.trim().length <=0 || stringDate.trim().equals("0000-00-00 00:00:00") ){
            return ""
        }

        val splitedStringDateTime = stringDate.split(" ")

        val currentDateTime = DateTime()
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val currentDateString =  dateTimeFormatter.print(currentDateTime)

        if(splitedStringDateTime[1].equals("00:00:00")){

            val scheduleDateTime = DateTime.parse(splitedStringDateTime[0], DateTimeFormat.forPattern("yyyy-MM-dd"))
            val currentDate = DateTime.parse(currentDateString, DateTimeFormat.forPattern("yyyy-MM-dd"))
            val days = Days.daysBetween(currentDate, scheduleDateTime).days

            if(days == 0){
                return "Today"

            }else if(days == 1){
                return "Tommorow"
            }else if(days in 2..6){
                return  DateTimeFormat.forPattern("EEEE").print(DateTime().plusDays(days))

            }else{
                return  DateTimeFormat.forPattern("MMMM d, Y").print(scheduleDateTime)
            }


        }else{

            val scheduleDateTime = DateTime.parse(splitedStringDateTime[0], DateTimeFormat.forPattern("yyyy-MM-dd"))
            val currentDate = DateTime.parse(currentDateString, DateTimeFormat.forPattern("yyyy-MM-dd"))
            val days = Days.daysBetween(currentDate, scheduleDateTime).days
            val hourString = DateTimeFormat.forPattern("h:mm a").print(DateTime.parse(splitedStringDateTime[1], DateTimeFormat.forPattern("HH:mm:ss")))
            if(days == 0){
                return "Today "+hourString

            }else if(days == 1){
                return "Tommorow "+hourString
            }else if(days in 2..6){
                return  DateTimeFormat.forPattern("EEEE").print(DateTime().plusDays(days))+" "+hourString

            }else{
                return  DateTimeFormat.forPattern("MMMM d, Y").print(scheduleDateTime)+" "+hourString
            }

        }

        return null
    }

    fun doGroceryListHasLocation(groceryListEntity: GroceryListEntity):Boolean{
        if(groceryListEntity.location.trim().length <=0){
            return false
        }
        return true
    }
    fun doGroceryListHasScheduleDate(groceryListEntity: GroceryListEntity):Boolean{
        if(groceryListEntity.shoppingDatetime.trim().length <=0 || groceryListEntity.shoppingDatetime.trim().equals("0000-00-00 00:00:00")){
            return false
        }
        return true
    }

    fun doGroceryListHasAlarm(context:Context,groceryListEntity: GroceryListEntity):Boolean{
        if(groceryListEntity.shoppingDatetime.trim().length <=0 || groceryListEntity.shoppingDatetime.trim().equals("0000-00-00 00:00:00")){
            return false
        }
        if(groceryListEntity.notifyType == context.resources.getString(R.string.grocery_notification_none)){
            return false
        }
        return true
    }


    fun doImageFromPathExists(context: Context, imageName:String): Boolean {

        val storageDir: File =  context.getExternalFilesDir(FINAL_IMAGES_LOCATION)!!
        if(!storageDir.exists()){
            return false
        }
        val imageFile  = File(storageDir, imageName)

        if(imageFile.exists() && imageFile.isFile){

            return true
        }

        return false
    }

    fun renameImageFile(context: Context, previousImageName:String, newImageName:String) {

        val storageDir: File =  context.getExternalFilesDir(FINAL_IMAGES_LOCATION)!!
        val imageFile  = File(storageDir, previousImageName)
        imageFile.renameTo(File(storageDir, newImageName))

    }

    fun deleteImageFile(context: Context, imageName:String) {

        val storageDir: File =  context.getExternalFilesDir(FINAL_IMAGES_LOCATION)!!
        val imageFile  = File(storageDir, imageName)
        imageFile.delete()

    }

    fun getImageFromPath(context: Context, imageName:String): Uri? {

        val storageDir: File =  context.getExternalFilesDir(FINAL_IMAGES_LOCATION)!!
        if(!storageDir.exists()){
            return null
        }
        val imageFile  = File(storageDir, imageName)

        if(imageFile.exists() && imageFile.isFile){
            return Uri.fromFile(imageFile)
        }

        return null
    }

    fun notify(groceryListEntity: GroceryListEntity?):String{

        if(groceryListEntity == null){
            return ""
        }
        if(groceryListEntity!!.notify <=0){
            return ""
        }
        return groceryListEntity.notify.toString()
    }






}