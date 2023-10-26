package com.emilkrebs.watchlock

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.emilkrebs.watchlock.services.WatchCommunicationService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private val mainScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get the device policy manager and the admin component
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        // set the click listener for the activate button
        findViewById<TextView>(R.id.activate_button).setOnClickListener {
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

        checkAllItems()
    }

    override fun onResume() {
        super.onResume()

        // check all items of the checklist
        checkAllItems()
    }


    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel() // Cancel the CoroutineScope to avoid leaks
    }

    private fun checkAllItems() {
        checkHasAdminPrivileges()
        checkIsWatchConnected()
        checkIsWatchLockActive()
    }

    private fun checkIsWatchConnected() {
        val watchText = findViewById<TextView>(R.id.watch_text)
        mainScope.launch {
            val isConnected = WatchCommunicationService.isWatchConnected(applicationContext)

            if (isConnected) {
                watchText.text = getString(R.string.watch_connected)
                watchText.setTextColor(getColor(R.color.success))
            } else {
                watchText.text = getString(R.string.no_watch_connected)
                watchText.setTextColor(getColor(R.color.danger))
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
        val watchLockText = findViewById<TextView>(R.id.watchlock_text)
        val activateButton = findViewById<TextView>(R.id.activate_button)

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

    private fun setWatchLockActive(isActive: Boolean) {
        val editor = getSharedPreferences(
            getString(R.string.preferences_file_key),
            MODE_PRIVATE
        ).edit()
        editor.putBoolean("isActive", isActive).apply()
    }

    private fun showRequestAdminDialog() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "WatchLock needs admin rights to lock the screen and to run in the background."
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