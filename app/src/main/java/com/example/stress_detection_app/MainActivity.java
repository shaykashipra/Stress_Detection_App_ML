//package com.example.stress_detection_app;
//
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.cardview.widget.CardView;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//public class MainActivity extends AppCompatActivity {
//    private CardView notificationcountcard, batteryCard, phoneringerCard, sensorCard, soundCard;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//
//            // Initialize the CardViews
//            notificationcountcard = findViewById(R.id.NotificationCountCard);
//            batteryCard = findViewById(R.id.batteryCard);
//        phoneringerCard = findViewById(R.id.phoneringerCard);
//            sensorCard = findViewById(R.id.sensorCard);
//         soundCard = findViewById(R.id.soundCard);
//
//
//        // Set click listeners for each CardView
//            notificationcountcard.setOnClickListener(v -> {
//                Intent intent = new Intent(MainActivity.this, NotificationCountActivity.class);
//                startActivity(intent);
//            });
//
//            batteryCard.setOnClickListener(v -> {
//                Intent intent = new Intent(MainActivity.this, BatteryActivity.class);
//                startActivity(intent);
//            });
//
//        phoneringerCard.setOnClickListener(v -> {
//                Intent intent = new Intent(MainActivity.this, PhoneRingerActivity.class);
//                startActivity(intent);
//            });
////
//            sensorCard.setOnClickListener(v -> {
//                Intent intent = new Intent(MainActivity.this, SensorActivity.class);
//                startActivity(intent);
//            });
//        soundCard.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, SoundLevelActivity.class);
//            startActivity(intent);
//        });
//        }
//
//
//    }
//
//
package com.example.stress_detection_app;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListUtil;

import com.example.stress_detection_app.Listener.NotificationListener;
import com.example.stress_detection_app.Model.StressDetectionData;
import com.example.stress_detection_app.Services.BatteryService;
import com.example.stress_detection_app.Services.PhoneRingerService;
import com.example.stress_detection_app.Services.SensorBackgroundService;
import com.example.stress_detection_app.Services.SoundLevelService;
import com.example.stress_detection_app.Services.UnifiedService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Map<String, Object> batteryData = new HashMap<>();
    private Handler handler = new Handler();
    private Runnable dataCollectionTask,dataUploadRunnable;
    private static final int INTERVAL = 3000; // 3 seconds
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button startButton, stopButton,logoutButton;
    String todayDate;

    private CardView notificationCountCard, batteryCard, phoneringerCard, sensorCard, soundCard,gyroscope;
    private DatabaseReference databaseReference;
    private static final String TAG = "UnifiedService";
    FirebaseUser user_fetch;
    DatabaseReference databaseReference_fetching;
    public static List<String> jsonDataList;
    public interface DataCallback {
        void onSuccess(List<String> jsonData);
        void onFailure(String errorMessage);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference("StressData");
        FirebaseApp.initializeApp(this);

        IntentFilter filter = new IntentFilter(BatteryService.ACTION_BATTERY_UPDATE);
        registerReceiver(new BatteryReceiver(), filter, Context.RECEIVER_NOT_EXPORTED);

        user_fetch = FirebaseAuth.getInstance().getCurrentUser();
        String userId=user_fetch.getUid();
        databaseReference_fetching = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("sensorData");


        jsonDataList = new ArrayList<>();

        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        logoutButton = findViewById(R.id.logoutButton);
        notificationCountCard = findViewById(R.id.NotificationCountCard);
        batteryCard = findViewById(R.id.batteryCard);
        phoneringerCard = findViewById(R.id.phoneringerCard);
        sensorCard = findViewById(R.id.sensorCard);
        soundCard = findViewById(R.id.soundCard);
        gyroscope=findViewById(R.id.gyroscopeData);
        checkPermissionsAndStartServices();
        checkNotificationListenerPermission();
        checkAndRequestDNDPermission();
        checkAndRequestAudioPermission();
        if(!checkLocationPermissions()){
            requestLocationPermissions();}

       checkUsageStatsPermission();

        startButton.setOnClickListener(v -> {
            NotificationListener.resetNotifications();
            startService(new Intent(this, UnifiedService.class)); // Start the service
        });
        stopButton.setOnClickListener(v -> stopService(new Intent(this, UnifiedService.class)));
        logoutButton.setOnClickListener(v -> performLogout());

        disableBatteryOptimizations();
//        initializeDataCollectionTask();

        /*  eikhane add krtesi
        *
        *
        *
        *
        *
        * */

       setupCardViewListeners();

        todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        fetchJsonData(todayDate, new DataCallback() {
//            @Override
//            public void onSuccess(List<String> jsonData) {
//                //  Handle fetched JSON data
//                Log.d("FirebaseData", "Fetched JSON: " + jsonData.toString());
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                // Handle errors
//                Log.e("FirebaseError", errorMessage);
//            }
//        });




    }


    private void fetchJsonData(String selectedDate, DataCallback callback) {
        // 🔥 Ensure user is logged in
        if (user_fetch == null) {
            Log.e("Firebase", "User not logged in!");
            callback.onFailure("User not logged in!");
            return;
        }

        // 🔥 Reference the selected date node in Firebase
        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    callback.onFailure("No data found for this date!");
                    return;
                }

                // 🔥 Store JSON data
//                List<String> jsonDataList = new ArrayList<>();

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        // ✅ Extract raw JSON data
                        String rawJson = entrySnapshot.getValue().toString();
                        jsonDataList.add(rawJson);
                    } catch (Exception e) {
                        Log.e("Firebase", "Error fetching JSON: " + e.getMessage());
                    }
                }

                // ✅ Return collected JSON data through callback
                callback.onSuccess(jsonDataList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
                callback.onFailure("Database Error: " + error.getMessage());
            }
        });
    }


    private void performLogout() {
        FirebaseAuth.getInstance().signOut(); // Log out the user

        // Redirect to Login Activity
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);

        // Show a toast message
        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // Helper function to check permissions
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void checkPermissionsAndStartServices() {
        if (hasPermissions()) {
            startAllServices();
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }


    /*




    ei jaygay kaj kra lagbe
    *
    *
    *
    *
    *
    *
    * */
    private void setupCardViewListeners() {
        notificationCountCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, NotificationCountActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });
//        gyroscope.setOnClickListener(v ->{
//            if (hasPermissions()) {
//                startActivity(new Intent(MainActivity.this, Gyroscope.class));
//            } else {
//                showPermissionDeniedToast();
//            }
//        });

        batteryCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, BatteryActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        phoneringerCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, PhoneRingerActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        sensorCard.setOnClickListener(v -> {

            if (hasPermissions()) {
                //startActivity(new Intent(MainActivity.this, SensorActivity.class));
                Intent intent = new Intent(MainActivity.this, SensorActivity.class);

                // 🔥 Pass the full ArrayList if available
//                if (!jsonDataList.isEmpty()) {
//
//                    intent.putStringArrayListExtra("jsonDataList", new ArrayList<>(jsonDataList));
//                } else {
//                    intent.putStringArrayListExtra("jsonDataList", new ArrayList<>()); // Empty list
//                }

                startActivity(intent);
            } else {
                showPermissionDeniedToast();
            }
        });

//        sensorCard.setOnClickListener(v -> {
//            if (!hasPermissions()) {
//                showPermissionDeniedToast();
//                return;
//            }
//
//            fetchJsonData(todayDate, new DataCallback() {
//                @Override
//                public void onSuccess(List<String> jsonData) {
//                    // 🔥 Ensure data is properly logged
//                    Log.d("FirebaseData", "Fetched JSON: " + jsonData.toString());
//
//                    // ✅ Start SensorActivity AFTER data is fetched
//                    Intent intent = new Intent(MainActivity.this, SensorActivity.class);
//                    intent.putStringArrayListExtra("jsonDataList", new ArrayList<>(jsonData));
//                    startActivity(intent);
//                }
//
//                @Override
//                public void onFailure(String errorMessage) {
//                    // ❌ Handle errors properly
//                    Log.e("FirebaseError", errorMessage);
//                }
//            });
//        });



        soundCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, SoundLevelActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });
    }

    private void startAllServices() {
        if (hasPermissions()) {
            startService(new Intent(this, BatteryService.class));
            startService(new Intent(this, PhoneRingerService.class));
            startService(new Intent(this, SensorBackgroundService.class));
            startService(new Intent(this, SoundLevelService.class));
        } else {
            Toast.makeText(this, "Required permissions are not granted.", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopAllServices() {
        stopService(new Intent(this, BatteryService.class));
        stopService(new Intent(this, PhoneRingerService.class));
        stopService(new Intent(this, SensorBackgroundService.class));
        stopService(new Intent(this, SoundLevelService.class));
    }

    private void showPermissionDeniedToast() {
        Toast.makeText(this, "Permission is required to access this feature.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            boolean audioPermissionGranted = false;

            // Iterate through permissions to check their results
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                    // Check if RECORD_AUDIO permission was granted
                    audioPermissionGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }

                // Check if any permission was denied
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                }
            }

            // Handle permissions based on results
            if (allPermissionsGranted) {
                // Start all services if all required permissions are granted
                startAllServices();
            } else {
                // Handle RECORD_AUDIO-specific scenario
                if (!audioPermissionGranted) {
                    Toast.makeText(this, "Audio recording permission is required for noise level detection.", Toast.LENGTH_SHORT).show();
                }
                // Show general permission denied toast
                showPermissionDeniedToast();
            }
        }
    }
    private void initializeDataCollectionTask() {
        dataCollectionTask = new Runnable() {
            @Override
            public void run() {
                // Broadcast an intent to trigger data collection in services
                Intent intent = new Intent("com.example.stress_detection_app.COLLECT_DATA");
                sendBroadcast(intent);

                // Schedule the next execution
                handler.postDelayed(this, INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(dataCollectionTask); // Start periodic data collection

        // Register the receiver to listen for data updates from services
        IntentFilter filter = new IntentFilter("com.example.stress_detection_app.DATA_UPDATE");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(new BatteryReceiver(), filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(new BatteryReceiver(), filter);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(dataCollectionTask); // Stop periodic data collection
        try {
            unregisterReceiver(dataReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("ReceiverError", "Receiver not registered", e);
        }
    }

    public class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                int batteryLevel = intent.getIntExtra("batteryLevel", -1);
                String batteryStatus = intent.getStringExtra("batteryStatus");
                String dndStatus = intent.getStringExtra("dndStatus");

                batteryData.put("batteryLevel", batteryLevel);
                batteryData.put("batteryStatus", batteryStatus != null ? batteryStatus : "Unknown");
                batteryData.put("dndStatus", dndStatus != null ? dndStatus : "Unknown");
            } else {
                Log.e("BatteryReceiver", "Intent or extras are null");
            }
        }
    }

    public Map<String, Object> getBatteryData() {
        return batteryData;
    }

    private void startDataCollection() {
        dataUploadRunnable = new Runnable() {
            @Override
            public void run() {
                // Send data to Firebase
                sendToFirebase(batteryData);

                // Repeat every 3 seconds
                handler.postDelayed(this, INTERVAL);
            }
        };
        handler.post(dataUploadRunnable);
    }

    private void stopDataCollection() {
        handler.removeCallbacks(dataUploadRunnable);
    }

    private void sendToFirebase(Map<String, Object> data) {
        if (!data.isEmpty()) {
            databaseReference.push().setValue(data)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data uploaded successfully"))
                    .addOnFailureListener(e -> Log.d("Firebase", "Failed to upload data", e));
        } else {
            Toast.makeText(this, "No data to upload", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadDataToFirebase(StressDetectionData data) {
        /*
        *
        *
        * eikhane unique id te change krsi
        *
        * */
        String uniqueKey = databaseReference.push().getKey();
        if (uniqueKey != null) {
            databaseReference.child(uniqueKey).setValue(data)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to upload data. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", "Error uploading data", e);
                    });
        }
    }


    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Collect data from the broadcast
            String batteryData = intent.getStringExtra("batteryData");
            String notificationStatus = intent.getStringExtra("notificationStatus");
            String phoneStatus = intent.getStringExtra("phoneStatus");
            String sensors = intent.getStringExtra("sensors");
            String noiseLevel = intent.getStringExtra("noiseLevel");
            String dndstatus = intent.getStringExtra("dndStatus");
            float accelerometerX = intent.getFloatExtra("accelerometerX", 0f);
            float accelerometerY = intent.getFloatExtra("accelerometerY", 0f);
            float accelerometerZ = intent.getFloatExtra("accelerometerZ", 0f);

            float gyroscopeX = intent.getFloatExtra("gyroscopeX", 0f);
            float gyroscopeY = intent.getFloatExtra("gyroscopeY", 0f);
            float gyroscopeZ = intent.getFloatExtra("gyroscopeZ", 0f);

            float ambientLight = intent.getFloatExtra("ambientLight", -1f);
            // Generate a timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Combine data into a StressDetectionData object
            StressDetectionData data = new StressDetectionData();
            data.setTimestamp(timestamp);
            data.setBatteryData(batteryData);
            data.setNotificationStatus(notificationStatus);
            data.setPhoneStatus(phoneStatus);
            data.setSensors(sensors);
            data.setNoiseLevel(noiseLevel);
            data.setDndstatus(dndstatus);

            data.setAccelerometerX(accelerometerX);
            data.setAccelerometerY(accelerometerY);
            data.setAccelerometerZ(accelerometerZ);

            data.setGyroscopeX(gyroscopeX);
            data.setGyroscopeY(gyroscopeY);
            data.setGyroscopeZ(gyroscopeZ);

            data.setAmbientLight(ambientLight);

            // Send the data to Firebase
            uploadDataToFirebase(data);
        }
    };


    private void disableBatteryOptimizations() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void checkNotificationListenerPermission() {
        if (!NotificationListener.isEnabled(this)) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void checkUsageStatsPermission() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent); // Redirect user to usage access settings
        }
    }




    private void checkAndRequestDNDPermission() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            // Guide the user to grant the required permission
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


    }

    private void checkAndRequestAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, guide the user to app settings
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d(TAG, "Please enable RECORD_AUDIO permission in app settings.");
        } else {
            Log.d(TAG, "RECORD_AUDIO permission already granted.");
        }
    }
}
