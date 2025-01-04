package com.emilkrebs.watchlock.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper
import android.view.Surface

class ScreenCaptureHelper(private val context: Context) {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var screenCaptureCallback: ScreenCaptureCallback? = null

    interface ScreenCaptureCallback {
        fun onScreenCapture(bitmap: Bitmap?)
    }

    fun startScreenCapture(mediaProjection: MediaProjection) {
        this.mediaProjection = mediaProjection

        val displayMetrics = context.resources.displayMetrics
        val screenDensity = displayMetrics.densityDpi
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2)
        val surface: Surface = imageReader!!.surface

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            Handler(Looper.getMainLooper())
        )

        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * screenWidth

                val bitmap = Bitmap.createBitmap(
                    screenWidth + rowPadding / pixelStride,
                    screenHeight,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()

                screenCaptureCallback?.onScreenCapture(bitmap)
            }
        }, Handler(Looper.getMainLooper()))
    }

    fun stopScreenCapture() {
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
        imageReader?.close()
        imageReader = null
    }

    fun setScreenCaptureCallback(callback: ScreenCaptureCallback) {
        this.screenCaptureCallback = callback
    }
}

fun captureScreen(context: Context, mediaProjection: MediaProjection, callback: (Bitmap?) -> Unit) {
    val screenCaptureHelper = ScreenCaptureHelper(context)
    screenCaptureHelper.setScreenCaptureCallback(object : ScreenCaptureHelper.ScreenCaptureCallback {
        override fun onScreenCapture(bitmap: Bitmap?) {
            callback(bitmap)
            screenCaptureHelper.stopScreenCapture()
        }
    })
    screenCaptureHelper.startScreenCapture(mediaProjection)
}