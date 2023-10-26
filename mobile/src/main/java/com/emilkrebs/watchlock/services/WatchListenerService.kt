package com.emilkrebs.watchlock.services

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.core.graphics.drawable.toIcon
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.R
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService



class WatchListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val message = Message.fromString(messageEvent.path, messageEvent.data.toString(Charsets.UTF_8))
        val watchCommunicationService = WatchCommunicationService(this)
        val isActive = getSharedPreferences(getString(R.string.preferences_file_key), MODE_PRIVATE).getBoolean("isActive", false)


        // if the message is a command to lock the phone
        if (message.isEqualTo(Message.fromString(WatchCommunicationServiceDefaults.COMMAND_PATH, "lock_phone"))) {
            // lock the phone
            if(isActive) {
                lockPhone()
                watchCommunicationService.fetch(Message(WatchCommunicationServiceDefaults.RESPONSE_PATH, "success".toByteArray())) { }

            }
            else {
                watchCommunicationService.fetch(Message(WatchCommunicationServiceDefaults.RESPONSE_PATH, "rejected".toByteArray())) { }
            }
        }

        // if the message is a query for the lock status
        else if (message.isEqualTo(Message.fromString(WatchCommunicationServiceDefaults.QUERY_PATH, "lock_status"))) {
            if ((this.getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked) {
                watchCommunicationService.fetch(Message(WatchCommunicationServiceDefaults.RESPONSE_PATH, "phone_locked".toByteArray()))  { }
            } else {
                watchCommunicationService.fetch(Message(WatchCommunicationServiceDefaults.RESPONSE_PATH, "phone_unlocked".toByteArray())) { }
            }
        }
    }



    private fun lockPhone() {
        (getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager).lockNow()
    }

}
