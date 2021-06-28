package com.example.allhome.utils

import android.icu.number.IntegerWidth
import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object NumberUtils {

    val withCommaAndWithoutDecimalFormater = DecimalFormat("#,###")
    val withCommaAndWithDecimalFormater = DecimalFormat("#,###.00")
    val withoutCommaAndWithoutDecimalFormater = DecimalFormat("####")
    val withoutCommaAndWithDecimalFormater = DecimalFormat("####.00")
    fun formatNumber(anyNumber: Double) : String{

        return if(anyNumber % 1 == 0.0 ) withCommaAndWithoutDecimalFormater.format(anyNumber) else withCommaAndWithDecimalFormater.format(anyNumber)

    }
        fun bigDecimalToString(bigDecimal: BigDecimal):String{
            if(bigDecimal == null){
                return ""
            }
            val isInteger = bigDecimal.signum() == 0 || bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0

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

    fun fraction(numberDouble: Double):String {

        val splitedNumber = numberDouble.toString().split(".")
        val wholeNumber = Integer.parseInt(splitedNumber[0])
        val decimalNumber = Integer.parseInt(splitedNumber[1])

        if(wholeNumber ==0 && decimalNumber == 0){
            return ""
        }
        if(wholeNumber >0 && decimalNumber == 0){
            return wholeNumber.toString()
        }
        val multiplier = multiplier(2)
        val numerator = (("."+decimalNumber).toDouble() * multiplier).toInt()
        val denominator = 1 * multiplier
        val gcd = getGCD(numerator,denominator)
        val theFraction = "${numerator/gcd}/${denominator/gcd}"

        if(wholeNumber <= 0){
            return theFraction.trim()
        }else{
            return "${wholeNumber} ${theFraction}".trim()
        }

    }
    fun getGCD(numerator: Int, denominator: Int): Int {
        return if (denominator == 0) {
            numerator
        } else getGCD(denominator, numerator % denominator)
    }
    fun multiplier(numberCout:Int):Int{
        var multiplier = 0
        for(x in 1..numberCout){
            multiplier =  if(x == 1)  10 else   multiplier * 10
        }

        return multiplier

    }


}