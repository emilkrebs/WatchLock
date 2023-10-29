package com.emilkrebs.watchlock.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable


/**
 * A service that handles communication with the phone
 */
class PhoneCommunicationService(private val context: Context) {

    companion object {
        fun getNodes(context: Context, onNodesReceived: (Collection<String>) -> Unit) {
            Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
                val nodeIds = nodes.map { it.id }
                onNodesReceived(nodeIds)
            }
        }

    }

    /**
     * Called when a message is received from the phone
     */
    var onMessageReceived: ((Message) -> Unit) = {}


    var onLockStatusReceived: ((LockStatus) -> Unit) = {}

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

                if (message.path == PhoneCommunicationServiceDefaults.RESPONSE_PATH) {
                    // return the lock status
                    val lockStatus =
                        LockStatus.fromBoolean(message.data.toString(Charsets.UTF_8) == "phone_locked")
                    onLockStatusReceived(lockStatus)
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
    fun requestLockStatus() {
        sendMessage(
            Message(
                PhoneCommunicationServiceDefaults.QUERY_PATH,
                "lock_status".toByteArray()
            )
        )
    }

    fun requestLockPhone() {
        sendMessage(
            Message(
                PhoneCommunicationServiceDefaults.COMMAND_PATH,
                "lock_phone".toByteArray()
            )
        )
//        ) {
//            val result = if (it.isEqualTo(
//                    Message.fromString(
//                        PhoneCommunicationServiceDefaults.RESPONSE_PATH,
//                        "rejected"
//                    )
//                )
//            ) {
//                RequestLockPhoneResult.REJECTED
//            } else if (it.isEqualTo(
//                    Message.fromString(
//                        PhoneCommunicationServiceDefaults.RESPONSE_PATH,
//                        "success"
//                    )
//                )
//            ) {
//                RequestLockPhoneResult.SUCCESS
//            } else {
//                RequestLockPhoneResult.FAILURE
//            }
//            response(result)
//        }
    }


    /**
     * Sends a message synchronously to the phone
     * @param message the message to send
     */
    fun sendMessage(
        message: Message,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val messageApiClient = Wearable.getMessageClient(context)
        getNodes(context) { nodes ->
            if (nodes.isNotEmpty()) {
                val tasks = mutableListOf<Task<Int>>()

                for (node in nodes) {
                    val sendMessageTask =
                        messageApiClient.sendMessage(node, message.path, message.data)
                            .addOnCompleteListener { task ->
                                onComplete(
                                    task.isSuccessful,
                                    if (!task.isSuccessful) task.exception?.message else null
                                )
                            }

                    tasks.add(sendMessageTask)
                }

                // Wait for all tasks to complete before returning.
                Tasks.whenAll(tasks).addOnCompleteListener {
                    // All tasks have completed.

                }
            } else {
                onComplete(false, "No connected nodes found.")
            }
        }
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
}

enum class LockStatus(val value: Int) {
    LOCKED(1),
    UNLOCKED(0),
    UNKNOWN(-1);

    companion object {

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