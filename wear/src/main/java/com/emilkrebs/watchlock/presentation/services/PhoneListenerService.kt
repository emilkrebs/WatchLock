package com.emilkrebs.watchlock.presentation.services

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class PhoneListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val intent = Intent()
        val path = messageEvent.path
        intent.action = ACTION_SEND
        intent.putExtra("data", messageEvent.data)
        intent.putExtra("path", path)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        if(path == PhoneCommunicationServiceDefaults.PING_PATH) {
            return handlePing(String(messageEvent.data))
        }
        else if (path == PhoneCommunicationServiceDefaults.COMMAND_PATH) {
            return handleCommand(String(messageEvent.data))
        }
    }

    private fun handleCommand(data: String) {
        if(data == "not_active") {
            Toast.makeText(this, "Request blocked", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handlePing(message: String) {
        if(message == "ping") {
            return PhoneCommunicationService(this).sendMessage(
                Message.fromString(
                    PhoneCommunicationServiceDefaults.PING_PATH,
                    "pong"
                )
            )
        }

    }


}
