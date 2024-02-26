package com.emilkrebs.watchlock

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.emilkrebs.watchlock.ui.theme.WatchLockTheme
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

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(animationSpec = tween(100)) + fadeIn(animationSpec = tween(100))
    }

    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(animationSpec = tween(100)) + fadeOut(animationSpec = tween(100))
    }

    WatchLockTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = enterTransition,
                exitTransition = exitTransition
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
