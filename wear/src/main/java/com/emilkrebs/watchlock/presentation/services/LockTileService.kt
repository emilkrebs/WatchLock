package com.emilkrebs.watchlock.presentation.services

import com.emilkrebs.watchlock.R
import androidx.compose.ui.graphics.toArgb
import androidx.wear.tiles.*
import androidx.wear.tiles.DimensionBuilders.dp
import androidx.wear.tiles.LayoutElementBuilders.Spacer
import androidx.wear.tiles.ModifiersBuilders.Clickable
import androidx.wear.tiles.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.material.Text
import androidx.wear.tiles.material.Typography
import com.emilkrebs.watchlock.presentation.MainActivity
import com.emilkrebs.watchlock.presentation.theme.Purple200
import com.emilkrebs.watchlock.presentation.theme.Purple500
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture


const val RESOURCES_VERSION = "1"
const val ID_CLICK_REFRESH = "click_refresh"


class LockTileService : TileService() {
    private lateinit var phoneCommunicationService: PhoneCommunicationService;
    private var isLoading: Boolean = false
    private var lockStatus: LockStatus = LockStatus.UNKNOWN
    override fun onCreate() {
        super.onCreate()
        phoneCommunicationService = PhoneCommunicationService(this)
    }

    override fun onTileEnterEvent(requestParams: EventBuilders.TileEnterEvent) {
        super.onTileEnterEvent(requestParams)
        // request the lock status from the phone
        requestUpdate()
    }

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        // request the lock status from the phone
        if (requestParams.state?.lastClickableId == ID_CLICK_REFRESH) {
            requestUpdate()
        }
        if (isLoading) {
            return Futures.immediateFuture(
                TileBuilders.Tile.Builder()
                    .setResourcesVersion(RESOURCES_VERSION)
                    .setTimeline(
                        TimelineBuilders.Timeline.Builder().addTimelineEntry(
                            TimelineEntry.Builder()
                                .setLayout(
                                    LayoutElementBuilders.Layout.Builder().setRoot(loadingLayout())
                                        .build()
                                )
                                .build()
                        ).build()
                    ).build()
            )
        } else {
            return Futures.immediateFuture(
                TileBuilders.Tile.Builder()
                    .setResourcesVersion(RESOURCES_VERSION)
                    .setTimeline(
                        TimelineBuilders.Timeline.Builder().addTimelineEntry(
                            TimelineEntry.Builder()
                                .setLayout(
                                    LayoutElementBuilders.Layout.Builder().setRoot(mainLayout())
                                        .build()
                                )
                                .build()
                        ).build()
                    ).build()
            )
        }
    }


    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    private fun loadingLayout(): LayoutElementBuilders.LayoutElement {
        return LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.wrap())
            .setHeight(DimensionBuilders.wrap())
            .addContent(
                Text.Builder(this, "Loading...")
                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                    .setWeight(LayoutElementBuilders.FONT_WEIGHT_BOLD)
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .build()
            )
            .build()
    }

    private fun mainLayout(): LayoutElementBuilders.LayoutElement {
        val buttonContent: String = if (lockStatus == LockStatus.LOCKED) {
            getString(R.string.phone_locked)
        } else {
            getString(R.string.phone_unlocked)
        }
        val buttonColor: Int = if (lockStatus == LockStatus.LOCKED) {
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
                refreshButton()
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

    private fun refreshButton(): LayoutElementBuilders.LayoutElement {
        return Text.Builder(this, "Refresh")
            .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
            .setWeight(LayoutElementBuilders.FONT_WEIGHT_NORMAL)
            .setTypography(Typography.TYPOGRAPHY_BODY1)
            .setModifiers(
                ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        Clickable.Builder()
                            .setId(ID_CLICK_REFRESH)
                            .setOnClick(
                                // send message to phone to query lock status
                                ActionBuilders.LoadAction.Builder().build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun requestUpdate() {
        // proceed if not already loading
        if (!isLoading) {

            // now it is loading
            isLoading = true
            // request the lock status from the phone
            phoneCommunicationService.getLockStatus { lockStatus ->
                this.lockStatus = lockStatus
                // now it is not loading anymore
                isLoading = false

                // update the tile
                getUpdater(this).requestUpdate(LockTileService::class.java)
            }
        }

    }
}