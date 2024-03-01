package com.emilkrebs.watchlock.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.emilkrebs.watchlock.preferences
import com.emilkrebs.watchlock.workers.CheckWatchNearbyWorker
import java.util.concurrent.TimeUnit


class DistanceCheckerService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!preferences.isLockNotNearbyEnabled()) {
            stopSelf()
        } else {
            val checkWatchNearbyRequest = PeriodicWorkRequest.Builder(
                CheckWatchNearbyWorker::class.java,
                preferences.getLockNotNearbyInterval().toLong(),
                TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(applicationContext).enqueue(checkWatchNearbyRequest)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}

fun startDistanceCheckerService(context: Context) {
    val serviceIntent = Intent(context, DistanceCheckerService::class.java)
    context.startService(serviceIntent)
}

fun stopDistanceCheckerService(context: Context) {
    val serviceIntent = Intent(context, DistanceCheckerService::class.java)
    context.stopService(serviceIntent)
}