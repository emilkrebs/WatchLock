package com.emilkrebs.watchlock.presentation

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.*
import androidx.wear.tiles.TileService
import com.emilkrebs.watchlock.presentation.services.Message
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationService
import com.emilkrebs.watchlock.presentation.theme.WatchLockTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clickableId = intent.getStringExtra(TileService.EXTRA_CLICKABLE_ID)
        if (clickableId == "lock_phone") {
            setContent {
                WearApp(this)
            }
        } else {
            setContent {
                WearApp(this)
            }
        }
    }
}


@Composable
fun WearApp(context: Context) {
    WatchLockTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LockButton(context)
        }
    }
}

@Composable
fun LockButton(context: Context) {
    Chip(
        onClick = {
            PhoneCommunicationService(context).requestLockPhone{
                if (it) {
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        },
        label = { Text("Lock phone", color = MaterialTheme.colors.onBackground) },
        icon = { Icon(Icons.Filled.Lock, contentDescription = "lock icon", tint = MaterialTheme.colors.onBackground) }
    )
}



