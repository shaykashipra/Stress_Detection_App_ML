package com.example.stress_detection_app.Listener;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NotificationListener extends NotificationListenerService {
    public static int notificationCount = 0; // Total notification count
    public static Map<String, Integer> notificationSources = new HashMap<>(); // Source app and their counts

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        notificationCount++;
        String packageName = sbn.getPackageName(); // Get the source app package name

        // Update the count for the source app
        notificationSources.put(packageName, notificationSources.getOrDefault(packageName, 0) + 1);
        Log.d("NotificationListener", "Notification received from: " + packageName + ", Total: " + notificationCount);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if necessary
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d("NotificationListener", "Listener connected");
    }
}
