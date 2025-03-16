package com.pshealthcare.customer.app.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.pshealthcare.customer.app.R
import com.pshealthcare.customer.app.utils.ReminderNotificationService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val service = ReminderNotificationService(context)
        service.showNotification()


    }
}
