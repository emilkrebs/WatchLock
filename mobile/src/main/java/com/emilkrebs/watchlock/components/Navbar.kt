package com.emilkrebs.watchlock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

enum class NavScreen(val route: String) {
    Home("home"),
    Settings("settings")
}
@Composable
fun Navbar(
    title: String,
    navController: NavController,
    currentScreen: NavScreen,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)

        if(currentScreen != NavScreen.Home) BackButton(navController = navController)
        if(currentScreen != NavScreen.Settings) SettingsButton(navController = navController)
    }
}

@Composable
private fun BackButton(navController: NavController) {
    AssistChip(
        modifier = Modifier.height(AssistChipDefaults.Height),
        onClick = {
            navController.popBackStack()
        },
        label = { Text("Back") },
        leadingIcon = {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "Back to previous screen",
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
private fun SettingsButton(navController: NavController) {
    AssistChip(
        modifier = Modifier.size(AssistChipDefaults.Height),
        onClick = {
            navController.navigate("settings")
        },
        label = { Text("Settings") },
        leadingIcon = {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Open the Settings",
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}
