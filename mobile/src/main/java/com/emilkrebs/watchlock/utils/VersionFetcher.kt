package com.emilkrebs.watchlock.utils

import android.content.Context
import android.util.JsonReader
import android.widget.Toast
import com.emilkrebs.watchlock.getVersionName
import java.net.URL

suspend fun fetchLatestVersion(context: Context) {
    // fetch the latest version from https://api.github.com/repos/emilkrebs/WatchLock/releases and compare it to the current version
    try {
        var latestVersion = ""
        URL("https://api.github.com/repos/emilkrebs/WatchLock/releases").openStream()
            .use { stream ->
                JsonReader(stream.reader().buffered()).use { reader ->
                    reader.beginArray()
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val name = reader.nextName()
                        if (name == "tag_name") {
                            latestVersion = reader.nextString()
                            break
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endObject()
                    reader.endArray()
                }
            }

        if (latestVersion.isNotEmpty()) {
            // compare the versions
            val currentVersion = getVersionName(context)
            if (latestVersion != currentVersion) {
                // show a dialog to the user that a new version is available
                Toast.makeText(
                    context,
                    "A new version is available: $latestVersion",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    } catch (e: Exception) {
        // show a dialog to the user that the version check failed
        println(e)
        Toast.makeText(
            context,
            "There was an error while fetching the latest version.",
            Toast.LENGTH_LONG
        )
            .show()
    }
}