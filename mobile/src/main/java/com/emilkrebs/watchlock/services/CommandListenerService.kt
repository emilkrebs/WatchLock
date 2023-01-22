package com.emilkrebs.watchlock.services


import android.app.admin.DevicePolicyManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class CommandListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val message = String(messageEvent.data)
        // TODO: Get the broadcast receiver to work
        var devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        when (message) {
            "lock_phone" -> devicePolicyManager.lockNow()
        }
    }
}
