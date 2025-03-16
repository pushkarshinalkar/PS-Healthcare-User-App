package com.pshealthcare.customer.app.utils

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel(){

        val channel = NotificationChannel(
            "Reminder_Channel_ID",
            "Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Used for showing Lab order reminder notifications"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}