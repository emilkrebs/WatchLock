package com.emilkrebs.watchlock.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationServiceDefaults.Companion.LOCK_STATUS_PATH
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationServiceDefaults.Companion.REQUEST_PATH
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable


/**
 * A service that handles communication with the phone
 */
class PhoneCommunicationService(private val context: Context) {

    companion object {

        fun convertImageByteArrayToBitmap(imageData: ByteArray): Bitmap {
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        }

        fun isPhoneConnected(context: Context, onConnected: (Boolean) -> Unit) {
            getNodes(context) { nodes ->
                onConnected(nodes.isNotEmpty())
            }
        }

        fun getNodes(context: Context, onNodesReceived: (Collection<String>) -> Unit) {
            Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
                val nodeIds = nodes.map { it.id }
                onNodesReceived(nodeIds)
            }
        }

    }

    init {
        val broadcastReceiver = object : BroadcastReceiver() {
            // create a broadcast receiver to receive messages from the phone
            override fun onReceive(context: Context, intent: Intent) {
                val path = intent.getStringExtra("path")

                if (path == LOCK_STATUS_PATH) {
                    val data = intent.getByteArrayExtra("data")
                    val isLocked = data?.toString(Charsets.UTF_8)?.toBoolean() ?: false

                    onLockStatusReceived(LockStatus.fromBoolean(isLocked))
                }

                if (path == PhoneCommunicationServiceDefaults.SCREENSHOT_PATH) {
                    // received a screenshot
                    val data = intent.getByteArrayExtra("data")
                    onScreenshotReceived(data)
                }

            }
        }

        // register the broadcast receiver
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_SEND))
    }

    var onLockStatusReceived: ((LockStatus) -> Unit) = {}

    var onScreenshotReceived: ((ByteArray?) -> Unit) = {}

    fun requestLockPhone() {
        sendMessage(
            Message.fromString(
                PhoneCommunicationServiceDefaults.COMMAND_PATH,
                "lock_phone"
            )
        )
    }

    fun requestLockStatus() {
        sendMessage(Message.fromString(REQUEST_PATH, "lock_status"))
    }

    // Request a screenshot from the phone
    fun requestScreenshot() {
        Log.d("PhoneCommunicationService", "Requesting screenshot")
        sendMessage(Message.fromString(REQUEST_PATH, "screenshot"))
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

// Make sure the paths are exactly the same as in the phone app
class PhoneCommunicationServiceDefaults {
    companion object {
        const val REQUEST_PATH = "/wearable/request/"
        const val COMMAND_PATH = "/wearable/command/"
        const val PING_PATH = "/wearable/ping/"

        const val LOCK_STATUS_PATH = "/wearable/lock_status/"
        const val SCREENSHOT_PATH = "/wearable/screenshot/"
    }
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

    override fun toString(): String {
        return "Message(path='$path', data=${data.contentToString()})"
    }
}