package com.example.allhome.storage

import com.example.allhome.R
import com.example.allhome.data.entities.StorageItemEntity
import com.example.allhome.data.entities.StorageItemEntityValues
import com.example.allhome.grocerylist.GroceryUtil
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.DecimalFormat

object StorageUtil {
    val withoutCommaAndWithoutDecimalFormater = DecimalFormat("####")
    fun formatExpirationDate(expirationDateString:String):String{
        val expirationDate = DateTime.parse(expirationDateString, DateTimeFormat.forPattern("yyyy-MM-dd"))
        return  DateTimeFormat.forPattern("MMMM d, Y").print(expirationDate)
    }
    fun formatExpirationDateWithDayRemaining(expirationDateString:String):String{
        val expirationDate = DateTime.parse(expirationDateString, DateTimeFormat.forPattern("yyyy-MM-dd"))
        return  DateTimeFormat.forPattern("MMMM d, Y").print(expirationDate)
    }
    fun stockWeightIntegerIdToIntegerValue(checkedRadioBtnId:Int):Int{

        return when(checkedRadioBtnId){
            R.id.pantryNoStockRadioButton->{
                StorageItemEntityValues.NO_STOCK
            }
            R.id.pantryLowStockRadioButton->{
                StorageItemEntityValues.LOW_STOCK
            }
            R.id.pantryHightStockRadioButton->{
                StorageItemEntityValues.HIGH_STOCK
            }
            else->{
                StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT
            }
        }
    }

    fun stockWeightIntegerToStringValue(storageItemEntity:StorageItemEntity):String{
        return when(storageItemEntity.stockWeight){
            StorageItemEntityValues.NO_STOCK_WEIGHT_INPUT->{
                return ""
            }
            StorageItemEntityValues.NO_STOCK->{
                return "Stock Weight : "+StorageItemEntityValues.NO_STOCK_STRING
            }
            StorageItemEntityValues.LOW_STOCK->{
                return "Stock Weight : "+StorageItemEntityValues.LOW_STOCK_STRING
            }
            StorageItemEntityValues.HIGH_STOCK->{
                return "Stock Weight : "+StorageItemEntityValues.HIGH_STOCK_STRING
            }
            else->{
                return ""
            }
        }

    }

    fun displayQuantity(anyNumber: Double):String{
        if(anyNumber <=0){
            return ""
        }
        return if(anyNumber % 1 == 0.0 ) GroceryUtil.withoutCommaAndWithoutDecimalFormater.format(anyNumber) else GroceryUtil.withoutCommaAndWithDecimalFormater.format(anyNumber)
    }


}