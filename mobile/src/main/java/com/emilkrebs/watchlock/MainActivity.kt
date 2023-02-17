package com.emilkrebs.watchlock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable



class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            resultLauncher.launch(intent)
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {  result ->
            if (result.resultCode == Activity.RESULT_OK) {

            }
        }
}
public fun sendMessage(context: Context, path: String, message: String) {
    Thread {
        run {
            getNodes(context).forEach {
                val messageApiClient = Wearable.getMessageClient(context)
                Tasks.await(messageApiClient.sendMessage(it, path, message.toByteArray()))
            }

        }
    }.start()
}

public fun getNodes(context: Context): Collection<String> {
    return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
}