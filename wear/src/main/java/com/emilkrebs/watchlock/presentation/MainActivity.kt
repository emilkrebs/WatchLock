package com.emilkrebs.watchlock.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.emilkrebs.watchlock.R
import com.emilkrebs.watchlock.presentation.services.LockStatus
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationService
import com.emilkrebs.watchlock.presentation.services.PhoneCommunicationService.Companion.convertImageByteArrayToBitmap
import com.emilkrebs.watchlock.presentation.theme.WatchLockTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp(this)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun Preview(context: Context = LocalContext.current) {
    Connected(context)
}

@Composable
fun WearApp(context: Context) {
    var isConnected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PhoneCommunicationService.isPhoneConnected(context) { connected ->
            isConnected = connected
        }
    }

    if (isConnected) {
        Connected(context)
    } else {
        NotConnected {
            PhoneCommunicationService.isPhoneConnected(context) { connected ->
                isConnected = connected
            }
        }
    }

}

@Composable
fun Connected(context: Context) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    WatchLockTheme {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
        ){
            HorizontalPager(state = pagerState) { page ->
                Log.d("Pager", "Current Page: $page")
                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .background(MaterialTheme.colors.background),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            StatusView(context)
                            Spacer(modifier = Modifier.height(18.dp))
                            LockView(context)
                        }
                    }

                    1 -> {
                        Screensharing(context)
                    }
                }
            }

            // Page Indicator
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) Color.LightGray else Color.DarkGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Screensharing(context: Context) {
    val phoneCommunicationService = PhoneCommunicationService(context)
    var currentScreenshot by remember { mutableStateOf<ByteArray?>(null) }

    phoneCommunicationService.requestScreenshot()
    phoneCommunicationService.onScreenshotReceived = { screenshot ->
        // handle screenshot
        currentScreenshot = screenshot

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .clickable {
                phoneCommunicationService.requestScreenshot()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (currentScreenshot != null) {
            // display the screenshot
            Image(
                bitmap = convertImageByteArrayToBitmap(currentScreenshot!!).asImageBitmap(),
                contentDescription = "Current Screenshot",
            )
        } else {
            Text(
                text = stringResource(R.string.loading),
                color = MaterialTheme.colors.onBackground,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
@Preview
fun NotConnected(onRetry: () -> Unit = {}) {
    WatchLockTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.no_phone_connected),
                color = MaterialTheme.colors.onBackground,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Chip(onClick = onRetry,
                label = {
                    Text(
                        stringResource(R.string.retry),
                        color = MaterialTheme.colors.onBackground
                    )
                }, icon = {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = "sync icon",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            )

        }
    }
}

@Composable
fun StatusView(context: Context) {
    var lockStatus by remember { mutableStateOf(LockStatus.UNKNOWN) }
    var isLoading = false
    val phoneCommunicationService = PhoneCommunicationService(context)

    phoneCommunicationService.onLockStatusReceived = {
        lockStatus = it
        isLoading = false
    }

    // request the lock status every 500ms
    LaunchedEffect(Unit) {
        while (true) {
            if (!isLoading) {
                isLoading = true
                phoneCommunicationService.requestLockStatus()
                LockStatus.UNKNOWN
            }
            delay(500)
        }
    }

    StatusIcon(lockStatus)
    StatusText(lockStatus)
}

@Composable
fun LockView(context: Context) {
    Chip(onClick = {
        PhoneCommunicationService(context).requestLockPhone()
    },
        label = {
            Text(
                stringResource(R.string.lock_phone),
                color = MaterialTheme.colors.onBackground
            )
        }, icon = {
            Icon(
                Icons.Filled.Lock,
                contentDescription = "lock icon",
                tint = MaterialTheme.colors.onBackground
            )
        }
    )
}


@Composable
fun StatusIcon(lockStatus: LockStatus) {
    return when (lockStatus) {
        LockStatus.LOCKED -> Icon(
            Icons.Filled.Lock,
            contentDescription = "lock icon",
            tint = MaterialTheme.colors.onBackground
        )

        LockStatus.UNLOCKED -> Icon(
            Icons.Filled.LockOpen,
            contentDescription = "unlocked icon",
            tint = MaterialTheme.colors.onBackground
        )

        LockStatus.UNKNOWN -> Icon(
            Icons.Filled.DeviceUnknown,
            contentDescription = "unknown icon",
            tint = MaterialTheme.colors.onBackground
        )
    }
}

@Composable
fun StatusText(lockStatus: LockStatus) {
    Text(
        text = when (lockStatus) {
            LockStatus.LOCKED -> stringResource(R.string.phone_locked)
            LockStatus.UNLOCKED -> stringResource(R.string.phone_unlocked)
            LockStatus.UNKNOWN -> stringResource(R.string.loading)
        },
        color = MaterialTheme.colors.onBackground,
        fontSize = 16.sp
    )
}

