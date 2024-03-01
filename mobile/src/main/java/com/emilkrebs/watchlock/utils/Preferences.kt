package com.emilkrebs.watchlock.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.emilkrebs.watchlock.R
import com.emilkrebs.watchlock.isPreview
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.emilkrebs.watchlock.utils.Constants.LOCK_NEARBY_INTERVAL_KEY
import com.emilkrebs.watchlock.utils.Constants.LOCK_NOT_NEARBY_KEY

object Constants {
    const val PREFERENCE_FILE_KEY = "com.emilkrebs.android.watchlock_preferences"
    const val ACTIVE_KEY = "is_active"

    const val LOCK_NOT_NEARBY_KEY = "lock_not_nearby_enabled"
    const val LOCK_NEARBY_INTERVAL_KEY = "lock_nearby_interval"
}


class Preferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

    /* init {
         sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
             if (key == LOCK_PASSWORD_KEY) {
                 lifecycleScope.launch {
                     updateDataLayer(getPasscode(), context)
                 }

             }
         }
   }

     private suspend fun updateDataLayer(lockPassword: String, context: Context) {
         try {

             val request = PutDataMapRequest.create("/lock_password").apply {
                 dataMap.putString("com.emilkrebs.key.lock_password", lockPassword)
             }.asPutDataRequest().setUrgent()

             val result = dataClient.putDataItem(request).await()
             Toast.makeText(context, "Settings synced with watch.", Toast.LENGTH_SHORT).show()
         } catch (e: Exception) {
             e.printStackTrace()
         }
     }
     */

    fun resetPreferences() {
        sharedPreferences
            .edit()
            .clear()
            .apply()
    }

    fun getLockNotNearbyInterval(): Int {
        return sharedPreferences
            .getInt(LOCK_NEARBY_INTERVAL_KEY, 15)
    }

    fun setLockNotNearbyInterval(interval: Int) {
        sharedPreferences
            .edit()
            .putInt(LOCK_NEARBY_INTERVAL_KEY, interval)
            .apply()
    }

    fun setLockNotNearbyEnabled(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(LOCK_NOT_NEARBY_KEY, enabled)
            .apply()
    }

    fun isLockNotNearbyEnabled(): Boolean {
        return sharedPreferences
            .getBoolean(LOCK_NOT_NEARBY_KEY, false)
    }

    fun isWatchLockEnabled(): Boolean {
        return sharedPreferences
            .getBoolean(Constants.ACTIVE_KEY, false)
    }

    fun setWatchLockEnabled(
        enabled: Boolean,
        context: Context,
        fragmentActivity: FragmentActivity,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        authenticateBiometric(
            context,
            fragmentActivity,
            title = when (enabled) {
                true -> context.getString(R.string.enable_watchlock)
                false -> context.getString(R.string.disable_watchlock)
            },
            subtitle = context.getString(R.string.biometric_subtitle),
            onSuccess = {
                sharedPreferences
                    .edit()
                    .putBoolean(Constants.ACTIVE_KEY, enabled)
                    .apply()
                onSuccess()
            },
            onFailure = {
                onFailure()
            }
        )
    }
}

fun isAdminActive(context: Context): Boolean {
    if(isPreview) return true
    return try {
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AdminReceiver::class.java)

        devicePolicyManager.isAdminActive(adminComponent)
    } catch (e: Exception) {
        Toast.makeText(
            context, context.getString(R.string.admin_status_error), Toast.LENGTH_SHORT
        ).show()
        false
    }
}

fun getAdminDialogIntent(context: Context): Intent {
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    val explanation = context.getString(R.string.admin_explanation)

    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, explanation)

    return intent
}

fun revokeAdminPermissions(context: Context) {
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)

    if (devicePolicyManager.isAdminActive(adminComponent)) {
        devicePolicyManager.removeActiveAdmin(adminComponent)
    }
}