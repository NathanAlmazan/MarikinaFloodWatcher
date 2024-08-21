package com.floodalert.disafeter

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.floodalert.disafeter.MainActivity.Companion.CHANNEL_ID
import com.floodalert.disafeter.model.DbCollections
import com.floodalert.disafeter.model.FloodData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.NumberFormat


class NotificationService: Service() {
    private var previousLevel: Int = 0

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val extras = intent?.extras
        val temperature = extras?.get("temperature") as Double?
        val description = extras?.get("description") as String?

        startForeground(NOTIFICATION_ID, buildNotification(0,0, temperature, description))

        val db = Firebase.firestore
        val query = db.collection(DbCollections.WEATHER.db)
            .document("floodData")

        query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                stopSelf()
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val floodData = snapshot.toObject<FloodData>()
                if (floodData != null) {
                    if (floodData.floodLevel.toInt() != previousLevel) {
                        previousLevel = floodData.floodLevel.toInt()

                        updateNotification(
                            floodData.precipitation.toInt(),
                            floodData.floodLevel.toInt(),
                            temperature,
                            description
                        )
                    }

                    if (floodData.announcement.isNotEmpty()) {
                        with(NotificationManagerCompat.from(this)) {
                            notify(BULLETIN_NOTIFICATION_ID, buildBulletinNotification(floodData.announcement))
                        }
                    }
                }
            }
        }

        return START_STICKY
    }

    private fun buildNotification(rain: Int, floodLevel: Int, temp: Double? = 0.0, desc: String? = ""): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val descCapitalized = desc?.split(" ")?.joinToString(" ") {
            it.replaceFirstChar { char -> char.uppercaseChar() }
        }

        val numberFormat = NumberFormat.getInstance()
        numberFormat.maximumFractionDigits = 1

        val tempInCelsius = if (temp != null) temp - 273.15 else 0.0
        val tempInString = numberFormat.format(tempInCelsius)
        val title = if (floodLevel > 13) "River level reached $floodLevel meters!" else "$tempInString°C $descCapitalized"
        val content = if (floodLevel > 13) "View evacuation centers and emergency hotlines" else "View river level and announcements"
        val icon = if (floodLevel > 13) R.drawable.ic_home_flood else R.drawable.ic_weather_partly_cloudy

        if (floodLevel > 13) return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .build()
    }

    private fun buildBulletinNotification(title: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("Open app to see more details.")
            .setSmallIcon(R.drawable.ic_baseline_announcement_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .build()
    }

    private fun updateNotification(rain: Int, floodLevel: Int, temp: Double? = 0.0, desc: String? = "") {
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, buildNotification(rain, floodLevel, temp, desc))
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val BULLETIN_NOTIFICATION_ID = 2
    }
}