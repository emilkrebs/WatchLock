package com.emilkrebs.watchlock.presentation.services

import android.content.Intent
import android.content.Intent.ACTION_SEND
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService


class PhoneListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val intent = Intent()
        intent.action = ACTION_SEND
        intent.putExtra("data", messageEvent.data)
        intent.putExtra("path", messageEvent.path)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
