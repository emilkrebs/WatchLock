package com.emilkrebs.watchlock.presentation.services

import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class PhoneListenerService : WearableListenerService(){

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val intent = Intent()
        val path = messageEvent.path
        intent.action = ACTION_SEND
        intent.putExtra("data", messageEvent.data)
        intent.putExtra("path", path)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        if (path == PhoneCommunicationServiceDefaults.PING_PATH) {
            return handlePing(String(messageEvent.data))
        } else if (path == PhoneCommunicationServiceDefaults.COMMAND_PATH) {
            return handleCommand(String(messageEvent.data))
        }
        else if (path == PhoneCommunicationServiceDefaults.SCREENSHOT_PATH) {
            // received a screenshot
            return handleScreenshot(String(messageEvent.data))
        }
    }

//    override fun onDataChanged(dataEvents: DataEventBuffer) {
//        super.onDataChanged(dataEvents)
//
//        dataEvents.forEach { event ->
//            // DataItem changed
//            if (event.type == DataEvent.TYPE_CHANGED) {
//                event.dataItem.also { item ->
//                    if (item.uri.path?.compareTo("/lock_password") == 0) {
//                        DataMapItem.fromDataItem(item).dataMap.apply {
//                            val password =
//                                this.getString("com.emilkrebs.key.lock_password")
//                            println(password)
//
//                        }
//                    }
//                }
//            } else if (event.type == DataEvent.TYPE_DELETED) {
//                // DataItem deleted
//            }
//        }
//
//    }
//

    private fun handleScreenshot(data: String) {
        if (data == "screenshot") {
            Log.d("PhoneListenerService", "Received screenshot")
        }
    }

    private fun handleCommand(data: String) {
        if (data == "not_active") {
            Toast.makeText(this, "Request blocked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePing(message: String) {
        if (message == "ping") {
            return PhoneCommunicationService(this).sendMessage(
                Message.fromString(
                    PhoneCommunicationServiceDefaults.PING_PATH,
                    "pong"
                )
            )
        }
    }
}
