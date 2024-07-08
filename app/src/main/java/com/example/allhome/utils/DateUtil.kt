package com.example.allhome.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    fun formatDateString(date: String,format:String) : String{

        return SimpleDateFormat(format).format(SimpleDateFormat("yyyy-MM-dd").parse(date))

    }

    fun getCustomCalendar(): Calendar {
        val customCalendar = Calendar.getInstance()
        customCalendar.firstDayOfWeek = Calendar.SUNDAY
        return customCalendar.clone() as Calendar
    }
    fun getDayOfWeekName(calendar: Calendar): String {

        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
    }

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val cal = Calendar.getInstance()
        return dateFormat.format(cal.time)
    }
}
