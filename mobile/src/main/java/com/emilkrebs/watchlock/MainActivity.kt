package com.emilkrebs.watchlock

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emilkrebs.watchlock.ui.theme.WatchLockTheme
import com.emilkrebs.watchlock.ui.theme.enterTransition
import com.emilkrebs.watchlock.ui.theme.exitTransition
import com.emilkrebs.watchlock.utils.Preferences


lateinit var preferences: Preferences
var isPreview = false
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        preferences = Preferences(this)

        setContent {
            WatchLockTheme (dynamicColor = true) {
                MobileApp(this)
            }
        }
    }
}

@Preview(name = "Application", showBackground = true, showSystemUi = true)
@Composable
fun MobileAppPreview() {
    preferences = Preferences(LocalContext.current)
    isPreview = LocalInspectionMode.current
    WatchLockTheme(darkTheme = true) {
        MobileApp(FragmentActivity())
    }
}

@Composable
fun MobileApp(context: FragmentActivity) {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = enterTransition,
            exitTransition =  exitTransition,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(context, navController)
            }
            composable("settings") {
                SettingsScreen(context, navController)
            }
        }
    }
}
