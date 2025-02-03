package com.emilkrebs.watchlock.presentation

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.emilkrebs.watchlock.R

@Composable
fun SettingsScreen(context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.about),
            style = MaterialTheme.typography.title1,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "WatchLock ${getVersionName(context)} (${getVersionCode(context)})",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 16.dp)
        )

    }

}

fun getVersionName(context: Context): String {
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "NOT FOUND"
}

fun getVersionCode(context: Context): Long {
    return context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
}