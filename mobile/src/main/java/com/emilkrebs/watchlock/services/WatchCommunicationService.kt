package com.emilkrebs.watchlock.services

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.emilkrebs.watchlock.R
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.tasks.await


class WatchCommunicationService(private val context: Context) {
    private var devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private var adminComponent: ComponentName = ComponentName(context, AdminReceiver::class.java)


    companion object {
        fun openPlayStoreOnWatch(context: Context) {
            getNodes(context) { nodes ->
                if (nodes.isNotEmpty()) {
                    val remoteActivityHelper =
                        RemoteActivityHelper(context, Dispatchers.IO.asExecutor())

                    remoteActivityHelper.startRemoteActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setData(
                                Uri.parse(context.getString(R.string.play_store_link))
                            )
                            .addCategory(Intent.CATEGORY_BROWSABLE),
                        nodes.first()
                    )

                }
            }
        }

        suspend fun isWatchConnected(context: Context): Boolean {
            return Wearable.getNodeClient(context).connectedNodes.await().isNotEmpty()
        }

        suspend fun isWearAppInstalled(context: Context, capabilityName: String): Boolean {
            val capabilityInfo = Wearable.getCapabilityClient(context)
                .getCapability(capabilityName, CapabilityClient.FILTER_REACHABLE)
                .await()
            return capabilityInfo.nodes.isNotEmpty()
        }

        fun getNodes(context: Context, onNodesReceived: (Collection<String>) -> Unit) {
            Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
                val nodeIds = nodes.map { it.id }
                onNodesReceived(nodeIds)
            }
        }

    }

    fun isAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    fun pingWatch(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        sendMessage(
            Message.fromString(
                WatchCommunicationServiceDefaults.PING_PATH,
                "ping"
            )
        ) { success, message ->
            onComplete(success, message)
        }
    }

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

class WatchCommunicationServiceDefaults {
    companion object {
        const val REQUEST_PATH = "/wearable/request/"
        const val COMMAND_PATH = "/wearable/command/"
        const val PING_PATH = "/wearable/ping/"

        const val LOCK_STATUS_PATH = "/wearable/lock_status/"
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