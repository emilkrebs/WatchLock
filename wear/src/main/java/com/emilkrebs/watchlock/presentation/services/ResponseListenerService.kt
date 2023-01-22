package com.emilkrebs.watchlock.presentation.services

import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class ResponseListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val message = String(messageEvent.data)
        println(message)
        val intent = Intent()
        intent.action = ACTION_SEND
        intent.putExtra("status", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
