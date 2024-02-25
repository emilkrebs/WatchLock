package com.emilkrebs.watchlock.utils

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.DEVICE_POLICY_SERVICE
import com.emilkrebs.watchlock.services.WatchCommunicationService

fun lockPhoneDialog(context: Context, reason: String = "") {
    if (WatchCommunicationService(context).isAdminActive()) {
        authenticateBiometric(context,
            title = "This phone is about to be locked",
            description = "Authenticate to prevent your phone from being locked",
            subtitle = reason,
            onFailure = {
                lockPhone(context)
            })
    }
}

private fun lockPhone(context: Context) {
    (context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager).lockNow()
}