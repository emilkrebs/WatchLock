package com.emilkrebs.watchlock.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.emilkrebs.watchlock.preferences
import com.emilkrebs.watchlock.utils.lockPhoneDialog
import com.google.android.gms.wearable.Wearable

class CheckWatchNearbyWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        checkWatchNearby(applicationContext)
        return Result.success()
    }

    private fun checkWatchNearby(context: Context) {
        Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                if (!node.isNearby && preferences.isLockNotNearbyEnabled()) {
                    lockPhoneDialog(context, "Reason: Your watch is not nearby")
                }
            }

        }
    }
}