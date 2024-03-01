package com.emilkrebs.watchlock.utils

import android.content.Context
import com.emilkrebs.watchlock.services.WatchCommunicationService

fun lockPhone(context: Context) {
    if (WatchCommunicationService(context).isAdminActive()) {
        lockPhone(context)
    }
}

