//package com.example.stress_detection_app.Services;
//
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.BatteryManager;
//import android.os.Handler;
//import android.os.IBinder;
//
//public class BatteryService extends Service {
//
//    public static final String ACTION_BATTERY_UPDATE = "com.example.stress_detection_app.BATTERY_UPDATE";
//    private NotificationManager notificationManager;
//    private Handler handler = new Handler();
//    private Runnable runnable;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                broadcastBatteryAndDndStatus();
//                handler.postDelayed(this, 3000); // Run every 3 seconds
//            }
//        };
//        handler.post(runnable); // Start the runnable
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY; // Service will continue running
//    }
//
//    @Override
//    public void onDestroy() {
//        handler.removeCallbacks(runnable); // Stop updates
//        super.onDestroy();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    private void broadcastBatteryAndDndStatus() {
//        Intent batteryStatusIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//        int batteryLevel = batteryStatusIntent != null
//                ? (int) (100 * (batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) /
//                (float) batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)))
//                : -1;
//
//        // Battery status as string (charging, discharging, etc.)
//        int batteryStatus = batteryStatusIntent != null
//                ? batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
//                : -1;
//
//        String statusString;
//        switch (batteryStatus) {
//            case BatteryManager.BATTERY_STATUS_CHARGING:
//                statusString = "Charging";
//                break;
//            case BatteryManager.BATTERY_STATUS_DISCHARGING:
//                statusString = "Discharging";
//                break;
//            case BatteryManager.BATTERY_STATUS_FULL:
//                statusString = "Full";
//                break;
//            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
//                statusString = "Not Charging";
//                break;
//            default:
//                statusString = "Unknown";
//                break;
//        }
//
//        // Check DND status
//        String dndStatus = "Unknown";
//        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
//            int filter = notificationManager.getCurrentInterruptionFilter();
//            dndStatus = (filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
//                    filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
//                    filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ? "Activated" : "Deactivated";
//        }
//
//        // Broadcast the battery level, status, and DND status
//        Intent broadcastIntent = new Intent(ACTION_BATTERY_UPDATE);
//        broadcastIntent.putExtra("batteryLevel", batteryLevel);
//        broadcastIntent.putExtra("batteryStatus", statusString);
//        broadcastIntent.putExtra("dndStatus", dndStatus);
//        sendBroadcast(broadcastIntent);
//    }
//}
//package com.example.stress_detection_app.Services;
//
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.BatteryManager;
//import android.os.Handler;
//import android.os.IBinder;
//
//public class BatteryService extends Service {
//
//    public static final String ACTION_BATTERY_UPDATE = "com.example.stress_detection_app.BATTERY_UPDATE";
//    private NotificationManager notificationManager;
//    private Handler handler = new Handler();
//    private Runnable runnable;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                broadcastBatteryAndDndStatus();
//                handler.postDelayed(this, 3000); // Run every 3 seconds
//            }
//        };
//        handler.post(runnable); // Start the runnable
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY; // Service will continue running
//    }
//
//    @Override
//    public void onDestroy() {
//        handler.removeCallbacks(runnable); // Stop updates
//        super.onDestroy();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    private void broadcastBatteryAndDndStatus() {
//        Intent batteryStatusIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//        int batteryLevel = batteryStatusIntent != null
//                ? (int) (100 * (batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) /
//                (float) batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)))
//                : -1;
//
//        int batteryStatus = batteryStatusIntent != null
//                ? batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
//                : -1;
//
//        String statusString;
//        switch (batteryStatus) {
//            case BatteryManager.BATTERY_STATUS_CHARGING:
//                statusString = "Charging";
//                break;
//            case BatteryManager.BATTERY_STATUS_DISCHARGING:
//                statusString = "Discharging";
//                break;
//            case BatteryManager.BATTERY_STATUS_FULL:
//                statusString = "Full";
//                break;
//            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
//                statusString = "Not Charging";
//                break;
//            default:
//                statusString = "Unknown";
//                break;
//        }
//
//        String dndStatus = "Unknown";
//        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
//            int filter = notificationManager.getCurrentInterruptionFilter();
//            dndStatus = (filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
//                    filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
//                    filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ? "Activated" : "Deactivated";
//        }
//
//        Intent broadcastIntent = new Intent(ACTION_BATTERY_UPDATE);
//        broadcastIntent.putExtra("batteryLevel", batteryLevel);
//        broadcastIntent.putExtra("batteryStatus", statusString);
//        broadcastIntent.putExtra("dndStatus", dndStatus);
//        sendBroadcast(broadcastIntent);
//    }
//}
package com.example.stress_detection_app.Services;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class BatteryService extends Service {

    public static final String ACTION_BATTERY_UPDATE = "com.example.stress_detection_app.BATTERY_UPDATE";
    private NotificationManager notificationManager;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if DND access is granted
        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(this, "Please grant Do Not Disturb access.", Toast.LENGTH_LONG).show();
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                broadcastBatteryAndDndStatus();
                handler.postDelayed(this, 3000); // Run every 3 seconds
            }
        };
        handler.post(runnable); // Start the runnable
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Service will continue running
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable); // Stop updates
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void broadcastBatteryAndDndStatus() {

        Intent batteryStatusIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int batteryLevel = batteryStatusIntent != null
                ? (int) (100 * (batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) /
                (float) batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)))
                : -1;

        int batteryStatus = batteryStatusIntent != null
                ? batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                : -1;

        String statusString;
        switch (batteryStatus) {
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

        // Check DND status
        String dndStatus = "Unknown";
        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
            int filter = notificationManager.getCurrentInterruptionFilter();
            dndStatus = (filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                    filter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                    filter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) ? "Activated" : "Deactivated";
        }

        Intent broadcastIntent = new Intent(ACTION_BATTERY_UPDATE);
        broadcastIntent.putExtra("batteryLevel", batteryLevel);
        broadcastIntent.putExtra("batteryStatus", statusString);
        broadcastIntent.putExtra("dndStatus", dndStatus);
        sendBroadcast(broadcastIntent);
    }
}
