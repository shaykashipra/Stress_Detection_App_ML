package com.example.stress_detection_app.Listener;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class NotificationListener extends NotificationListenerService {

    public static int notificationCount = 0;
    public static Map<String, Integer> notificationSources = new HashMap<>();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String packageName = sbn.getPackageName();
        Log.d("NotificationListener", "Notification posted from: " + packageName);

        // Update notification count and sources
        notificationCount++;
        notificationSources.put(packageName, notificationSources.getOrDefault(packageName, 0) + 1);

        Log.d("NotificationListener", "Notification count: " + notificationCount);
        Log.d("NotificationListener", "Notification sources: " + notificationSources);
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);

        // Optional: You can handle notification removal if needed
    }

    public static boolean isEnabled(Context context) {
        String enabledNotificationListeners = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                "enabled_notification_listeners"
        );
        String packageName = context.getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }
    public static void resetNotifications() {
        notificationCount = 0;
        notificationSources.clear();
    }

}
