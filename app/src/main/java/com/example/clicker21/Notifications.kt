package com.example.clicker21

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat


object NotificationHelper{
    private const val CHANNEL_ID = "offline_earnings_channel"

    fun createNotificationChannel(context: Context){
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Оффлайн доход",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о заработке в оффлайн"
        }

        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun sendNotification(context: Context, earnings:String){
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_info_24)
            .setContentTitle("Оффлайн заработок!")
            .setContentText("Скорее заходи, лимит хранилища заполнен! Доход: $earnings")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }
}

//class OfflineEarningsWorker(
//    context: Context,
//    params: WorkerParameters
//)