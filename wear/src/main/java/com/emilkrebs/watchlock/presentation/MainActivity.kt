/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.emilkrebs.watchlock.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotTouch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneEnabled
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import androidx.wear.tiles.TileService
import com.emilkrebs.watchlock.presentation.theme.WatchLockTheme
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable


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
            sendMessage(context, "lock_phone")
        },
        label = { Text("Lock phone") },
        icon = { Icon(Icons.Filled.Lock, contentDescription = "lock icon") }
    )

}

fun sendMessage(context: Context, message: String) {
    Thread(Runnable {
        run {
            getNodes(context).forEach {
                val messageApiClient = Wearable.getMessageClient(context)
                Tasks.await(messageApiClient.sendMessage(it, "/command", message.toByteArray()).addOnSuccessListener {

                })
            }

        }
    }).start()

}

private fun getNodes(context: Context): Collection<String> {
    return Tasks.await(Wearable.getNodeClient(context).connectedNodes).map { it.id }
}



