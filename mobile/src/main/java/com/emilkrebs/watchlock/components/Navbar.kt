package com.emilkrebs.watchlock.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.emilkrebs.watchlock.R

enum class NavScreen(val route: String) {
    Home("home"),
    Settings("settings")
}

@Composable
@Preview
fun NavbarPreview() {
    Navbar(
        title = stringResource(R.string.app_name),
        navController = NavController(LocalContext.current),
        currentScreen = NavScreen.Home
    )
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
        label = { Text(stringResource(R.string.back)) },
        leadingIcon = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_to_previous_screen),
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

@Composable
private fun SettingsButton(navController: NavController) {
    OutlinedButton(
        modifier = Modifier.size(40.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(4.dp),
        onClick = {
            navController.navigate("settings")
        },
    )
    {
        Icon(
            Icons.Filled.Settings,
            contentDescription = stringResource(R.string.open_the_settings),
            Modifier.fillMaxSize()
        )
    }
}
