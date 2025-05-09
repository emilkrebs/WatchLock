package com.emilkrebs.watchlock

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.emilkrebs.watchlock.components.NavScreen
import com.emilkrebs.watchlock.components.Navbar
import com.emilkrebs.watchlock.services.DistanceCheckerService
import com.emilkrebs.watchlock.services.WatchListenerService
import com.emilkrebs.watchlock.services.startDistanceCheckerService
import com.emilkrebs.watchlock.services.stopDistanceCheckerService
import com.emilkrebs.watchlock.utils.Preferences
import com.emilkrebs.watchlock.utils.revokeAdminPermissions
import kotlin.math.roundToInt


const val GROUP_INVITE = "https://groups.google.com/forum/#!forum/watchlock/join"

@Composable
@Preview
private fun SettingsScreenPreview() {
    val context = LocalContext.current
    preferences = Preferences(context)
    isPreview = LocalInspectionMode.current

    SettingsScreen(
        FragmentActivity(), navController = NavController(context)
    )
}

@Composable
fun SettingsScreen(context: FragmentActivity, navController: NavController) {
    var isWatchLockEnabled by remember { mutableStateOf(preferences.isWatchLockEnabled()) }
    var isLockNotNearbyEnabled by remember { mutableStateOf(preferences.isLockNotNearbyEnabled()) }

    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
        ) {
            Navbar(
                title = stringResource(R.string.settings),
                navController = navController,
                currentScreen = NavScreen.Settings
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Section(title = stringResource(R.string.general), content = {
                    BooleanSetting(
                        label = stringResource(R.string.enable_watchlock),
                        details = stringResource(R.string.enable_watchlock_details),
                        enabled = isWatchLockEnabled
                    ) {
                        preferences.setWatchLockEnabled(
                            it, context, onSuccess = {
                                isWatchLockEnabled = it
                            }, onFailure = {
                                isWatchLockEnabled = !it
                            }
                        )
                    }


                    // restart services
                    ButtonSetting(
                        label = stringResource(R.string.restart_services),
                        details = stringResource(R.string.restart_services_details),
                    ) {
                        try {
                            restartServices(context)
                            Toast.makeText(
                                context,
                                context.getString(R.string.services_restart_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.services_restart_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                    ResetWatchLockSetting(context, navController)
                })

                Section(title = stringResource(R.string.experimental), content = {
                    BooleanSetting(
                        label = stringResource(R.string.lock_on_distance),
                        details = stringResource(R.string.lock_on_distance_details),
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
                            label = stringResource(R.string.nearby_interval),
                            details = stringResource(R.string.nearby_interval_details),
                            valueRange = 15f..60f,
                            unitName = " ${stringResource(R.string.minutes)}",
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
    val versionName = getVersionName(context)
    val versionCode = getVersionCode(context)

    Section(title = stringResource(R.string.about), content = {
        // github link
        ButtonSetting(
            label = stringResource(R.string.github), icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.github_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }, details = stringResource(R.string.view_source)
        ) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.github_link))
                )
            )
        }

        // check for updates
       OutlinedButton(
            onClick = {
               // open play store
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.play_store_link))
                    )
                )
            }
        ) {
            Text(stringResource(R.string.check_for_updates))
        }


        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                String.format(
                    stringResource(R.string.version_text),
                    versionName,
                    versionCode
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }

    })
}

@Composable
@Preview
fun RangeDialog(
    defaultValue: Float = 0f,
    title: String = "",
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
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = { onRangeEntered(sliderValue.roundToInt()) },
                    ) {
                        Text(stringResource(R.string.okay))
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
                        stringResource(R.string.reset_watchlock),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Column {
                        Text(
                            stringResource(R.string.caution),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            stringResource(R.string.reset_watchlock_warning),
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
                            Text(stringResource(R.string.cancel))
                        }
                        Button(
                            onClick = {
                                preferences.resetPreferences()
                                revokeAdminPermissions(context)

                                // reload the settings screen
                                navController.navigate(NavScreen.Home.route)
                            },
                        ) {
                            Text(stringResource(R.string.reset))
                        }
                    }
                }
            }
        }
    }
    ButtonSetting(
        label = stringResource(R.string.reset_watchlock),
        details = stringResource(R.string.reset_watchlock_details),
    ) {
        showConfirmationDialog = true
    }

}

fun openGroupInvite(context: Context) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(GROUP_INVITE)
        )
    )
}

fun restartServices(context: Context) {
    WatchListenerService.restartService(context)
    DistanceCheckerService.restartService(context)
}

fun getVersionName(context: Context): String {
    if (isPreview) {
        return "x.x.x"
    }
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "NOT FOUND"
}

fun getVersionCode(context: Context): Long {
    if (isPreview) {
        return 0
    }
    return context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
}