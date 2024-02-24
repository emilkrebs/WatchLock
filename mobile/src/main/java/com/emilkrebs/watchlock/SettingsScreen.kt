package com.emilkrebs.watchlock

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.emilkrebs.watchlock.components.NavScreen
import com.emilkrebs.watchlock.components.Navbar

@Composable
@Preview
private fun SettingsScreenPreview() {
    SettingsScreen(context = LocalContext.current, navController = NavController(LocalContext.current))
}
@Composable
fun SettingsScreen(context: Context, navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Navbar(
                title = "Settings",
                navController = navController,
                currentScreen = NavScreen.Settings
            )

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AssistChip(
                    modifier = Modifier.height(AssistChipDefaults.Height),
                    onClick = {
                        setWatchLockEnabled(context, !isWatchLockEnabled(context))
                    },
                    label = { Text("Enable watch lock") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Enable watch lock",
                            Modifier.size(AssistChipDefaults.IconSize)
                        )
                    }
                )
            }
        }
    }
}



fun isWatchLockEnabled(context: Context): Boolean {
    return context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        .getBoolean("isActive", false)
}

fun setWatchLockEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        .edit()
        .putBoolean("isActive", enabled)
        .apply()
}