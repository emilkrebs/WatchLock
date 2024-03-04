package com.emilkrebs.watchlock

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
        preferences = Preferences(this)
        
        setContent {
            WatchLockTheme {
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
        MobileApp(LocalContext.current)
    }
}

@Composable
fun MobileApp(context: Context) {
    val navController = rememberNavController()


    WatchLockTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = enterTransition,
                exitTransition =  exitTransition
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
}
