package com.emilkrebs.watchlock.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchCommunicationService(private val context: Context) {

    companion object {
        suspend fun isWatchConnected(context: Context): Boolean = withContext(Dispatchers.IO) {
            getNodes(context).isNotEmpty()
        }

        private fun getNodes(context: Context): Collection<String> {
            return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
        }
    }

    init {
        val broadcastReceiver = object : BroadcastReceiver() {
            // create a broadcast receiver to receive messages from the phone
            override fun onReceive(context: Context, intent: Intent) {

                onMessageReceived(getMessageFromIntent(intent))
            }
        }

        // register the broadcast receiver
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SEND))
    }

    /**
     * Called when a message is received from the phone
     */
    var onMessageReceived: ((Message) -> Unit) = {}


    /**
     * Called when a message fails to send to the phone
     */
    var onFailure: ((Exception) -> Unit) = { _ -> }


    fun getMessageFromIntent(intent: Intent): Message {
        val extras = intent.extras
        return Message(extras?.getString("path")!!, extras.getByteArray("data")!!)
    }

    /**
     * Sends a message to the phone and waits for a response
     * @param message the message to send
     */
    fun fetch(message: Message, response: (Message) -> Unit) {
        sendMessage(message).start()
        // send the message to the phone
        val receiver = object : BroadcastReceiver() {
            // create a broadcast receiver to receive messages from the phone
            override fun onReceive(context: Context, intent: Intent) {
                val extras = intent.extras
                val responseMessage =
                    Message(extras?.getString("path")!!, extras.getByteArray("data")!!)
                response(responseMessage)
            }
        }
        // register the broadcast receiver
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(Intent.ACTION_SEND))
    }

    fun ping(response: (Boolean) -> Unit) {
        fetch(
            Message(
                WatchCommunicationServiceDefaults.QUERY_PATH,
                "ping".toByteArray()
            )
        ) {
            if (it.path == WatchCommunicationServiceDefaults.RESPONSE_PATH) {
                // return the lock status
                response(it.data.toString(Charsets.UTF_8) == "pong")
            }
        }
    }

    /**
     * Sends a message synchronously to the phone
     * @param message the message to send
     */
    fun sendMessage(message: Message): Thread {
        return Thread {
            getNodes(context).forEach { it ->
                val messageApiClient = Wearable.getMessageClient(context)
                val sendMessageTask = messageApiClient.sendMessage(
                    it,
                    message.path,
                    message.data
                )
                sendMessageTask.addOnFailureListener { e ->
                    onFailure(e)
                    return@addOnFailureListener
                }
                sendMessageTask.addOnCompleteListener {
                    // return ok
                    return@addOnCompleteListener
                }
            }
        }
    }

}

class WatchCommunicationServiceDefaults {
    companion object {
        const val QUERY_PATH = "/wearable/query/"
        const val COMMAND_PATH = "/wearable/command/"
        const val RESPONSE_PATH = "/phone/response"
    }
}

enum class LockStatus(val value: Int) {
    LOCKED(1),
    UNLOCKED(0),
    UNKNOWN(-1);

    companion object {
        fun fromInt(value: Int): LockStatus {
            return when (value) {
                1 -> LOCKED
                0 -> UNLOCKED
                -1 -> UNKNOWN
                else -> UNKNOWN
            }
        }

        fun fromBoolean(bool: Boolean): LockStatus {
            return when (bool) {
                true -> LOCKED
                false -> UNLOCKED
            }
        }
    }

}

/**
 * A message object
 * @param path the path of the message
 * @param data the data of the message as a byte array
 */
class Message(path: String, data: ByteArray) {
    val path: String
    val data: ByteArray

    companion object {
        fun fromString(path: String, data: String): Message {
            return Message(path, data.toByteArray(Charsets.UTF_8))
        }
    }

    init {
        this.path = path
        this.data = data
    }

    fun isEqualTo(message: Message): Boolean {
        return this.path == message.path && this.data.contentEquals(message.data)
    }

    override fun toString(): String {
        return "Message(path='$path', data=${data.contentToString()})"
    }
}