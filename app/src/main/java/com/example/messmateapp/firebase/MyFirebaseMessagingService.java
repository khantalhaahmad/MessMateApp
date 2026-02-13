package com.example.messmateapp.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.messmateapp.R;
import com.example.messmateapp.ui.home.HomeActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "messmate_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d("FCM_TOKEN", "New Token: " + token);

        // 🔥 TODO: Send this token to backend API
        // sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title = "MessMate";
        String body = "New Notification";

        // ✅ 1. Notification payload
        if (message.getNotification() != null) {

            if (message.getNotification().getTitle() != null)
                title = message.getNotification().getTitle();

            if (message.getNotification().getBody() != null)
                body = message.getNotification().getBody();
        }

        // ✅ 2. Data payload support (Important for backend FCM)
        Map<String, String> data = message.getData();

        if (data.size() > 0) {

            if (data.get("title") != null)
                title = data.get("title");

            if (data.get("body") != null)
                body = data.get("body");
        }

        showNotification(title, body);
    }

    private void showNotification(String title, String message) {

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // ✅ Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "MessMate Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("Order updates and alerts");
            manager.createNotificationChannel(channel);
        }

        // ✅ Open HomeActivity on click
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher) // ⚠ Use white icon ideally
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        // ✅ Unique notification ID (won't overwrite old one)
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
