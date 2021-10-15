package com.example.allhome.utils

import java.text.SimpleDateFormat

object DateUtil {

    fun formatDateString(date: String,format:String) : String{

        return SimpleDateFormat(format).format(SimpleDateFormat("yyyy-MM-dd").parse(date))

    }
}
