package com.example.allhome

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.allhome.R

class SyncNotificationProgress(private val context: Context) {

    private val channelId = "SYNC_CHANNEL_ID"
    private val notificationId = 1
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val customNotificationLayout: RemoteViews = RemoteViews(context.packageName, R.layout.sync_notification_progress_layout)

    init {
        createNotificationChannel()
        initNotificationBuilder()
    }

    // Create a notification channel (required for Android O and higher)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sync Progress"
            val descriptionText = "Displays the sync progress"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initNotificationBuilder() {
        notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_close_icon) // Add your app's sync icon here
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customNotificationLayout)
            .setOnlyAlertOnce(true)
            .setOngoing(true) // Keep the notification active until the sync completes
    }

    @SuppressLint("MissingPermission")
    fun showOverallProgressNotification(overallProgress: Int, maxOverall: Int) {
        // Set text and progress for the overall sync progress
        customNotificationLayout.setTextViewText(
            R.id.overall_progress_text, "Overall progress: $overallProgress/$maxOverall"
        )
        customNotificationLayout.setProgressBar(
            R.id.progress_bar_overall, maxOverall, overallProgress, false
        )

        Log.e("Progress", "Overall progress: $overallProgress/$maxOverall")

        // Notify the system to update the notification
        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    fun showDetailedProgressNotification(message: String, detailedProgress: Int, maxDetailed: Int) {
        // Set text and progress for the detailed sync progress
        customNotificationLayout.setTextViewText(
            R.id.detailed_progress_text, "$message : $detailedProgress/$maxDetailed"
        )
        customNotificationLayout.setProgressBar(
            R.id.progress_bar_detailed, maxDetailed, detailedProgress, false
        )

        Log.e("Progress", "$message : $detailedProgress/$maxDetailed")

        // Notify the system to update the notification
        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    fun showDetailedProgressMessageNotification(message: String) {
        // Set the message for the detailed sync progress
        customNotificationLayout.setTextViewText(R.id.detailed_progress_text, message)

        // Notify the system to update the notification
        NotificationManagerCompat.from(context).notify(notificationId, notificationBuilder.build())
    }

    // Hide notification once sync is complete
    @SuppressLint("MissingPermission")
    fun completeSync() {
        val builder = NotificationCompat.Builder(context, channelId)
           .setSmallIcon(R.drawable.check) // Add your complete icon here
            .setContentTitle("Sync Complete")
            .setContentText("Your data has been successfully synced")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            // Issue the notification
            notify(notificationId, builder.build())
        }
    }

    // Cancel notification
    fun cancelNotification() {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }
}
