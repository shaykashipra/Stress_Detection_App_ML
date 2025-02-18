//package com.example.stress_detection_app;
//
//import android.Manifest;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.pm.PackageManager;
//import android.media.AudioManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.telephony.PhoneStateListener;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.stress_detection_app.Listener.ScreenStatusReceiver;
//
//public class PhoneRingerActivity extends AppCompatActivity {
//    private TextView phoneCallStatusTextView, ringerModeTextView, screenStatusTextView;
//    private TelephonyManager telephonyManager;
//    private AudioManager audioManager;
//    private BroadcastReceiver ringerModeReceiver;
//    private ScreenStatusReceiver screenStatusReceiver;
//
//    private static final int PERMISSION_REQUEST_CODE = 1;
//    private static final int UPDATE_INTERVAL_MS = 3000; // 3 seconds
//
//    private Handler handler = new Handler();
//    private Runnable statusUpdateRunnable;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_phone_ringer);
//
//        // Initialize TextViews
//        phoneCallStatusTextView = findViewById(R.id.phoneCallStatusTextView);
//        ringerModeTextView = findViewById(R.id.ringerModeTextView);
//        screenStatusTextView = findViewById(R.id.screenStatusTextView); // New TextView for screen status
//
//        // Get system services with try-catch
//        try {
//            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        } catch (Exception e) {
//            Toast.makeText(this, "Error accessing system services", Toast.LENGTH_SHORT).show();
//            Log.e("PhoneRingerActivity", "Error accessing system services", e);
//        }
//
//        // Check and request permissions
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            startPhoneCallStatusListener();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
//        }
//
//        // Start listening for ringer mode and screen status changes
//        startRingerModeListener();
//        startScreenStatusListener();
//
//        // Start periodic status updates
//        setupStatusUpdater();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // Unregister the BroadcastReceivers when the activity is destroyed
//        if (ringerModeReceiver != null) {
//            unregisterReceiver(ringerModeReceiver);
//        }
//        if (screenStatusReceiver != null) {
//            unregisterReceiver(screenStatusReceiver);
//        }
//        // Stop periodic updates
//        handler.removeCallbacks(statusUpdateRunnable);
//    }
//
//    // Handle permission result
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startPhoneCallStatusListener();
//            } else {
//                phoneCallStatusTextView.setText("Permission denied to access phone state.");
//            }
//        }
//    }
//
//    // Periodic status updater setup
//    private void setupStatusUpdater() {
//        statusUpdateRunnable = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    updateRingerMode();
//                } catch (Exception e) {
//                    Log.e("PhoneRingerActivity", "Error updating ringer mode", e);
//                }
//                handler.postDelayed(this, UPDATE_INTERVAL_MS); // Update every 3 seconds
//            }
//        };
//        handler.post(statusUpdateRunnable);
//    }
//
//    // Phone Call Status Listener (real-time updates with try-catch)
//    private void startPhoneCallStatusListener() {
//        try {
//            telephonyManager.listen(new PhoneStateListener() {
//                @Override
//                public void onCallStateChanged(int state, String phoneNumber) {
//                    try {
//                        switch (state) {
//                            case TelephonyManager.CALL_STATE_IDLE:
//                                phoneCallStatusTextView.setText("Phone Status: Not in a call");
//                                break;
//                            case TelephonyManager.CALL_STATE_RINGING:
//                                phoneCallStatusTextView.setText("Phone Status: Incoming call ringing");
//                                break;
//                            case TelephonyManager.CALL_STATE_OFFHOOK:
//                                phoneCallStatusTextView.setText("Phone Status: In a call");
//                                break;
//                            default:
//                                phoneCallStatusTextView.setText("Phone Status: Unknown");
//                                break;
//                        }
//                    } catch (Exception e) {
//                        Log.e("PhoneRingerActivity", "Error updating call state", e);
//                    }
//                }
//            }, PhoneStateListener.LISTEN_CALL_STATE);
//        } catch (SecurityException e) {
//            Toast.makeText(this, "Permission denied to access phone state.", Toast.LENGTH_SHORT).show();
//            Log.e("PhoneRingerActivity", "Error starting call status listener", e);
//        }
//    }
//
//    // Ringer Mode Listener (real-time updates with try-catch)
//    private void startRingerModeListener() {
//        try {
//            ringerModeReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    try {
//                        updateRingerMode();
//                    } catch (Exception e) {
//                        Log.e("PhoneRingerActivity", "Error updating ringer mode on broadcast", e);
//                    }
//                }
//            };
//            IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
//            registerReceiver(ringerModeReceiver, filter);
//            updateRingerMode();
//        } catch (Exception e) {
//            Toast.makeText(this, "Error setting up ringer mode listener", Toast.LENGTH_SHORT).show();
//            Log.e("PhoneRingerActivity", "Error registering ringer mode receiver", e);
//        }
//    }
//
//    // Screen Status Listener with try-catch
//    private void startScreenStatusListener() {
//        try {
//            screenStatusReceiver = new ScreenStatusReceiver(this);
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(Intent.ACTION_SCREEN_ON);
//            filter.addAction(Intent.ACTION_SCREEN_OFF);
//            registerReceiver(screenStatusReceiver, filter);
//        } catch (Exception e) {
//            Toast.makeText(this, "Error setting up screen status listener", Toast.LENGTH_SHORT).show();
//            Log.e("PhoneRingerActivity", "Error registering screen status receiver", e);
//        }
//    }
//
//    // Update the ringer mode status with try-catch
//    private void updateRingerMode() {
//        try {
//            int ringerMode = audioManager.getRingerMode();
//            switch (ringerMode) {
//                case AudioManager.RINGER_MODE_SILENT:
//                    ringerModeTextView.setText("Ringer Mode: Silent");
//                    break;
//                case AudioManager.RINGER_MODE_VIBRATE:
//                    ringerModeTextView.setText("Ringer Mode: Vibrate");
//                    break;
//                case AudioManager.RINGER_MODE_NORMAL:
//                    ringerModeTextView.setText("Ringer Mode: Loud");
//                    break;
//                default:
//                    ringerModeTextView.setText("Ringer Mode: Unknown");
//                    break;
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Error updating ringer mode", Toast.LENGTH_SHORT).show();
//            Log.e("PhoneRingerActivity", "Error updating ringer mode", e);
//        }
//    }
//
//    // Method to update screen status
//    public void updateScreenStatus(boolean isScreenOn) {
//        try {
//            if (isScreenOn) {
//                screenStatusTextView.setText("Screen Status: ON");
//            } else {
//                screenStatusTextView.setText("Screen Status: OFF");
//            }
//        } catch (Exception e) {
//            Log.e("PhoneRingerActivity", "Error updating screen status", e);
//        }
//    }
//}
//package com.example.stress_detection_app;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//
//import com.example.stress_detection_app.Services.PhoneRingerService;
//
//public class MainActivity extends AppCompatActivity {
//
//    private Button startButton, stopButton;
//    private CardView phoneRingerCard;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        startButton = findViewById(R.id.startButton);
//        stopButton = findViewById(R.id.stopButton);
//        phoneRingerCard = findViewById(R.id.phoneRingerCard);
//
//        startButton.setOnClickListener(v -> startService(new Intent(this, PhoneRingerService.class)));
//        stopButton.setOnClickListener(v -> stopService(new Intent(this, PhoneRingerService.class)));
//
//        phoneRingerCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PhoneRingerActivity.class)));
//    }
//}
package com.example.stress_detection_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stress_detection_app.Services.PhoneRingerService;

public class PhoneRingerActivity extends AppCompatActivity {

    private TextView phoneCallStatusTextView, ringerModeTextView, screenStatusTextView;

    private BroadcastReceiver phoneDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ringerModeStatus = intent.getStringExtra("ringerModeStatus");
            String screenStatus = intent.getStringExtra("screenStatus");
            String phoneCallStatus = intent.getStringExtra("phoneCallStatus");


            phoneCallStatusTextView.setText("Phone Call Status: " + phoneCallStatus);
            ringerModeTextView.setText("Ringer Mode: " + ringerModeStatus);
            screenStatusTextView.setText("Screen Status: " + screenStatus);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_ringer);

        phoneCallStatusTextView = findViewById(R.id.phoneCallStatusTextView);
        ringerModeTextView = findViewById(R.id.ringerModeTextView);
        screenStatusTextView = findViewById(R.id.screenStatusTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register receiver to listen for updates from PhoneRingerService
        registerReceiver(phoneDataReceiver, new IntentFilter(PhoneRingerService.ACTION_PHONE_UPDATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receiver to prevent memory leaks
        unregisterReceiver(phoneDataReceiver);
    }
}

