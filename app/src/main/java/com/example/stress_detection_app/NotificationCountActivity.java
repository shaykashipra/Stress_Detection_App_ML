package com.example.stress_detection_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stress_detection_app.Listener.NotificationListener;

import java.util.Map;

public class NotificationCountActivity extends AppCompatActivity {
    private TextView notificationCountTextView, notificationSourcesTextView;
    private Handler handler = new Handler();
    private Runnable updateNotificationCountRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_count);

        notificationCountTextView = findViewById(R.id.notificationCountTextView);
        notificationSourcesTextView = findViewById(R.id.notificationSourcesTextView);

        setupNotificationCountChecker();
        requestNotificationListenerPermission();
    }

    private void setupNotificationCountChecker() {
        updateNotificationCountRunnable = new Runnable() {
            @Override
            public void run() {
                int count = NotificationListener.notificationCount; // Get total count
                notificationCountTextView.setText("Total Notifications: " + count);

                // Display source apps and their counts
                StringBuilder sources = new StringBuilder("Sources:\n");
                for (Map.Entry<String, Integer> entry : NotificationListener.notificationSources.entrySet()) {
                    sources.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                notificationSourcesTextView.setText(sources.toString());

                // Schedule the next update
                handler.postDelayed(this, 10000); // Update every 10 seconds
            }
        };
        handler.post(updateNotificationCountRunnable);
    }

    private void requestNotificationListenerPermission() {
        // Prompt user to enable notification listener
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateNotificationCountRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateNotificationCountRunnable);
    }
}
