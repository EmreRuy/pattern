package com.example.pattern.utils

import android.content.Context
import android.content.Intent

object ShareUtils {
    fun shareApp(context: Context) {
        val appPackageName = context.packageName
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this awesome app: https://play.google.com/store/apps/details?id=$appPackageName"
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share via")
        context.startActivity(shareIntent)
    }
}