package com.emilkrebs.watchlock.utils

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Context.DEVICE_POLICY_SERVICE
import com.emilkrebs.watchlock.services.WatchCommunicationService

fun requestLockPhone(context: Context): Boolean {
    if (WatchCommunicationService(context).isAdminActive() && Preferences(context).isWatchLockEnabled()) {
        val devicePolicyManager = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        devicePolicyManager.lockNow()
        return true
    }
    return false
}

