package com.example.allhome.utils

import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode

object NumberUtils {

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