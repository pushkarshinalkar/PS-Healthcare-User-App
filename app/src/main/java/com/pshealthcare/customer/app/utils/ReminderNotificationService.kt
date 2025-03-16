package com.pshealthcare.customer.app.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.activities.MainActivity

class ReminderNotificationService(

    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun showNotification(){

        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context,"Reminder_Channel_ID")
            .setSmallIcon(R.drawable.ic_main_icon)
            .setContentTitle("Lab tests reminder !")
            .setContentText("Our Executive will contact you tomorrow for necessary details")
            .setContentIntent(activityPendingIntent)
            .build()

        notificationManager.notify(1,notification)
    }
}