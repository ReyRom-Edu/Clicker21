package com.example.clicker21

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

object NotificationHelper {
    private const val CHANNEL_ID = "offline_earnings_channel"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Оффлайн доход",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о заработке в оффлайне"
        }
        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendNotification(context: Context, earnings: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Важно для Android 12+
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Вы заработали оффлайн!")
            .setContentText("За время отсутствия вы заработали $earnings очков.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Добавляем переход на MainActivity
            .setAutoCancel(true) // Убирает уведомление при нажатии
            .build()

        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }
}

class OfflineEarningsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val gamePreferences = GameStorage(applicationContext) // Загружаем настройки
        val lastExitTime = gamePreferences.getExitTime()
        val currentTime = System.currentTimeMillis()

        val elapsedTime = (currentTime - lastExitTime) / 1000 // Время в секундах

        val offlineBonusUpgrade = gamePreferences.getUpgrages()
            .filterIsInstance<OfflineEarningsUpgrade>()
            .firstOrNull()

        val autoclickUpgrade = gamePreferences.getUpgrages()
            .filterIsInstance<AutoclickUpgrade>()
            .firstOrNull()

        if (autoclickUpgrade != null && offlineBonusUpgrade != null){
            if (autoclickUpgrade.clicksPerSecond * elapsedTime.toBigDecimal() > offlineBonusUpgrade.offlineCap) {
                NotificationHelper.sendNotification(applicationContext, offlineBonusUpgrade.offlineCap.formatNumber())
                return Result.success()
            }
        }
        return Result.retry()
    }
}