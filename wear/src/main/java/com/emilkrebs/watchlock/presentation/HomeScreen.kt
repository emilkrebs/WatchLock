package com.emilkrebs.watchlock.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.emilkrebs.watchlock.R
import com.emilkrebs.watchlock.presentation.services.LockStatus
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationService
import kotlinx.coroutines.delay


@Composable
fun HomeScreen(context: Context) {
    var isConnected by remember { mutableStateOf(false) }
    var isWatchInstalled by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }

    LaunchedEffect(retry) {
        isConnected = PhoneCommunicationService.isPhoneConnected(context)
        if (isConnected) {
            isWatchInstalled = PhoneCommunicationService.isWatchLockInstalled(context)
        }
    }

    if (isConnected && isWatchInstalled) {
        Connected(context)
    } else {
        NotConnected(
            message = if (isConnected) stringResource(R.string.watchlock_not_installed) else stringResource(
                R.string.no_phone_connected
            ),
            onRetry = {
                if (!isWatchInstalled && isConnected) {
                    PhoneCommunicationService.openPlayStoreOnWatch(context)
                    Toast.makeText(context, context.getString(R.string.check_your_phone), Toast.LENGTH_SHORT).show()
                }
                retry++
            }
        )
    }
}

@Composable
fun Connected(context: Context) {
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

@Composable
@Preview
fun NotConnected(message: String = "Phone not Connected", onRetry: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            modifier = Modifier.padding(16.dp)
        )
        Chip(onClick = onRetry,
            label = {
                Text(
                    stringResource(R.string.retry),
                    color = MaterialTheme.colors.onBackground
                )
            }, icon = {
                Icon(
                    Icons.Filled.Sync,
                    contentDescription = "sync icon",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        )

    }
}

@Composable
fun StatusView(context: Context) {
    var lockStatus by remember { mutableStateOf(LockStatus.UNKNOWN) }
    var isLoading = false
    val phoneCommunicationService = PhoneCommunicationService(context)

    phoneCommunicationService.onLockStatusReceived = {
        lockStatus = it
        isLoading = false
    }

    // request the lock status every 500ms
    LaunchedEffect(Unit) {
        while (true) {
            if (!isLoading) {
                isLoading = true
                phoneCommunicationService.requestLockStatus()
                LockStatus.UNKNOWN
            }
            delay(500)
        }
    }

    StatusIcon(lockStatus)
    StatusText(lockStatus)
}

@Composable
fun LockView(context: Context) {
    Chip(onClick = {
        PhoneCommunicationService(context).requestLockPhone()
    },
        label = {
            Text(
                stringResource(R.string.lock_phone),
                color = MaterialTheme.colors.onBackground
            )
        }, icon = {
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
            contentDescription = "unknown icon",
            tint = MaterialTheme.colors.onBackground
        )
    }
}

@Composable
fun StatusText(lockStatus: LockStatus) {
    Text(
        text = when (lockStatus) {
            LockStatus.LOCKED -> stringResource(R.string.phone_locked)
            LockStatus.UNLOCKED -> stringResource(R.string.phone_unlocked)
            LockStatus.UNKNOWN -> stringResource(R.string.loading)
        },
        color = MaterialTheme.colors.onBackground,
        fontSize = 16.sp
    )
}

