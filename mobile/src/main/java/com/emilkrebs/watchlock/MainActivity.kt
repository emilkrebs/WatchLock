package com.emilkrebs.watchlock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.receivers.AdminReceiver


class MainActivity : AppCompatActivity() {

    lateinit var devicePolicyManager: DevicePolicyManager;
    lateinit var adminComponent: ComponentName;

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

        val messageIntent = Intent()
        messageIntent.action = Intent.ACTION_SEND
        LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter())
       }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

        }
    }

    private var broadcastReceiver =  object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            if(extras != null){
                when(extras.getString("message")){
                    "lock_phone" -> devicePolicyManager.lockNow()
                    "block_touch" -> devicePolicyManager.setKeyguardDisabled(adminComponent,true)
                    "unblock_touch" -> devicePolicyManager.setKeyguardDisabled(adminComponent,false)
                }
            }
        }
    }
}