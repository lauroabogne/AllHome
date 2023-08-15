package com.example.allhome

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.allhome.data.AllHomeDatabase
import com.example.allhome.data.DAO.LogsDAO
import com.example.allhome.data.DAO.TodosDAO
import com.example.allhome.data.entities.LogsEntity
import com.example.allhome.data.entities.TodoEntity
import com.example.allhome.grocerylist.AddGroceryListItemFragment
import com.example.allhome.todo.CreateEditTodoFragment
import com.example.allhome.todo.viewmodel.TodoFragmentViewModel
import com.example.allhome.todo.viewmodel.TodoFragmentViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    companion object{
        const val NOTIFCATION_CHANNEL_ID = "GroceryList"
        const val NOTIFICATION_NAME = "Grocery list notification"
        const val NOTIFICATION_DESCRIPTION = "This is the notification for grocery list"
        const val NOTIFICATION_REQUEST_CODE = 1234
        const val GROCERY_LIST_UNIQUE_ID = "GROCERY_LIST_UNIQUE_ID"
        const val TODO_UNIQUE_ID = "TODO_UNIQUE_ID"
        const val TODO_NAME = "TODO_NAME"
        const val GROCERY_NOTIFICATION_ACTION = "grocery_list_scheduled_notification"
        const val TODO_NOTIFICATION_ACTION = "todo_scheduled_notification"
        const val DAILY_NOTIFICATION_ACTION = "daily_notification"

    }

    override fun onReceive(context: Context, intent: Intent) {

        /**
         * @see MainActivity.onCreate
         * @see MainActivity.onNewIntent
         * See code at function onNewIntent(intent: Intent?) in MainActivity.kt
         */

        val intentAction = intent.action
        if(intentAction.contentEquals(GROCERY_NOTIFICATION_ACTION)){

            val groceryListUniqueid  = intent.getStringExtra(GROCERY_LIST_UNIQUE_ID)
            val groceryIntent = Intent(context,MainActivity::class.java)
            groceryIntent.apply {
                //flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = intentAction
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AddGroceryListItemFragment.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueid)
            }

            val pendingIntent = PendingIntent.getActivity(context.applicationContext, Calendar.getInstance().timeInMillis.toInt(),groceryIntent,0)

            createNotificationChannel(context)
            var builder = NotificationCompat.Builder(context, NOTIFCATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.check)
                .setContentTitle("This is your reminder")
                .setContentText("Hello... How are you?$groceryListUniqueid")
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1986,builder.build())

        }else if(intentAction.contentEquals(TODO_NOTIFICATION_ACTION)){

            val todoUniqueId  = intent.getStringExtra(TODO_UNIQUE_ID)
            val todoName  = intent.getStringExtra(TODO_NAME)
            val todoIntent = Intent(context,MainActivity::class.java)
            todoIntent.apply {
                //flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = intentAction
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(CreateEditTodoFragment.TODO_UNIQUE_ID_TAG,todoUniqueId)


            }
            /*groceryIntent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueid)
            groceryIntent.putExtra(SingleGroceryListActivity.LAUNCH_FROM_TAG,SingleGroceryListActivity.LAUNCH_FROM_NOTIFICATION)*/


            //val pendingIntent = PendingIntent.getActivity(context.applicationContext, LocalDateTime.now().millisOfDay,todoIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val pendingIntent = PendingIntent.getActivity(context.applicationContext, LocalDateTime.now().millisOfDay,todoIntent,PendingIntent.FLAG_UPDATE_CURRENT or  PendingIntent.FLAG_MUTABLE)

            createNotificationChannel(context)
            var builder = NotificationCompat.Builder(context, NOTIFCATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.check)
                .setContentTitle("Reminder Notification")
                .setContentText("$todoName")
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)



            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(LocalDateTime.now().millisOfDay,builder.build())
        }else if(intentAction.contentEquals(DAILY_NOTIFICATION_ACTION)){

            Log.e("DAILY_NOTIFICATION_ACTION","TRIGGERED")

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val currentDatetime: String = simpleDateFormat.format(Date())



           // val pendingIntent = PendingIntent.getActivity(context.applicationContext, LocalDateTime.now().millisOfDay,todoIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            createNotificationChannel(context)
            var builder = NotificationCompat.Builder(context, NOTIFCATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.check)
                .setContentTitle("Reminder Notification")
                .setContentText("Alarmed ${  SimpleDateFormat("MMM d, yyyy h:mm a").format(Date())}")
                .setAutoCancel(false)
                //.setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)



            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1161986,builder.build())


            var taskUniqueId = UUID.randomUUID().toString()
            val logEntity = LogsEntity(
                uniqueId = taskUniqueId,
                name = "Alarmed",
                description = "This is the description",
                status = LogsEntity.NOT_DELETED_STATUS,
                uploaded = LogsEntity.NOT_UPLOADED,
                created = currentDatetime,
                modified = currentDatetime

            )

            val database by lazy { AllHomeDatabase.getDatabase(context) }
            val logsDAO: LogsDAO = database.getLogsDAO()
            val todoDAO:TodosDAO = database.getTodosDAO()


            val coroutineScope = CoroutineScope(Dispatchers.Main)
            coroutineScope.launch {


                withContext(IO){


                    val sameDayAndTime = context.resources.getString(R.string.grocery_notification_same_day_and_time)
                    val minutesBefore = context.resources.getString(R.string.grocery_notification_minute_before)
                    val hourBefore = context.resources.getString(R.string.grocery_notification_hour_before)
                    val dayBefore = context.resources.getString(R.string.grocery_notification_day_before)

                    val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:00").format(Date())
                    val todos:List<TodoEntity> = todoDAO.getTodosNeedToCreateAlarm(sameDayAndTime,minutesBefore,hourBefore,dayBefore,currentDate)

                    todos.forEach {

                        createTodoAlarm(context, it.notifyAt, it.notifyEveryType, it.dueDate, it.uniqueId, it.id, it.name)
                    }

                    logsDAO.insert(logEntity)
                    setAlarm(context.applicationContext)

                }
            }
        }
    }

//    private fun createIntervalNotification(context:Context){
//        val alarmDateTimeMilli = DateTime.now().plusMinutes(3).millis
//        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(context, NotificationReceiver::class.java)
//
//        intent.apply {
//            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
//            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
//            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
//
//        }
//
//        val pendingIntent = PendingIntent.getBroadcast(context, NotificationReceiver.NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDateTimeMilli, pendingIntent)
//    }

    private fun setAlarm(context: Context) {


//        Log.e("Error_Here", "Alarmed")
//
//        val alarmDateTimeMilli = DateTime.now().plusSeconds(10).millis
//        val alarmManager1 = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent1 = Intent(context, NotificationReceiver::class.java)
//        intent1.apply {
//            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
//            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
//            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
//
//        }
//
//        val pendingIntent1 = PendingIntent.getBroadcast(context, NotificationReceiver.NOTIFICATION_REQUEST_CODE, intent1, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        alarmManager1.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmDateTimeMilli, pendingIntent1)
//
//        return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        intent.apply {
            action = NotificationReceiver.DAILY_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        }
        // Get the current time
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Set the alarm to trigger at 8 AM
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                // If the current time is already past 8 AM, schedule it for the next day
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,  calendar.timeInMillis, pendingIntent)


    }
    private fun createNotificationChannel(context:Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFCATION_CHANNEL_ID, NOTIFICATION_NAME, importance).apply {
                description = NOTIFICATION_DESCRIPTION
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun createTodoAlarm(context:Context, notifyValue: Int, notifyType: String, dueDateTimeFormatted: String, todoUniqueId: String, todoEntityId:Int, todoName : String){


        val notificationDatetime  = generatedAlarmDatetime(context, notifyValue, notifyType,dueDateTimeFormatted)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        intent.apply {
            action = TODO_NOTIFICATION_ACTION
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            intent.putExtra(TODO_UNIQUE_ID, todoUniqueId)
            intent.putExtra(TODO_NAME, todoName)

        }

        val pendingIntent = createPendingIntent(context, intent, todoEntityId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)

        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }else{

            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationDatetime, pendingIntent)
        }

    }

    private fun generatedAlarmDatetime(context: Context, notifyValue: Int, notifyType: String, dueDateTimeFormatted: String):Long{

        if(dueDateTimeFormatted.isEmpty() || dueDateTimeFormatted == "0000-00-00 00:00:00"){

            return 0
        }
        val formatter: org.joda.time.format.DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
        val datetime:DateTime = formatter.parseDateTime(dueDateTimeFormatted)

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
    private fun createPendingIntent(context:Context, intent: Intent,todoEntityId:Int): PendingIntent {

        return PendingIntent.getBroadcast(context, todoEntityId, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
}