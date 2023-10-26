package com.emilkrebs.watchlock.presentation.services

import android.content.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * A service that handles communication with the phone
 */
class PhoneCommunicationService(private val context: Context) {

    /**
     * Called when a message is received from the phone
     */
    var onMessageReceived: ((Message) -> Unit) = {}

    /**
     * Called when a message fails to send to the phone
     */
    var onFailure: ((Exception) -> Unit) = { _ -> }

    init {
        val broadcastReceiver = object : BroadcastReceiver() {
            // create a broadcast receiver to receive messages from the phone
            override fun onReceive(context: Context, intent: Intent) {
                val extras = intent.extras
                val message = Message(extras?.getString("path")!!, extras.getByteArray("data")!!)

                // check if message is a ping
                if (message.isEqualTo(
                        Message.fromString(
                            PhoneCommunicationServiceDefaults.QUERY_PATH,
                            "ping"
                        )
                    )
                ) {
                    // send a pong
                    return sendMessage(
                        Message.fromString(
                            PhoneCommunicationServiceDefaults.RESPONSE_PATH,
                            "pong"
                        )
                    ).start()
                }

                onMessageReceived(message)

            }
        }

        // register the broadcast receiver
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SEND))
    }


    /**
     * Requests the lock status from the phone
     * @true the phone is locked
     * @false the phone is unlocked
     */
    fun getLockStatus(response: (LockStatus) -> Unit) {
        fetch(
            Message(
                PhoneCommunicationServiceDefaults.QUERY_PATH,
                "lock_status".toByteArray()
            )
        ) {
            if (it.path == PhoneCommunicationServiceDefaults.RESPONSE_PATH) {
                // return the lock status
                response(LockStatus.fromBoolean(it.data.toString(Charsets.UTF_8) == "phone_locked"))
            }
        }
    }

    fun requestLockPhone(response: (RequestLockPhoneResult) -> Unit) {
        fetch(
            Message(
                PhoneCommunicationServiceDefaults.COMMAND_PATH,
                "lock_phone".toByteArray()
            )
        ) {
            val result = if (it.isEqualTo(
                    Message.fromString(
                        PhoneCommunicationServiceDefaults.RESPONSE_PATH,
                        "rejected"
                    )
                )
            ) {
                RequestLockPhoneResult.REJECTED
            } else if (it.isEqualTo(
                    Message.fromString(
                        PhoneCommunicationServiceDefaults.RESPONSE_PATH,
                        "success"
                    )
                )
            ) {
                RequestLockPhoneResult.SUCCESS
            } else {
                RequestLockPhoneResult.FAILURE
            }
            response(result)
        }
    }

    fun ping(response: (Boolean) -> Unit) {
        fetch(
            Message(
                PhoneCommunicationServiceDefaults.QUERY_PATH,
                "ping".toByteArray()
            )
        ) {
            if (it.path == PhoneCommunicationServiceDefaults.RESPONSE_PATH) {
                // return the lock status
                response(it.data.toString(Charsets.UTF_8) == "pong")
            }
        }
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
                LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
                response(Message(extras?.getString("path")!!, extras.getByteArray("data")!!))
            }
        }
        // register the broadcast receiver
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(Intent.ACTION_SEND))

    }

    /**
     * Sends a message synchronously to the phone
     * @param message the message to send
     */
    private fun sendMessage(message: Message): Thread {
        return Thread {
            getNodes(context).forEach {
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

    private fun getNodes(context: Context): Collection<String> {
        return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
    }
}

class PhoneCommunicationServiceDefaults {
    companion object {
        const val QUERY_PATH = "/wearable/query/"
        const val COMMAND_PATH = "/wearable/command/"
        const val RESPONSE_PATH = "/phone/response"
    }
}

enum class RequestLockPhoneResult {
    SUCCESS,
    REJECTED,
    FAILURE,
    UNKNOWN
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