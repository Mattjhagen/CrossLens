package com.crosslens.app.core.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.crosslens.app.R

/** External reporting stays with its publisher; never accept executable/custom URI schemes. */
object PublisherLinks {
    fun open(context: Context, url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull()
        if (uri?.scheme != "https" || uri.host.isNullOrBlank() || uri.userInfo != null) {
            return unavailable(context)
        }
        try {
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
            return true
        } catch (_: android.content.ActivityNotFoundException) {
            // Some devices have a browser without Custom Tabs support.
        } catch (_: SecurityException) {
            return unavailable(context)
        }
        return try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (_: android.content.ActivityNotFoundException) {
            unavailable(context)
        } catch (_: SecurityException) {
            unavailable(context)
        }
    }

    private fun unavailable(context: Context): Boolean {
        Toast.makeText(context, R.string.browser_unavailable, Toast.LENGTH_LONG).show()
        return false
    }
}
