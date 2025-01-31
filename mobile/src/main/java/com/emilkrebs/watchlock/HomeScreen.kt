package com.emilkrebs.watchlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.emilkrebs.watchlock.components.NavScreen
import com.emilkrebs.watchlock.components.Navbar
import com.emilkrebs.watchlock.components.OnLifecycleEvent
import com.emilkrebs.watchlock.services.ACTION_PING_BROADCAST
import com.emilkrebs.watchlock.services.WatchCommunicationService
import com.emilkrebs.watchlock.services.WatchCommunicationService.Companion.openPlayStoreOnWatch
import com.emilkrebs.watchlock.utils.getAdminDialogIntent
import com.emilkrebs.watchlock.utils.isAdminActive

data class CheckListItem(val text: String, val success: Boolean)

val pingFilter = IntentFilter("com.emilkrebs.watchlock.PING")
private const val pingTimeout = 8000L

enum class PingStatus {
    SUCCESS, PENDING, FAILED, NONE
}

@Composable
fun HomeScreen(context: FragmentActivity, navController: NavController) {
    var pingStatus by remember { mutableStateOf(PingStatus.NONE) }
    var watchConnected by remember { mutableStateOf(false) }
    var isWearAppInstalled by remember { mutableStateOf(false) }
    var adminActive by remember { mutableStateOf(isAdminActive(context)) }
    var watchLockEnabled by remember { mutableStateOf(preferences.isWatchLockEnabled()) }

    var retry by remember { mutableIntStateOf(0) }


    val pingTimeoutRunnable = Runnable {
        if (pingStatus == PingStatus.PENDING) {
            pingStatus = PingStatus.FAILED
        }
    }

    LaunchedEffect(retry) {
        // Check if the watch is connected
        watchConnected = WatchCommunicationService.isWatchConnected(context)
        if (watchConnected) {
            isWearAppInstalled = WatchCommunicationService.isWearAppInstalled(context)
        }
    }

    // Broadcast receiver for the ping
    val pingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_PING_BROADCAST) {
                // Handle the broadcast here
                val pingValue = intent.getBooleanExtra("ping", false)
                pingStatus = if (pingValue) {  PingStatus.SUCCESS } else { PingStatus.FAILED }
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if(!isPreview) {
                    adminActive = isAdminActive(context)
                }
                retry++
            }

            Lifecycle.Event.ON_START -> {
                context.registerReceiver(pingReceiver, pingFilter, Context.RECEIVER_EXPORTED)
            }

            Lifecycle.Event.ON_DESTROY -> {
                context.unregisterReceiver(pingReceiver)
            }

            else -> {}
        }
    }


    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Navbar(
                title = "", navController = navController, currentScreen = NavScreen.Home
            )

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .offset(y = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CheckList(
                    watchConnected,
                    isWearAppInstalled,
                    pingStatus,
                    adminActive,
                    watchLockEnabled
                )

                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MainButton(adminActive, watchLockEnabled) {
                        if (!isWearAppInstalled) {
                            openPlayStoreOnWatch(context)
                        } else
                            if (!adminActive) {
                                context.startActivity(getAdminDialogIntent(context))
                            } else {
                                preferences.setWatchLockEnabled(
                                    !watchLockEnabled,
                                    context,
                                    onSuccess = {
                                        watchLockEnabled = !watchLockEnabled
                                    })
                            }
                    }

                    PingButton(pingStatus) {
                        if (pingStatus == PingStatus.PENDING) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.ping_already_pending),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pingStatus = try {
                                WatchCommunicationService(context).pingWatch()
                                Handler(Looper.getMainLooper()).postDelayed(
                                    pingTimeoutRunnable,
                                    pingTimeout
                                )
                                PingStatus.PENDING
                            } catch (e: Exception) {
                                Log.e("Ping", e.message.toString())
                                PingStatus.FAILED
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckList(
    watchConnected: Boolean,
    wearAppInstalled: Boolean,
    pingStatus: PingStatus,
    adminActive: Boolean,
    watchLockEnabled: Boolean
) {
    val checklist: ArrayList<CheckListItem> = ArrayList()
    checklist.add(
        when (watchConnected) {
            true -> CheckListItem(stringResource(R.string.watch_connected), true)
            false -> CheckListItem(stringResource(R.string.no_watch_connected), false)
        }
    )

    checklist.add(
        when (wearAppInstalled) {
            true -> CheckListItem(stringResource(R.string.watch_app_installed), true)
            false -> CheckListItem(stringResource(R.string.watch_app_not_installed), false)
        }
    )

    if (pingStatus != PingStatus.NONE && pingStatus != PingStatus.PENDING) {
        checklist.add(
            when (pingStatus) {
                PingStatus.SUCCESS -> CheckListItem(stringResource(R.string.ping_success), true)
                PingStatus.FAILED -> CheckListItem(stringResource(R.string.ping_failed), false)
                else -> CheckListItem(stringResource(R.string.ping_pending), false)
            }
        )
    }

    checklist.add(
        when (adminActive) {
            true -> CheckListItem(stringResource(R.string.admin_active), true)
            false -> CheckListItem(stringResource(R.string.admin_inactive), false)
        }
    )

    checklist.add(
        when (watchLockEnabled) {
            true -> CheckListItem(stringResource(R.string.watchlock_active), true)
            false -> CheckListItem(stringResource(R.string.watchlock_inactive), false)
        }
    )

    checklist.sortBy { !it.success }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentPadding = PaddingValues(32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {}
        items(checklist.size) { index ->
            val item = checklist[index]
            ChecklistItem(item.text, item.success)
        }

        item {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                when {
                    checklist.all { it.success } -> Text(
                        text = stringResource(R.string.ready_and_active_explanation),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // ready and inactive
                    !watchLockEnabled && adminActive -> Text(
                        text = stringResource(R.string.ready_and_inactive_explanation),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )

                    else -> Text(
                        text = stringResource(R.string.not_ready_explanation),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

        }
    }
}

@Composable
fun ChecklistItem(text: String, success: Boolean) {
    Card(
        modifier = Modifier
            .shadow(4.dp)
            .fillMaxWidth(),

        border = BorderStroke(
            width = 1.dp,
            color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (success) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = stringResource(R.string.success),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.failed),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = text, modifier = Modifier.padding(start = 8.dp), fontSize = 18.sp,

                color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
fun MainButton(adminActive: Boolean, watchLockEnabled: Boolean, onClick: () -> Unit) {
    val mainButtonText = if (!adminActive) stringResource(R.string.activate_watchlock)
    else if (watchLockEnabled) stringResource(R.string.deactivate_watchlock)
    else stringResource(R.string.activate_watchlock)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        Text(
            mainButtonText,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun PingButton(pingStatus: PingStatus, onClick: () -> Unit) {
    val buttonText = when (pingStatus) {
        PingStatus.SUCCESS -> stringResource(R.string.ping_watch)
        PingStatus.PENDING -> stringResource(R.string.ping_pending)
        PingStatus.FAILED -> stringResource(R.string.ping_watch)
        PingStatus.NONE -> stringResource(R.string.ping_watch)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Loading")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "Loading Animation",
    )

    OutlinedButton(
        onClick, modifier = Modifier
            .offset(y = 12.dp)
            .fillMaxWidth()
    ) {
        if (pingStatus == PingStatus.PENDING) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = stringResource(R.string.ping_watch),
                modifier = Modifier.graphicsLayer {
                    rotationZ = angle
                })
        }
        Text(buttonText)
    }
}