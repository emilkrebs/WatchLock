package com.emilkrebs.watchlock.services


import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class WatchService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/command") {
            val message = String(messageEvent.data)
            val messageIntent = Intent()
            messageIntent.action = Intent.ACTION_SEND
            messageIntent.putExtra("message", message)

            // TODO: Get the broadcast receiver to work
            var devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            when(message){
                "lock_phone" -> devicePolicyManager.lockNow()
//                "block_touch" -> devicePolicyManager.setKeyguardDisabled(adminComponent,true)
//                "unblock_touch" -> devicePolicyManager.setKeyguardDisabled(adminComponent,false)
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
        } else {
            super.onMessageReceived(messageEvent)
        }
    }
}
