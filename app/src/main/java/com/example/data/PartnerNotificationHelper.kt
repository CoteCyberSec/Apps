package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AppLanguage

object PartnerNotificationHelper {
    private const val CHANNEL_ID = "duet_partner_updates"

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannel(context: Context, language: AppLanguage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (language == AppLanguage.RU) "Ответы партнёра" else "Partner Answers"
            val descriptionText = if (language == AppLanguage.RU) {
                "Уведомления о новых ответах партнёра на вопросы"
            } else {
                "Alerts when your partner answers a topic question"
            }
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = descriptionText
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun notifyPartnerAnswer(
        context: Context,
        answerId: String,
        partnerName: String,
        questionText: String,
        language: AppLanguage
    ) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context, language)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            answerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (language == AppLanguage.RU) {
            "$partnerName ответил(а) на вопрос"
        } else {
            "$partnerName answered a question"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(questionText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(questionText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(answerId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission revoked mid-flight; in-app bottom tray still delivers the update
        }
    }
}
