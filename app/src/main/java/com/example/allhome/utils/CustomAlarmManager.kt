package com.example.allhome.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.allhome.NotificationReceiver
import com.example.allhome.R
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object CustomAlarmManager {

    fun createAlarm(context:Context,requestCode:Int , intent:Intent, alarmDateTime:Long){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDateTime, pendingIntent)
    }

     fun createAlarmForTodo(context:Context, notifyValue: Int, notifyType: String, dueDateTimeFormatted: String, todoUniqueId: String,todoEntityId:Int, todoName : String){
        val notificationDatetime  = generatedAlarmDatetimeForTodo(context, notifyValue, notifyType,dueDateTimeFormatted)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        intent.apply {
            action = NotificationReceiver.TODO_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            intent.putExtra(NotificationReceiver.TODO_UNIQUE_ID, todoUniqueId)
            intent.putExtra(NotificationReceiver.TODO_NAME, todoName)

        }

        val pendingIntent = createPendingIntent(context, intent,todoEntityId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }else{

            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }

    }

     fun generatedAlarmDatetimeForTodo(context:Context, notifyValue: Int, notifyType: String, dueDateTimeFormatted: String):Long{

        if(dueDateTimeFormatted.isEmpty() || dueDateTimeFormatted == "0000-00-00 00:00:00"){

            return 0
        }
        val formatter: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val datetime: DateTime = formatter.parseDateTime(dueDateTimeFormatted)

        when(notifyType){
            context.resources.getString(R.string.grocery_notification_none) -> {
                return 0
            }
            context.resources.getString(R.string.grocery_notification_same_day_and_time) -> {

                return datetime.millis
            }
            context.resources.getString(R.string.grocery_notification_minute_before) -> {

                return datetime.minusMinutes(notifyValue).millis
            }
            context.resources.getString(R.string.grocery_notification_hour_before) -> {

                return datetime.minusHours(notifyValue).millis
            }
            context.resources.getString(R.string.grocery_notification_day_before) -> {

                return datetime.minusDays(notifyValue).millis
            }
        }
        return 0
    }
     fun createPendingIntent(context:Context, intent: Intent, uniqueRequestCode:Int): PendingIntent {

        return PendingIntent.getBroadcast(context, uniqueRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

     fun isAlarmActive(context:Context, requestCode:Int, intent:Intent): Boolean {

        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent != null
    }

     fun cancelAlarm(context:Context, uniqueRequestCode: Int, intent:Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context, intent, uniqueRequestCode)

        alarmManager.cancel(pendingIntent)
    }

//    private fun cancelTodoAlarm(context:Context, uniqueRequestCode: Int) {
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, NotificationReceiver::class.java)
//        intent.action = NotificationReceiver.TODO_NOTIFICATION_ACTION
//        intent.putExtra(NotificationReceiver.TODO_UNIQUE_ID, todoEntityId.toString()) // Use the same identifier used when setting the alarm
//        val pendingIntent = createPendingIntent(context, intent, uniqueRequestCode)
//
//        alarmManager.cancel(pendingIntent)
//    }

}