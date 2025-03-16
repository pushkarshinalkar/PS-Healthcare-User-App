package com.pshealthcare.customer.app.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class OrdersAlarmService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("pshealthcare-debug", "alarm received")
        return super.onStartCommand(intent, flags, startId)

    }

}