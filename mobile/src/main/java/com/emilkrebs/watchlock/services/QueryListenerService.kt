package com.emilkrebs.watchlock.services


import android.app.KeyguardManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.emilkrebs.watchlock.sendMessage


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
