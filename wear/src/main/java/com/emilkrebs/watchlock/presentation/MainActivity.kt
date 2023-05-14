package com.emilkrebs.watchlock.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tiles.TileService
import com.emilkrebs.watchlock.presentation.services.LockStatus
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationService
import com.emilkrebs.watchlock.presentation.theme.WatchLockTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clickableId = intent.getStringExtra(TileService.EXTRA_CLICKABLE_ID)
        if (clickableId == "lock_phone") {
            setContent {
                WearApp(this)
            }
        } else {
            setContent {
                WearApp(this)
            }
        }
    }
}


@Composable
fun WearApp(context: Context) {
    WatchLockTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StatusView(context)
            Spacer(modifier = Modifier.height(18.dp))
            LockView(context)
        }
    }
}

@Composable
fun StatusView(context: Context) {
    var lockStatus by remember { mutableStateOf(LockStatus.UNKNOWN) }
    PhoneCommunicationService(context).getLockStatus {
        lockStatus = it
    }

    // request lock status every  second
    LaunchedEffect(Unit) {
        while (true) {
            PhoneCommunicationService(context).getLockStatus {
                lockStatus = it
            }
            delay(2000)
        }
    }
    StatusIcon(lockStatus)
    StatusText(lockStatus)
}

@Composable
fun LockView(context: Context) {
    Chip(onClick = { PhoneCommunicationService(context).requestLockPhone() },
        label = { Text("Lock phone", color = MaterialTheme.colors.onBackground) }, icon = {
            Icon(
                Icons.Filled.Lock,
                contentDescription = "lock icon",
                tint = MaterialTheme.colors.onBackground
            )
        }
    )
}

@Composable
fun StatusIcon(lockStatus: LockStatus) {
    return when (lockStatus) {
        LockStatus.LOCKED -> Icon(
            Icons.Filled.Lock,
            contentDescription = "lock icon",
            tint = MaterialTheme.colors.onBackground
        )
        LockStatus.UNLOCKED -> Icon(
            Icons.Filled.LockOpen,
            contentDescription = "unlocked icon",
            tint = MaterialTheme.colors.onBackground
        )
        LockStatus.UNKNOWN -> Icon(
            Icons.Filled.DeviceUnknown,
            contentDescription = "unkown icon",
            tint = MaterialTheme.colors.onBackground
        )
    }
}

@Composable
fun StatusText(lockStatus: LockStatus) {
    return when (lockStatus) {
        LockStatus.LOCKED -> Text(
            text = "Phone Locked",
            color = MaterialTheme.colors.onBackground,
            fontSize = 16.sp
        )
        LockStatus.UNLOCKED -> Text(
            text = "Phone Unlocked",
            color = MaterialTheme.colors.onBackground,
            fontSize = 16.sp
        )
        LockStatus.UNKNOWN -> Text(
            text = "Loading",
            color = MaterialTheme.colors.onBackground,
            fontSize = 16.sp
        )
    }
}

