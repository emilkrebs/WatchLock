package com.emilkrebs.watchlock.services

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra("data", messageEvent.data)
        intent.putExtra("path", messageEvent.path)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)


        val watchCommunicationService = WatchCommunicationService(this)
        watchCommunicationService.onMessageReceived = { message ->
            val testVar = message === WatchCommunicationServiceDefaults.REQUEST_LOCK_PHONE
            if (message === WatchCommunicationServiceDefaults.REQUEST_LOCK_PHONE) {
                var devicePolicyManager =
                    getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
                devicePolicyManager.lockNow()
                watchCommunicationService.sendMessage(WatchCommunicationServiceDefaults.RESPONSE_PHONE_LOCKED)
            } else if (message === WatchCommunicationServiceDefaults.REQUEST_LOCK_STATUS) {
                if ((this.getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked) {
                    watchCommunicationService.sendMessage(WatchCommunicationServiceDefaults.RESPONSE_PHONE_LOCKED)
                } else {
                    watchCommunicationService.sendMessage(WatchCommunicationServiceDefaults.RESPONSE_PHONE_UNLOCKED)
                }
            }
        }
    }

}
