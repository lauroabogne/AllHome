package com.example.allhome.grocerylist

import com.example.allhome.data.entities.GroceryItemEntity
import com.example.allhome.data.entities.GroceryListEntity
import java.text.DecimalFormat

object GroceryUtil {

    val withCommaAndWithoutDecimalFormater = DecimalFormat("#,###")
    val withCommaAndWithDecimalFormater = DecimalFormat("#,###.00")
    val withoutCommaAndWithoutDecimalFormater = DecimalFormat("####")
    val withoutCommaAndWithDecimalFormater = DecimalFormat("####.00")

    fun quantityPriceAndTotalPerItemUtil(groceryItemEntity: GroceryItemEntity):String{

        if(groceryItemEntity.quantity <=0 ){

            return ""
        }

        if(groceryItemEntity.quantity > 0 && groceryItemEntity.unit.trim().length > 0 && groceryItemEntity.pricePerUnit > 0){

            return formatNumber(groceryItemEntity.quantity)+" "+groceryItemEntity.unit+" x "+formatNumber(groceryItemEntity.pricePerUnit)+" = ₱ "+formatNumber(groceryItemEntity.quantity* groceryItemEntity.pricePerUnit )
        }
        if(groceryItemEntity.quantity <= 0 && groceryItemEntity.unit.trim().length > 0 && groceryItemEntity.pricePerUnit > 0){
            return formatNumber(groceryItemEntity.quantity)+" "+groceryItemEntity.unit+" x "+formatNumber(groceryItemEntity.pricePerUnit)+" = ₱ "+formatNumber(groceryItemEntity.quantity* groceryItemEntity.pricePerUnit)
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

    fun formatNumber(anyNumber:Double) : String{

        return if(anyNumber % 1 == 0.0 ) withCommaAndWithoutDecimalFormater.format(anyNumber) else withCommaAndWithDecimalFormater.format(anyNumber)

    }
    fun formatNumberToStringForEditing(anyNumber:Double):String{
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
    fun totalItemsToBuyCount(totalItemCount:Int):String{

        return "TO EXPENSE ("+totalItemCount+")"
    }
    fun totalItemsBoughtCount(totalItemCount:Int):String{

        return "IN CART ("+totalItemCount+")"
    }
    fun formatNumberToStringForMoneyWithCommanAndDecimal(moneySign:String,anyNumber:Double):String{
        if(anyNumber == 0.0){
            return moneySign+" 0"
        }
       return moneySign+" "+withCommaAndWithDecimalFormater.format(anyNumber)
    }

}