package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirestoreNotificationManager(private val context: Context) {

    private var db: FirebaseFirestore? = null
    private val channelId = "matches_messages_channel"
    private var matchesListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null

    init {
        try {
            db = FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            Log.e("NotificationService", "Firebase not initialized", e)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Matches and Messages"
            val descriptionText = "Notifications for new matches and messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startListening(userId: String) {
        val database = db ?: return
        // Listen for new matches
        matchesListener = database.collection("users").document(userId).collection("matches")
            .whereEqualTo("isNew", true)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("NotificationService", "listen:error", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val matchName = dc.document.getString("name") ?: "Someone"
                        showNotification(
                            "New Match!",
                            "You have a new match with $matchName",
                            notificationId = dc.document.id.hashCode()
                        )
                    }
                }
            }

        // Listen for new messages
        messagesListener = database.collection("users").document(userId).collection("messages")
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("NotificationService", "listen:error", e)
                    return@addSnapshotListener
                }

                for (dc in snapshots!!.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val senderName = dc.document.getString("senderName") ?: "Someone"
                        val text = dc.document.getString("text") ?: "Sent you a message"
                        showNotification(
                            "New Message from $senderName",
                            text,
                            notificationId = dc.document.id.hashCode()
                        )
                    }
                }
            }
    }

    fun stopListening() {
        matchesListener?.remove()
        messagesListener?.remove()
    }

    private fun showNotification(title: String, content: String, notificationId: Int) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Try to use default launcher foreground or custom icon
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
