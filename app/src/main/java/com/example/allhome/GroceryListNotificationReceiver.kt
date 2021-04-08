package com.example.allhome

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.allhome.grocerylist.AddGroceryListItemActivity
import com.example.allhome.grocerylist.SingleGroceryListActivity

class GroceryListNotificationReceiver : BroadcastReceiver() {

    companion object{
        val NOTIFCATION_CHANNEL_ID = "GroceryList"
        val NOTIFICATION_NAME = "Grocery list notification"
        val NOTIFICATION_DESCRIPTION = "This is the notification for grocery list";
        val NOTIFICATION_REQUEST_CODE = 1234
        val GROCERY_LIST_UNIQUE_ID = "GROCERY_LIST_UNIQUE_ID"
        val GROCERY_NOTIFICATION_ACTION = "grocery_list_scheduled_notification";
    }
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        //TODO("GroceryListNotificationReceiver.onReceive() is not implemented")

        if(intent.action?.contentEquals(GROCERY_NOTIFICATION_ACTION) == true){

            val groceryListUniqueid  = intent.getStringExtra(GROCERY_LIST_UNIQUE_ID)
            val groceryIntent = Intent(context,MainActivity::class.java)
            groceryIntent.apply {
                //flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = GROCERY_NOTIFICATION_ACTION
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueid)
            }
            /*groceryIntent.putExtra(AddGroceryListItemActivity.GROCERY_LIST_UNIQUE_ID_EXTRA_DATA_TAG, groceryListUniqueid)
            groceryIntent.putExtra(SingleGroceryListActivity.LAUNCH_FROM_TAG,SingleGroceryListActivity.LAUNCH_FROM_NOTIFICATION)*/

            val pendingIntent = PendingIntent.getActivity(context.applicationContext,1,groceryIntent,0)

            createNotificationChannel(context)
            var builder = NotificationCompat.Builder(context!!, NOTIFCATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.check)
                .setContentTitle("This is your reminder")
                .setContentText("Hello... How are you?"+groceryListUniqueid)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)


            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(200,builder.build())
        }


    }


    private fun createNotificationChannel(context:Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFCATION_CHANNEL_ID, NOTIFICATION_NAME, importance).apply {
                description = NOTIFICATION_DESCRIPTION;
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}