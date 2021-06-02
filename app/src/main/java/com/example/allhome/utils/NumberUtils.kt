package com.example.allhome.utils

import android.util.Log
import com.example.allhome.grocerylist.GroceryUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object NumberUtils {

    val withCommaAndWithoutDecimalFormater = DecimalFormat("#,###")
    val withCommaAndWithDecimalFormater = DecimalFormat("#,###.00")
    val withoutCommaAndWithoutDecimalFormater = DecimalFormat("####")
    val withoutCommaAndWithDecimalFormater = DecimalFormat("####.00")
    fun formatNumber(anyNumber: Double) : String{

        return if(anyNumber % 1 == 0.0 ) GroceryUtil.withCommaAndWithoutDecimalFormater.format(anyNumber) else GroceryUtil.withCommaAndWithDecimalFormater.format(anyNumber)

    }
        fun bigDecimalToString( bigDecimal: BigDecimal):String{
            if(bigDecimal == null){
                return "";
            }
            val isInteger = bigDecimal.signum() == 0 || bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0;

            if(isInteger && bigDecimal.signum() <= 0 ){
                // if bigDecimal is interger and  bigDecimal less than or equal 0
               return ""
            }else if(isInteger && bigDecimal.signum() > 0 ){
               // if bigDecimal is interger and  bigDecimal greater than 0
                return bigDecimal.toInt().toString()
            }

            return bigDecimal.setScale(2, RoundingMode.HALF_EVEN).toString()
         }

        fun test():String{
            return " test value"
        }

}