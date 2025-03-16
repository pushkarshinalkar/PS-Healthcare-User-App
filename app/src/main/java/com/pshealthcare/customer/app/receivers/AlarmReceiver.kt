package com.pshealthcare.customer.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pshealthcare.customer.app.utils.ReminderNotificationService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val service = ReminderNotificationService(context)
        service.showNotification()
    }
}
