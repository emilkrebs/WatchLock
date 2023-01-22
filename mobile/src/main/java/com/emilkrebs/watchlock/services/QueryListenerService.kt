package com.emilkrebs.watchlock.services


import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService


class QueryListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (String(messageEvent.data)) {
            "lock_status" -> {
                if ((this.getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked) {
                    sendMessage(this, "/phone/response", "phone_locked")
                } else {
                    sendMessage(this, "/phone/response", "phone_unlocked")
                }
            }
        }
    }
}

private fun sendMessage(context: Context, path: String, message: String) {
    Thread {
        run {
            getNodes(context).forEach {
                val messageApiClient = Wearable.getMessageClient(context)
                Tasks.await(messageApiClient.sendMessage(it, path, message.toByteArray()))
            }

        }
    }.start()
}

private fun getNodes(context: Context): Collection<String> {
    return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
}
