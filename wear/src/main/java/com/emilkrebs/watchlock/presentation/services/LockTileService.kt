package com.emilkrebs.watchlock.presentation.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.IntentFilter
import androidx.compose.ui.graphics.toArgb
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.wear.tiles.*
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import com.emilkrebs.watchlock.presentation.MainActivity
import com.emilkrebs.watchlock.presentation.sendMessage
import com.emilkrebs.watchlock.presentation.theme.Purple200
import com.emilkrebs.watchlock.presentation.theme.Purple500
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.TimelineBuilders.TimeInterval
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import com.emilkrebs.watchlock.presentation.getNodes
import com.emilkrebs.watchlock.presentation.isConnectedToPhone

const val RESOURCES_VERSION = "1"

class LockTileService : TileService() {

    var isPhoneLocked: Boolean = false

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        sendMessage(this, "/wearable/query", "lock_status")
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(lockStatusReceiver, IntentFilter(ACTION_SEND))

        val timeline = TimelineBuilders.Timeline.Builder()
        timeline.addTimelineEntry(TimelineEntry.Builder()
            .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(notConnectedLayout()).build())
//            .setValidity(TimeInterval.Builder().build())
            .build()
        )
        timeline.addTimelineEntry(TimelineEntry.Builder()
            .setLayout(LayoutElementBuilders.Layout.Builder().setRoot(mainLayout()).build())
            .build()
        )
        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                // refresh every 30sec
                .setFreshnessIntervalMillis(30 * 1000)
                .setTimeline(timeline.build()).build()
        )
    }


    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    private fun notConnectedLayout(): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.wrap())
            .setHeight(DimensionBuilders.wrap())
            .addContent(
                Text.Builder(this, "Phone is not connected")
                    .setColor(ColorBuilders.argb(0xFFFF0000.toInt()))
                    .setWeight(LayoutElementBuilders.FONT_WEIGHT_BOLD)
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .build()
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(12F))
                    .build()
            )
            .addContent(
                Text.Builder(this, "Refresh")
                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                    .setWeight(LayoutElementBuilders.FONT_WEIGHT_NORMAL)
                    .setTypography(Typography.TYPOGRAPHY_BODY1)
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder()
                                    .setOnClick(
                                        ActionBuilders.LoadAction.Builder().build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun showLayout(): LayoutElementBuilders.LayoutElement {
        var connected = isConnectedToPhone(this)
        return if (!connected) {
            notConnectedLayout()
        } else {
            mainLayout()
        }
    }

    private fun mainLayout(): LayoutElementBuilders.LayoutElement {
        val buttonContent: String = if (isPhoneLocked) {
            "Phone locked"
        } else {
            "Phone unlocked"
        }
        val buttonColor: Int = if (isPhoneLocked) {
            Purple200.toArgb()
        } else {
            Purple500.toArgb()
        }
        return LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.wrap())
            .setHeight(DimensionBuilders.wrap())
            .addContent(
                lockButton(buttonContent, buttonColor)
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(12F))
                    .build()
            )
            .addContent(
                Text.Builder(this, "Refresh")
                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                    .setWeight(LayoutElementBuilders.FONT_WEIGHT_NORMAL)
                    .setTypography(Typography.TYPOGRAPHY_BODY1)
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder()
                                    .setOnClick(
                                        ActionBuilders.LoadAction.Builder().build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun lockButton(content: String, color: Int): Text {
        return Text.Builder(this, content)
            .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
            .setWeight(LayoutElementBuilders.FONT_WEIGHT_MEDIUM)
            .setTypography(Typography.TYPOGRAPHY_BUTTON)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setPadding(
                        ModifiersBuilders.Padding.Builder()
                            .setAll(dp(12f)).build()
                    )
                    .setBackground(
                        ModifiersBuilders.Background.Builder()
                            .setCorner(
                                ModifiersBuilders.Corner.Builder()
                                    .setRadius(dp(26f)).build()
                            )
                            .setColor(ColorBuilders.argb(color))
                            .build()
                    )
                    .setClickable(
                        Clickable.Builder()
                            .setId("lock_phone")
                            .setOnClick(
                                ActionBuilders.LaunchAction.Builder()
                                    .setAndroidActivity(
                                        ActionBuilders.AndroidActivity.Builder()
                                            .setClassName(MainActivity::class.java.name)
                                            .setPackageName(this.packageName)
                                            .build()
                                    ).build()
                            ).build()
                    ).build()
            ).build()
    }
    private val lockStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            if (extras != null) {
                if(isPhoneLocked != (extras.getString("status") == "phone_locked")){
                    isPhoneLocked = extras.getString("status") == "phone_locked"
                    getUpdater(context).requestUpdate(LockTileService::class.java)
                }
            }
        }
    }
}