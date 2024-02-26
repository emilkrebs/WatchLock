package com.emilkrebs.watchlock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.JsonReader
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.emilkrebs.watchlock.components.NavScreen
import com.emilkrebs.watchlock.components.Navbar
import com.emilkrebs.watchlock.services.startDistanceCheckerService
import com.emilkrebs.watchlock.services.stopDistanceCheckerService
import com.emilkrebs.watchlock.utils.Preferences
import com.emilkrebs.watchlock.utils.revokeAdminPermissions
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt


@Composable
@Preview
private fun SettingsScreenPreview() {
    preferences = Preferences(LocalContext.current)
    isPreview = LocalInspectionMode.current
    SettingsScreen(
        context = LocalContext.current, navController = NavController(LocalContext.current)
    )
}

@Composable
fun SettingsScreen(context: Context, navController: NavController) {
    var isWatchLockEnabled by remember { mutableStateOf(preferences.isWatchLockEnabled()) }
    var isLockNotNearbyEnabled by remember { mutableStateOf(preferences.isLockNotNearbyEnabled()) }
    val fragmentActivity = LocalContext.current as FragmentActivity

    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Navbar(
                title = "Settings",
                navController = navController,
                currentScreen = NavScreen.Settings
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Section(title = "General", content = {
                    BooleanSetting(
                        label = "Enable WatchLock",
                        details = "Enable or disable locking your phone using WatchLock.",
                        enabled = isWatchLockEnabled
                    ) {
                        preferences.setWatchLockEnabled(
                            it, context, fragmentActivity, onSuccess = {
                                isWatchLockEnabled = it
                            }, onFailure = {
                                isWatchLockEnabled = !it
                            }
                        )
                    }

                    ResetWatchLockSetting(context, navController)
                })

                Section(title = "Experimental", content = {
                    BooleanSetting(
                        label = "Lock on distance",
                        details = "Lock the phone when the watch is not nearby.",
                        enabled = isLockNotNearbyEnabled
                    ) {
                        preferences.setLockNotNearbyEnabled(it)
                        if (it) {
                            startDistanceCheckerService(context)
                        } else {
                            stopDistanceCheckerService(context)
                        }
                        isLockNotNearbyEnabled = it
                    }

                    if (isLockNotNearbyEnabled) {
                        RangeSetting(
                            label = "Nearby Interval",
                            details = "The interval in which the phone checks if the watch is nearby.",
                            valueRange = 15f..60f,
                            unitName = " minutes",
                            range = preferences.getLockNotNearbyInterval()
                        ) {
                            preferences.setLockNotNearbyInterval(it)
                        }
                    }
                })

                AboutSection(context)
            }
        }
    }
}


@Composable
fun AboutSection(context: Context) {
    var latestVersion by remember { mutableStateOf("") }
    val versionName = getVersionName(context)
    val versionCode = getVersionCode(context)

    Section(title = "About", content = {
        if (latestVersion.isNotEmpty()) {
            // show package name
            ButtonSetting(
                label = "Latest Version", details = "v$latestVersion (Current: v$versionName)"
            ) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/emilkrebs/WatchLock/releases/latest")
                    )
                )
            }
        }

        // github link
        ButtonSetting(
            label = "GitHub", icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.github_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }, details = "View the source code on GitHub."
        ) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/emilkrebs/WatchLock")
                )
            )
        }

        CheckVersionButton(context) {
            latestVersion = it
        }


        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "WatchLock Version v$versionName ($versionCode)",
                style = MaterialTheme.typography.labelMedium,
            )
        }

    })
}

@Composable
@Preview
fun RangeDialog(
    defaultValue: Float = 0f,
    title: String = "Enter Range",
    valueRange: ClosedFloatingPointRange<Float> = 1f..10f,
    unitName: String = "",
    onRangeEntered: (range: Int) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var sliderValue by remember { mutableFloatStateOf(defaultValue) }

    Dialog(onDismissRequest = {
        onClose()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    title, style = MaterialTheme.typography.titleLarge
                )
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(value = sliderValue,
                        valueRange = valueRange,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onValueChange = { sliderValue = it })
                    Text(
                        "${sliderValue.roundToInt()}$unitName",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onClose,
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onRangeEntered(sliderValue.roundToInt()) },
                    ) {
                        Text("Okay")
                    }
                }
            }
        }
    }
}

@Composable
fun Section(
    title: String, content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title, style = MaterialTheme.typography.titleLarge
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun BooleanSetting(
    label: String, details: String = "", enabled: Boolean, onToggle: (Boolean) -> Unit = { }
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight()
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
            )
            if (details.isNotEmpty()) {
                Text(
                    details,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        Switch(checked = enabled, onCheckedChange = onToggle, thumbContent = if (enabled) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        })

    }
}

@Composable
fun RangeSetting(
    label: String,
    details: String = "",
    range: Int = 0,
    unitName: String = "",
    valueRange: ClosedFloatingPointRange<Float> = 1f..60f,
    onRangeChanged: (Int) -> Unit = { }
) {
    var showRangeDialog by remember { mutableStateOf(false) }
    var value by remember { mutableIntStateOf(range) }

    if (showRangeDialog) RangeDialog(defaultValue = preferences.getLockNotNearbyInterval()
        .toFloat(),
        title = label,
        valueRange = valueRange,
        unitName = unitName,
        onRangeEntered = {
            preferences.setLockNotNearbyInterval(it)
            showRangeDialog = false
            value = it
            onRangeChanged(it)
        },
        onClose = {
            showRangeDialog = false
        })


    Row(
        modifier = Modifier
            .clickable { showRangeDialog = true }
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .wrapContentHeight()
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
            )
            if (details.isNotEmpty()) {
                Text(
                    details,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        Text(
            "$value $unitName",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun ButtonSetting(
    label: String,
    details: String = "",
    icon: @Composable () -> Unit = { },
    onClick: () -> Unit = { }
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),

        ) {
        icon()

        Column(
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
            )
            if (details.isNotEmpty()) {
                Text(
                    details,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

    }
}

@Composable
fun ResetWatchLockSetting(context: Context, navController: NavController) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        Dialog(onDismissRequest = {
            showConfirmationDialog = false
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Reset WatchLock", style = MaterialTheme.typography.titleLarge
                    )

                    Column {
                        Text(
                            "Caution:", style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            "Are you sure you want to reset the WatchLock settings and revoke the admin permissions?\nThis action will cause WatchLock to restart.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                showConfirmationDialog = false
                            },
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                preferences.resetPreferences()
                                revokeAdminPermissions(context)

                                // reload the settings screen
                                navController.navigate(NavScreen.Home.route)
                            },
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }
    ButtonSetting(
        label = "Reset WatchLock",
        details = "Reset the WatchLock settings and revoke admin privileges."
    ) {
        showConfirmationDialog = true
    }

}

@Composable
fun CheckVersionButton(context: Context, onLatestVersion: (versionName: String) -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var fetchingVersion by remember { mutableStateOf(false) }

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
        onClick = {
            // launch in new scope
            scope.launch {
                fetchingVersion = true
                fetchLatestVersion(onSuccess = {
                    Toast.makeText(
                        context, "Latest version available: $it", Toast.LENGTH_LONG
                    ).show()
                    fetchingVersion = false
                    onLatestVersion(it)
                }, onError = {
                    Toast.makeText(
                        context,
                        "There was an error while fetching the latest version.",
                        Toast.LENGTH_LONG
                    ).show()
                    fetchingVersion = false
                })

            }

        },
    ) {
        if (fetchingVersion) {
            Icon(Icons.Default.Refresh,
                contentDescription = "Ping",
                modifier = Modifier.graphicsLayer {
                    rotationZ = angle
                })
        }
        Text(
            text = when {
                fetchingVersion -> "Fetching latest version…"
                else -> "Check for updates"
            }

        )
    }
}

fun fetchLatestVersion(onSuccess: (versionName: String) -> Unit = {}, onError: () -> Unit = {}) {
    val executor: ExecutorService = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())

    executor.execute {
        try {
            var latestVersion = ""
            URL("https://api.github.com/repos/emilkrebs/WatchLock/releases").openStream()
                .use { stream ->
                    JsonReader(stream.reader().buffered()).use { reader ->
                        reader.beginArray()
                        while (reader.hasNext()) {
                            reader.beginObject()
                            while (reader.hasNext()) {
                                val name = reader.nextName()
                                if (name == "tag_name") {
                                    latestVersion = reader.nextString()
                                } else {
                                    reader.skipValue()
                                }
                            }
                            reader.endObject()
                        }
                        reader.endArray()
                    }
                }

            if (latestVersion.isNotEmpty()) {
                handler.post {
                    onSuccess(latestVersion)
                }
            } else {
                handler.post {
                    onError()
                }
            }
        } catch (e: Exception) {
            // show a dialog to the user that the version check failed
            handler.post {
                onError()
            }
        }
    }
}

fun getVersionName(context: Context): String {
    if (isPreview) {
        return "x.x.x"
    }
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName
}

fun getVersionCode(context: Context): Long {
    if (isPreview) {
        return 0
    }
    return context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
}