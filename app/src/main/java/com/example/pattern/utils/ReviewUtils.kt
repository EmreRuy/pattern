package com.example.pattern.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.core.net.toUri

object ReviewUtils {

    /**
     * Launches the In-App Review flow. 
     * If the quota is reached or the API fails, it falls back to redirecting to the Play Store.
     */
    fun launchInAppReview(context: Context, scope: CoroutineScope) {
        val manager = ReviewManagerFactory.create(context)
        val activity = context.findActivity()

        if (activity == null) {
            redirectToPlayStore(context)
            return
        }

        scope.launch(Dispatchers.Main) {
            try {
                // Request the review info object
                val reviewInfo = manager.requestReview()
                // Launch the review flow
                manager.launchReview(activity, reviewInfo)
            } catch (e: Exception) {
                // Fallback to Play Store on any error (e.g., no Play Services, quota reached)
                redirectToPlayStore(context)
            }
        }
    }

    /**
     * Opens the Play Store page for the app.
     */
    fun redirectToPlayStore(context: Context) {
        val packageName = context.packageName
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=$packageName".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for browsers or if Play Store app is missing
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://play.google.com/store/apps/details?id=$packageName".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Helper function to find the Activity from a Context, unwrapping if necessary.
     */
    private fun Context.findActivity(): Activity? {
        var currentContext = this
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }
}
