//package com.example.stress_detection_app.Services;
//
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.media.AudioManager;
//import android.os.Handler;
//import android.os.IBinder;
//import android.telephony.PhoneStateListener;
//import android.telephony.TelephonyManager;
//
//public class PhoneRingerService extends Service {
//
//    public static final String ACTION_PHONE_UPDATE = "com.example.stress_detection_app.PHONE_UPDATE";
//    private TelephonyManager telephonyManager;
//    private AudioManager audioManager;
//    private BroadcastReceiver screenStatusReceiver;
//    private Handler handler = new Handler();
//    private Runnable statusUpdateRunnable;
//
//    private String phoneCallStatus = "Unknown";
//    private String ringerModeStatus = "Unknown";
//    private String screenStatus = "Unknown";
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//        startPhoneCallStatusListener();
//        startRingerModeListener();
//        startScreenStatusListener();
//
//        // Set up a periodic updater
//        statusUpdateRunnable = new Runnable() {
//            @Override
//            public void run() {
//                broadcastPhoneStatus();
//                handler.postDelayed(this, 3000); // Update every 3 seconds
//            }
//        };
//        handler.post(statusUpdateRunnable);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        handler.removeCallbacks(statusUpdateRunnable);
//        if (screenStatusReceiver != null) unregisterReceiver(screenStatusReceiver);
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    private void broadcastPhoneStatus() {
//        Intent broadcastIntent = new Intent(ACTION_PHONE_UPDATE);
//        broadcastIntent.putExtra("phoneCallStatus", phoneCallStatus);
//        broadcastIntent.putExtra("ringerModeStatus", ringerModeStatus);
//        broadcastIntent.putExtra("screenStatus", screenStatus);
//        sendBroadcast(broadcastIntent);
//    }
//
//    private void startPhoneCallStatusListener() {
//        telephonyManager.listen(new PhoneStateListener() {
//            @Override
//            public void onCallStateChanged(int state, String phoneNumber) {
//                switch (state) {
//                    case TelephonyManager.CALL_STATE_IDLE:
//                        phoneCallStatus = "Not in a call";
//                        break;
//                    case TelephonyManager.CALL_STATE_RINGING:
//                        phoneCallStatus = "Incoming call ringing";
//                        break;
//                    case TelephonyManager.CALL_STATE_OFFHOOK:
//                        phoneCallStatus = "In a call";
//                        break;
//                    default:
//                        phoneCallStatus = "Unknown";
//                        break;
//                }
//            }
//        }, PhoneStateListener.LISTEN_CALL_STATE);
//    }
//
//    private void startRingerModeListener() {
//        ringerModeStatus = getRingerModeString(audioManager.getRingerMode());
//        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                ringerModeStatus = getRingerModeString(audioManager.getRingerMode());
//            }
//        }, filter);
//    }
//
//    private String getRingerModeString(int ringerMode) {
//        switch (ringerMode) {
//            case AudioManager.RINGER_MODE_SILENT:
//                return "Silent";
//            case AudioManager.RINGER_MODE_VIBRATE:
//                return "Vibrate";
//            case AudioManager.RINGER_MODE_NORMAL:
//                return "Loud";
//            default:
//                return "Unknown";
//        }
//    }
//
//    private void startScreenStatusListener() {
//        screenStatusReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//                    screenStatus = "ON";
//                } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
//                    screenStatus = "OFF";
//                }
//            }
//        };
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(screenStatusReceiver, filter);
//    }
//}
package com.example.stress_detection_app.Services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class PhoneRingerService extends Service {

    public static final String ACTION_PHONE_UPDATE = "com.example.stress_detection_app.PHONE_UPDATE";
    private TelephonyManager telephonyManager;
    private AudioManager audioManager;
    private Handler handler = new Handler();
    private Runnable statusUpdateRunnable;

    private String phoneCallStatus = "Unknown";
    private String ringerModeStatus = "Unknown";
    private String screenStatus = "Unknown";

    @Override
    public void onCreate() {
        super.onCreate();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Check if READ_PHONE_STATE permission is granted before starting the listener
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            startPhoneCallStatusListener();
        } else {
            // Permission not granted, inform the user
            Toast.makeText(this, "Phone state permission not granted.", Toast.LENGTH_LONG).show();
            stopSelf(); // Stop the service if permission is not granted
            return;
        }

        startRingerModeListener();
        startScreenStatusListener();

        // Set up a periodic updater
        statusUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                broadcastPhoneStatus();
                handler.postDelayed(this, 3000); // Update every 3 seconds
            }
        };
        handler.post(statusUpdateRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(statusUpdateRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void broadcastPhoneStatus() {
        Intent broadcastIntent = new Intent(ACTION_PHONE_UPDATE);
        broadcastIntent.putExtra("phoneCallStatus", phoneCallStatus);
        broadcastIntent.putExtra("ringerModeStatus", ringerModeStatus);
        broadcastIntent.putExtra("screenStatus", screenStatus);
        sendBroadcast(broadcastIntent);
    }

    private void startPhoneCallStatusListener() {
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        phoneCallStatus = "Not in a call";
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        phoneCallStatus = "Incoming call ringing";
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        phoneCallStatus = "In a call";
                        break;
                    default:
                        phoneCallStatus = "Unknown";
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void startRingerModeListener() {
        ringerModeStatus = getRingerModeString(audioManager.getRingerMode());
        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ringerModeStatus = getRingerModeString(audioManager.getRingerMode());
            }
        }, filter);
    }

    private String getRingerModeString(int ringerMode) {
        switch (ringerMode) {
            case AudioManager.RINGER_MODE_SILENT:
                return "Silent";
            case AudioManager.RINGER_MODE_VIBRATE:
                return "Vibrate";
            case AudioManager.RINGER_MODE_NORMAL:
                return "Loud";
            default:
                return "Unknown";
        }
    }

    private void startScreenStatusListener() {
        BroadcastReceiver screenStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    screenStatus = "ON";
                } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    screenStatus = "OFF";
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStatusReceiver, filter);
    }
}
