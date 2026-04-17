package com.example.pattern.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri

object SupportUtils {
    fun sendSupportEmail(context: Context) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("uyar.em.eu@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Support Request: Pattern App")
            putExtra(
                Intent.EXTRA_TEXT,
                "\n\n--- Device Info ---\nModel: ${Build.MODEL}\nVersion: ${Build.VERSION.RELEASE}"
            )
        }
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send support email..."))
        } catch (_: Exception) {
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}