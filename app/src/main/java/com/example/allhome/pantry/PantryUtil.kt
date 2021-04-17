package com.example.allhome.pantry

import com.example.allhome.R
import com.example.allhome.data.entities.PantryItemEntity
import com.example.allhome.data.entities.PantryItemEntityValues
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object PantryUtil {

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
                PantryItemEntityValues.NO_STOCK
            }
            R.id.pantryLowStockRadioButton->{
                PantryItemEntityValues.LOW_STOCK
            }
            R.id.pantryHightStockRadioButton->{
                PantryItemEntityValues.HIGH_STOCK
            }
            else->{
                PantryItemEntityValues.NO_STOCK_WEIGHT_INPUT
            }
        }
    }

    fun stockWeightIntegerToStringValue(pantryItemEntity:PantryItemEntity):String{
        return when(pantryItemEntity.stockWeight){
            PantryItemEntityValues.NO_STOCK_WEIGHT_INPUT->{
                return ""
            }
            PantryItemEntityValues.NO_STOCK->{
                return "Stock Weight : "+PantryItemEntityValues.NO_STOCK_STRING
            }
            PantryItemEntityValues.LOW_STOCK->{
                return "Stock Weight : "+PantryItemEntityValues.LOW_STOCK_STRING
            }
            PantryItemEntityValues.HIGH_STOCK->{
                return "Stock Weight : "+PantryItemEntityValues.HIGH_STOCK_STRING
            }
            else->{
                return ""
            }
        }

    }


}