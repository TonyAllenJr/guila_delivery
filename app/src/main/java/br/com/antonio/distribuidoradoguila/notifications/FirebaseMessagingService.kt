package br.com.antonio.distribuidoradoguila.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.antonio.distribuidoradoguila.R
import br.com.antonio.distribuidoradoguila.ui.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationAdmin: String = "notification_channel"

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) run {
            val nome = "Principal"
            val descricao = "Notificações ao administrador"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(notificationAdmin, nome, importancia)
            notificationChannel.description = descricao

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(applicationContext, notificationAdmin)
        builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(data["body"])
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1, builder.build())

    }

}