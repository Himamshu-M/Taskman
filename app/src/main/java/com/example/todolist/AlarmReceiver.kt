
package com.example.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val taskName = intent?.getStringExtra("EXTRA_TASK_NAME") ?: ""
        val taskDescription = intent?.getStringExtra("EXTRA_TASK_DESCRIPTION") ?: ""
        val alarmId = intent?.getIntExtra("EXTRA_ALARM_ID", 0) ?: 0

        if (context != null) {
            showNotification(context, taskName, taskDescription, alarmId)
        }
    }

    private fun showNotification(context: Context, taskName: String, taskDescription: String, alarmId: Int) {
        val channelId = "alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for alarm notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // You can change this icon
            .setContentTitle("Alarm: $taskName")
            .setContentText(taskDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(alarmId, builder.build())
    }
}

