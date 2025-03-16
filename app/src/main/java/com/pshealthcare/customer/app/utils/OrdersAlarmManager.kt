package com.pshealthcare.customer.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.pshealthcare.customer.app.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OrdersAlarmManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(date: String) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val targetDate = dateFormat.parse(date)

        val calendar = Calendar.getInstance()
        targetDate?.let {
            calendar.time = it
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        val triggerTime = calendar.timeInMillis

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(context, "Please allow exact alarms in system settings", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(context, intent, null)
                    return
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e("OrdersAlarmManager", "Unable to schedule exact alarm", e)
            Toast.makeText(context, "Unable to schedule exact alarm", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
