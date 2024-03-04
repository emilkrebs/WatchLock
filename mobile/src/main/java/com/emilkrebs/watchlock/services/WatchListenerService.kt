package com.emilkrebs.watchlock.services

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import com.emilkrebs.watchlock.utils.requestLockPhone
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.SupervisorJob

const val ACTION_PING_BROADCAST = "com.emilkrebs.watchlock.PING"


class WatchListenerService : WearableListenerService() {
    private val job = SupervisorJob()
    private lateinit var watchCommunicationService: WatchCommunicationService

    override fun onCreate() {
        super.onCreate()
        watchCommunicationService = WatchCommunicationService(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val message =
            Message.fromString(messageEvent.path, messageEvent.data.toString(Charsets.UTF_8))

        val data = String(message.data)
        when (message.path) {
            WatchCommunicationServiceDefaults.COMMAND_PATH -> handleCommand(data)
            WatchCommunicationServiceDefaults.REQUEST_PATH -> handleRequest(data)
            WatchCommunicationServiceDefaults.PING_PATH -> handlePing(data, this)
        }

    }

    private fun handleCommand(command: String) {
        if (command == "lock_phone") {
            requestLockPhone(this)
        }
    }

    private fun handleRequest(request: String) {
        if (request == "lock_status") {
            sendLockStatus(isPhoneLocked())
        }
    }

    private fun handlePing(message: String, context: Context) {
        if (message == "ping") {
            return watchCommunicationService.sendMessage(
                Message.fromString(
                    WatchCommunicationServiceDefaults.PING_PATH,
                    "pong"
                )
            )
        } else if (message == "pong") {
            // send broadcast to MainActivity
            val intent = Intent()
            intent.action = ACTION_PING_BROADCAST
            intent.putExtra("ping", true)

            context.sendBroadcast(intent)
        }
    }

    private fun sendLockStatus(isLocked: Boolean) {
        watchCommunicationService.sendMessage(
            Message.fromString(
                WatchCommunicationServiceDefaults.LOCK_STATUS_PATH,
                isLocked.toString()
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun isPhoneLocked(): Boolean {
        return (this.getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
    }

}
