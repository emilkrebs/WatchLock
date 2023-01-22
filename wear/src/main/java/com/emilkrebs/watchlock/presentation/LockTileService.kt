package com.emilkrebs.watchlock.presentation

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.wear.tiles.*
import androidx.wear.tiles.ColorBuilders.argb
import androidx.wear.tiles.DimensionBuilders.*
import androidx.wear.tiles.LayoutElementBuilders.FONT_WEIGHT_MEDIUM
import androidx.wear.tiles.ModifiersBuilders.Background
import androidx.wear.tiles.ModifiersBuilders.Border
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.ModifiersBuilders.Corner
import androidx.wear.tiles.ModifiersBuilders.Padding
import androidx.wear.tiles.material.*
import com.emilkrebs.watchlock.presentation.theme.Purple200
import com.emilkrebs.watchlock.presentation.theme.WatchLockTheme
import com.emilkrebs.watchlock.presentation.theme.wearColorPalette
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture


private const val RESOURCES_VERSION = "1"


class LockTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        return Futures.immediateFuture(
            TileBuilders.Tile.Builder()
                .setResourcesVersion(RESOURCES_VERSION)
                .setTimeline(
                    TimelineBuilders.Timeline.Builder().addTimelineEntry(
                        TimelineBuilders.TimelineEntry.Builder().setLayout(
                            LayoutElementBuilders.Layout.Builder().setRoot(tileLayout()).build()
                        ).build()
                    ).build()
                ).build()
        )
    }


    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }


    private fun tileLayout(): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Box.Builder()
            .setWidth(wrap())
            .setHeight(wrap())
            .addContent(
                Text.Builder(this, "Open Lock Phone")
                    .setColor(argb(0xFFFFFFFF.toInt()))
                    .setWeight(FONT_WEIGHT_MEDIUM)
                    .setTypography(Typography.TYPOGRAPHY_BUTTON)
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setPadding(Padding.Builder().setAll(dp(12f)).build())
                            .setBackground(Background.Builder()
                                .setCorner(Corner.Builder().setRadius(dp(26f)).build())
                                .setColor(argb(Purple200.toArgb()))
                                .build()
                            )
                            .setClickable(Clickable.Builder()
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
            ).build()
    }
}