package com.emilkrebs.watchlock

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.emilkrebs.watchlock.receivers.AdminReceiver
import com.emilkrebs.watchlock.services.ACTION_PING_BROADCAST
import com.emilkrebs.watchlock.services.WatchCommunicationService
import com.emilkrebs.watchlock.ui.theme.WatchLockTheme

enum class PingStatus {
    SUCCESS,
    PENDING,
    FAILED,
    NONE
}

data class CheckListItem(val text: String, val success: Boolean)

const val PREFERENCE_FILE_KEY = "com.example.android.watchlock_preferences"
val pingFilter = IntentFilter("com.emilkrebs.watchlock.PING")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MobileApp(this)
        }
    }
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun MobileApp(context: Context) {
    var pingStatus by remember { mutableStateOf(PingStatus.NONE) }
    var watchConnected by remember { mutableStateOf(false) }
    var adminActive by remember { mutableStateOf(false) }
    var watchLockEnabled by remember {
        mutableStateOf(
            context.getSharedPreferences(
                PREFERENCE_FILE_KEY,
                MODE_PRIVATE
            ).getBoolean("isActive", false)
        )
    }
    var mainButtonText by remember { mutableStateOf(context.getString(R.string.activate_watchlock)) }

    val pingTimeoutRunnable = Runnable {
        if (pingStatus == PingStatus.PENDING) {
            pingStatus = PingStatus.FAILED
        }
    }

    mainButtonText =
        if (watchLockEnabled) stringResource(R.string.deactivate_watchlock)
        else if (!adminActive)stringResource(R.string.activate_watchlock_admin)
        else stringResource(R.string.activate_watchlock)


    LaunchedEffect(Unit) {
        // Check if the watch is connected
        WatchCommunicationService.isWatchConnected(context) { connected ->
            watchConnected = connected
        }
    }

    // Broadcast receiver for the ping
    val pingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_PING_BROADCAST) {
                // Handle the broadcast here
                val pingValue = intent.getBooleanExtra("ping", false)
                pingStatus = if (pingValue) {
                    PingStatus.SUCCESS
                } else {
                    PingStatus.FAILED
                }
            }
        }
    }

    // register the ping receiver
    LocalBroadcastManager.getInstance(context)
        .registerReceiver(pingReceiver, pingFilter)

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                adminActive = isAdminActive(context)
            }

            else -> {}
        }
    }

    WatchLockTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-32).dp)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CheckList(watchConnected, pingStatus, adminActive, watchLockEnabled)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            watchLockEnabled = if (!adminActive) {
                                showRequestAdminDialog(context)
                                true
                            } else {
                                !watchLockEnabled
                            }
                            context.getSharedPreferences(PREFERENCE_FILE_KEY, MODE_PRIVATE).edit()
                                .putBoolean("isActive", watchLockEnabled).apply()

                            mainButtonText = if (watchLockEnabled) context.getString(R.string.deactivate_watchlock) else context.getString(R.string.activate_watchlock)
                        },

                        ) {
                        Text(
                            mainButtonText,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    PingButton(pingStatus) {
                        pingStatus = try {
                            WatchCommunicationService(context).pingWatch()
                            Handler(Looper.getMainLooper()).postDelayed(pingTimeoutRunnable, 8000)
                            PingStatus.PENDING
                        } catch (e: Exception) {
                            PingStatus.FAILED
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WatchLockTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-32).dp)
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CheckList(true, PingStatus.SUCCESS, adminActive = true, watchLockEnabled = true)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Button(onClick = {}) {
                        Text("Activate WatchLock")
                    }
                    PingButton(PingStatus.FAILED) {

                    }
                }
            }
        }
    }
}

@Composable
fun PingButton(pingStatus: PingStatus, onClick: () -> Unit) {
    val buttonText = when (pingStatus) {
        PingStatus.SUCCESS -> "Ping Watch"
        PingStatus.PENDING -> "Ping requested..."
        PingStatus.FAILED -> "Ping Failed"
        PingStatus.NONE -> "Ping Watch"
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

    OutlinedButton(onClick) {
        if (pingStatus == PingStatus.PENDING) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Ping",
                modifier = Modifier.graphicsLayer {
                    rotationZ = angle
                })
        }
        Text(buttonText, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
fun CheckList(
    watchConnected: Boolean,
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
            .padding(16.dp),
        contentPadding = PaddingValues(32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(checklist.size) { index ->
            val item = checklist[index]
            ChecklistItem(item.text, item.success)
        }

        item {
            if (checklist.all { it.success } && pingStatus == PingStatus.SUCCESS) {
                Text(
                    text = stringResource(R.string.ready_and_active), modifier = Modifier.offset(y = (-8).dp),
                    fontSize = 12.sp,
                )
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
            1.dp,
            if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (success) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Failed",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 18.sp,

                color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }

}

private fun isAdminActive(context: Context): Boolean {
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, AdminReceiver::class.java)

    return devicePolicyManager.isAdminActive(adminComponent)
}

private fun showRequestAdminDialog(context: Context) {
    val adminComponent = ComponentName(context, AdminReceiver::class.java)
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    val explanation = context.getString(R.string.admin_explanation)

    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
    intent.putExtra(
        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
        explanation
    )

    context.startActivity(intent)
}

