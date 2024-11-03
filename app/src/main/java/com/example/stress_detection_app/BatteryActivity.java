package com.example.stress_detection_app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BatteryActivity extends AppCompatActivity {

    private TextView batteryStatusTextView, dndStatusTextView;
    private NotificationManager notificationManager;
    private Handler handler = new Handler();
    private Runnable checkDndStatusRunnable;

    private static final int CHECK_INTERVAL_MS = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);

        batteryStatusTextView = findViewById(R.id.batteryStatusTextView);
        dndStatusTextView = findViewById(R.id.dndStatusTextView);

        // Initialize NotificationManager
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            // Register to receive battery status updates
            registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception e) {
            Toast.makeText(this, "Battery sensor not available.", Toast.LENGTH_LONG).show();
            batteryStatusTextView.setText("Battery: Sensor not available");
            e.printStackTrace();
        }

        // Set up the DND status check runnable
        setupDndStatusChecker();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start periodic DND status checks when the activity is visible
        handler.post(checkDndStatusRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop DND status checks when the activity goes into the background
        handler.removeCallbacks(checkDndStatusRunnable);
    }

    // BroadcastReceiver to listen to battery status changes
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                float batteryPct = (level / (float) scale) * 100;
                String statusString;

                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = "Charging";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = "Discharging";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = "Full";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = "Not Charging";
                        break;
                    default:
                        statusString = "Unknown";
                        break;
                }

                // Update the UI with the current battery level and status
                batteryStatusTextView.setText("Battery: " + (int) batteryPct + "%, " + statusString);
            } catch (Exception e) {
                Toast.makeText(context, "Error reading battery data.", Toast.LENGTH_SHORT).show();
                batteryStatusTextView.setText("Battery: Error reading data");
                e.printStackTrace();
            }
        }
    };

    // Setup periodic DND status checker
    private void setupDndStatusChecker() {
        checkDndStatusRunnable = new Runnable() {
            @Override
            public void run() {
                checkDoNotDisturbMode();
                handler.postDelayed(this, CHECK_INTERVAL_MS); // Re-check every 3 seconds
            }
        };
    }

    // Check Do Not Disturb (DND) status
    private void checkDoNotDisturbMode() {
        try {
            if (notificationManager != null) {
                // Check if the app has permission to access notification policy
                if (!notificationManager.isNotificationPolicyAccessGranted()) {
                    // Request permission if not granted
                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, "Please grant Do Not Disturb access.", Toast.LENGTH_LONG).show();
                } else {
                    // Check DND status
                    int filter = notificationManager.getCurrentInterruptionFilter();
                    if (filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                            filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                            filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                        dndStatusTextView.setText("Do Not Disturb: Activated");
                    } else {
                        dndStatusTextView.setText("Do Not Disturb: Deactivated");
                    }
                }
            } else {
                dndStatusTextView.setText("Do Not Disturb: Unknown");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error checking Do Not Disturb status.", Toast.LENGTH_SHORT).show();
            dndStatusTextView.setText("Do Not Disturb: Error");
            e.printStackTrace();
        }
    }
}
