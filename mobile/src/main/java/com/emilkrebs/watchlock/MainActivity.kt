package com.emilkrebs.watchlock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.emilkrebs.watchlock.services.PING_BROADCAST_ACTION
import com.emilkrebs.watchlock.services.WatchCommunicationService
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


enum class PingStatus {
    SUCCESS,
    PENDING,
    FAILED,
    NONE
}
class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private val mainScope = MainScope()

    private var pingStatus = PingStatus.NONE

    private val pingTimeoutRunnable = Runnable {
        if(pingStatus == PingStatus.PENDING) {
            pingStatus = PingStatus.FAILED
            checkPing()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get the device policy manager and the admin component
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        val pingButton = findViewById<TextView>(R.id.ping_button)
        // set the click listener for the activate button
        findViewById<MaterialButton>(R.id.activate_button).setOnClickListener {
            if (!devicePolicyManager.isAdminActive(adminComponent)) {
                showRequestAdminDialog()
            } else {
                val isActive =
                    getSharedPreferences(
                        getString(R.string.preferences_file_key),
                        MODE_PRIVATE
                    ).getBoolean(
                        "isActive",
                        false
                    )
                setWatchLockActive(!isActive)
                checkIsWatchLockActive()
            }
        }

        pingButton.setOnClickListener {
            mainScope.launch {
                pingStatus = try {
                    WatchCommunicationService(applicationContext).pingWatch()

                    // set the ping status to failed after 5 seconds
                    Handler(Looper.getMainLooper()).postDelayed(pingTimeoutRunnable, 8000)

                    PingStatus.PENDING
                } catch (e: Exception) {
                    PingStatus.FAILED
                }
                checkPing()
            }
        }
        val filter = IntentFilter("com.emilkrebs.watchlock.PING")
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(pingReceiver, filter)

        checkAllItems()
    }

    private val pingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PING_BROADCAST_ACTION) {
                // Handle the broadcast here
                val pingValue = intent.getBooleanExtra("ping", false)
                pingStatus = if (pingValue) {
                    PingStatus.SUCCESS
                } else {
                    PingStatus.FAILED
                }
                Handler(Looper.getMainLooper()).removeCallbacks(pingTimeoutRunnable)
                checkPing()
            }
        }
    }


    override fun onResume() {
        super.onResume()

        // check all items of the checklist
        checkAllItems()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(pingReceiver)
        mainScope.cancel() // Cancel the CoroutineScope to avoid leaks
    }

    private fun checkAllItems() {
        checkHasAdminPrivileges()
        checkIsWatchConnected()
        checkIsWatchLockActive()
    }

    private fun checkIsWatchConnected() {
        val watchText = findViewById<TextView>(R.id.watch_connected_text) ?: return

        mainScope.launch {
            WatchCommunicationService.isWatchConnected(applicationContext) {
                if (it) {
                    watchText.text = getString(R.string.watch_connected)
                    watchText.setTextColor(getColor(R.color.success))
                } else {
                    watchText.text = getString(R.string.no_watch_connected)
                    watchText.setTextColor(getColor(R.color.danger))
                }
            }

        }
    }

    private fun checkHasAdminPrivileges() {
        val adminText = findViewById<TextView>(R.id.admin_text)
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            adminText.text = getString(R.string.admin_inactive)
            adminText.setTextColor(getColor(R.color.danger))
            adminText.setShadowLayer(1f, 0f, 0f, getColor(R.color.black))

            adminText.setOnClickListener {
                showRequestAdminDialog()
            }
        } else {
            adminText.text = getString(R.string.admin_active)
            adminText.setTextColor(getColor(R.color.success))
        }
    }

    private fun checkIsWatchLockActive() {
        val watchLockText = findViewById<TextView>(R.id.watchlock_active_text)
        val activateButton = findViewById<MaterialButton>(R.id.activate_button)

        val isActive =
            getSharedPreferences(getString(R.string.preferences_file_key), MODE_PRIVATE).getBoolean(
                "isActive",
                false
            )

        if (isActive && devicePolicyManager.isAdminActive(adminComponent)) {
            watchLockText.text = getString(R.string.watchlock_active)
            watchLockText.setTextColor(getColor(R.color.success))

            activateButton.text = getString(R.string.deactivate_watchlock)
            activateButton.setBackgroundColor(getColor(R.color.purple_500))

        } else {
            watchLockText.text = getString(R.string.watchlock_inactive)
            watchLockText.setTextColor(getColor(R.color.danger))

            activateButton.text = getString(R.string.activate_watchlock)
            activateButton.setBackgroundColor(getColor(R.color.purple_200))
        }
    }

    private fun checkPing() {
        val pingButton = findViewById<MaterialButton>(R.id.ping_button) ?: return
        val pingText = findViewById<TextView>(R.id.ping_text) ?: return

        when(pingStatus) {
            PingStatus.SUCCESS -> {
                pingButton.text = getString(R.string.ping)
                pingButton.isEnabled = true
                pingButton.setBackgroundColor(getColor(R.color.success))

                pingText.visibility = TextView.VISIBLE
                pingText.text = getString(R.string.ping_success)
                pingText.setTextColor(getColor(R.color.success))
            }
            PingStatus.PENDING -> {
                pingButton.text = getString(R.string.ping_pending)
                pingButton.isEnabled = false
                pingButton.setBackgroundColor(getColor(R.color.warning))

                pingText.visibility = TextView.GONE
            }
            PingStatus.FAILED -> {
                pingButton.text = getString(R.string.ping)
                pingButton.isEnabled = true
                pingButton.setBackgroundColor(getColor(R.color.danger))

                pingText.visibility = TextView.VISIBLE
                pingText.text = getString(R.string.ping_failed)
                pingText.setTextColor(getColor(R.color.danger))
            }
            PingStatus.NONE -> {
                pingButton.text = getString(R.string.ping)
                pingButton.isEnabled = true
                pingButton.setBackgroundColor(getColor(R.color.purple_200))

                pingText.visibility = TextView.GONE
            }
        }
    }

    private fun setWatchLockActive(isActive: Boolean) {
        val editor = getSharedPreferences(
            getString(R.string.preferences_file_key),
            MODE_PRIVATE
        ).edit()
        editor.putBoolean("isActive", isActive).apply()
    }

    private fun showRequestAdminDialog() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        val explanation = getString(R.string.admin_explanation)
        println(explanation)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            explanation
        )
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val statusText = findViewById<TextView>(R.id.admin_text)
                statusText.text = getString(R.string.admin_active)
                statusText.setTextColor(getColor(R.color.success))
                setWatchLockActive(true)
            }

        }
}