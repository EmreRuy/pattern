package com.example.pattern.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ReviewUtils {
    fun launchInAppReview(context: Context, scope: CoroutineScope) {
        val manager = ReviewManagerFactory.create(context)

        scope.launch(Dispatchers.Main) {
            try {
                val reviewInfo = manager.requestReview()
                val activity = context as? Activity
                if (activity != null) {
                    manager.launchReview(activity, reviewInfo)
                } else {
                    redirectToPlayStore(context)
                }
            } catch (_: Exception) {
                // If the API fails or quota is met, fallback gracefully
                redirectToPlayStore(context)
            }
        }
    }

    private fun redirectToPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "market://details?id=$packageName".toUri()
                )
            )
        } catch (_: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$packageName".toUri()
                )
            )
        }
    }
}