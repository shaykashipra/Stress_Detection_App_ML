package com.example.stress_detection_app.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.stress_detection_app.Listener.NotificationListener;
import com.example.stress_detection_app.Model.StressDetectionData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

public class UnifiedService extends Service {

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
    //String amPmTime = formatter.format(new Date(System.currentTimeMillis()));

    public static Queue<String> keyQueue = new LinkedList<>();
    private static final String TAG = "UnifiedService";
    private static final int INTERVAL = 3000; // 3 seconds
    private static final String CHANNEL_ID = "UnifiedServiceChannel";

    private DatabaseReference mDatabase;
    private Handler handler,handler2;
    private List<StressDetectionData> offlineBuffer;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private float[] accelerometerValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float ambientLightValue = -1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String address = "Unknown";
    private Runnable foregroundAppLogger;
    private String currentForegroundApp = "Unknown"; // Default value
    private String bluetooth_data="NULL";

    BluetoothDevice arduinoBTModule = null;
    private final String DEVICE_NAME = "HC-05";
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;


    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        Intent bluetoothServiceIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothServiceIntent);

        IntentFilter filter = new IntentFilter("com.example.stress_detection_app.BLUETOOTH_DATA");
        registerReceiver(dataReceiver, filter);

        handler2 = new Handler(Looper.getMainLooper()); // Initialize the handler
        foregroundAppLogger = new Runnable() {
            @Override
            public void run() {
                // Get the foreground app
                currentForegroundApp = getForegroundApp();
                Log.d(TAG, "Foreground App: " + currentForegroundApp);

                // Schedule the next run
                handler2.postDelayed(this, 3000); // Every 5 seconds
            }
        };

        // Start logging the foreground app
        handler2.post(foregroundAppLogger);
        // Initialize location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Define the location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        address = getAddressFromLocation(location);
                    }
                }
            }
        };


        // Start location updates
        startLocationUpdates();



        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UnifiedService:WakeLock");
        wakeLock.acquire();

        // Initialize Firebase Database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize offline buffer
        offlineBuffer = new ArrayList<>();

        // Start foreground service
        createNotificationChannel();
        startForeground(1, getNotification());

        // Initialize Handler for periodic tasks
        handler = new Handler();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerSensorListeners();

        createNotificationChannel();
        startForeground(1, getNotification());
        // Start periodic data collection
        startDataCollection();
    }

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.stress_detection_app.BLUETOOTH_DATA".equals(intent.getAction())) {
                String bluetoothData = intent.getStringExtra("data");
                Log.d(TAG, "Received Bluetooth data: " + bluetoothData);

                // Process the received Bluetooth data
                processBluetoothData(bluetoothData);
            }
        }
    };

    private void processBluetoothData(String bluetoothData) {
        //code kra lagbe
        bluetooth_data=bluetoothData;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Stop handler callbacks when the service is destroyed
//        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(dataReceiver);

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (handler2 != null) {
            handler2.removeCallbacks(foregroundAppLogger); // Stop the foreground app logger
        }
        // Stop location updates
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Start collecting data every 3 seconds
    private void startDataCollection() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                collectAndSyncData();
                handler.postDelayed(this, INTERVAL); // Schedule the next execution
            }
        }, INTERVAL);
    }

    // Collect data and sync it to Firebase
    private void collectAndSyncData() {
        try {
            String batteryLevel = getBatteryLevel();
            String batteryStatus = getBatteryStatus();
            String dndStatus = getDoNotDisturbStatus();
            String noiseLevel = getNoiseLevel();
            String bluetooth=bluetooth_data;
            int phoneCallStatusValue = getPhoneCallStatusValue();
            int ringerModeValue = getRingerModeValue();
            int screenStatusValue = getScreenStatusValue();
            // Create a StressDetectionData object
            StressDetectionData data = new StressDetectionData();
            data.setTimestamp(String.valueOf(formatter.format(System.currentTimeMillis())));
            data.setBatteryData(batteryLevel);
            data.setPhoneStatus(batteryStatus);
            data.setDndstatus(dndStatus);
            data.setBluetooth(bluetooth_data);

            data.setNoiseLevel(noiseLevel);
            data.setAccelerometerX(accelerometerValues[0]);
            data.setAccelerometerY(accelerometerValues[1]);
            data.setAccelerometerZ(accelerometerValues[2]);

            data.setGyroscopeX(gyroscopeValues[0]);
            data.setGyroscopeY(gyroscopeValues[1]);
            data.setGyroscopeZ(gyroscopeValues[2]);


            data.setAmbientLight(ambientLightValue);
            data.setNotificationCount(getNotificationCount());
            data.setNotificationSources(serializeNotificationSources());
            data.setPhoneCallStatusValue(phoneCallStatusValue);
            data.setRingerModeValue(ringerModeValue);
            data.setScreenStatusValue(screenStatusValue);

            data.setLatitude(latitude);
            data.setLongitude(longitude);
            data.setAddress(address);

            data.setForegroundApp(currentForegroundApp);



            String jsonData = convertDataToJson(data);

            if (isNetworkAvailable()) {
                // Sync to Firebase if network is available
                syncToFirebase(jsonData);

                // Sync buffered data
                syncBufferedData();
            } else {
                // Add data to offline buffer
                offlineBuffer.add(data);
                Log.d(TAG, "Data added to offline buffer: " + data);
            }
            broadcastSensorData();

        } catch (Exception e) {
            Log.e(TAG, "Error collecting or syncing data: " + e.getMessage(), e);
        }
    }

    // Check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Sync buffered data to Firebase
    private void syncBufferedData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            for (StressDetectionData data : new ArrayList<>(offlineBuffer)) {
                try {
                    String jsonData = convertDataToJson(data);
                    String key = mDatabase.child("users").child(userId).child("sensorData").push().getKey();
                      //String key="0";
                    if (key != null) {
                        mDatabase.child("users").child(userId).child("sensorData").child(key).setValue(jsonData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Buffered data synced successfully for user: " + userId);
                                    offlineBuffer.remove(data);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to sync buffered data for user: " + userId, e));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error syncing buffered data for user: " + userId, e);
                }
            }
        } else {
            Log.e(TAG, "User is not authenticated. Cannot sync buffered data.");
        }
    }


    // Get battery level
    private String getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            if (level != -1 && scale != -1) {
                return (level * 100 / scale) + "%";
            }
        }
        return "Unknown";
    }

    // Get battery charging status
    private String getBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            return isCharging ? "Charging" : "Not Charging";
        }
        return "Unknown";
    }

    // Get Do Not Disturb status
    private String getDoNotDisturbStatus() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Check if DND access is granted
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                int interruptionFilter = notificationManager.getCurrentInterruptionFilter();

                switch (interruptionFilter) {
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        return "Do Not Disturb (Total Silence)";
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                        return "Do Not Disturb (Priority Only)";
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                        return "Do Not Disturb (Alarms Only)";
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        return "Off";
                    default:
                        return "Unknown";
                }
            } else {
                return "Permission Not Granted";
            }
        }
        return "Unknown";
    }

    // Get noise level (simulated for this example)
    private String getNoiseLevel() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return "Permission Denied";
        }

        int bufferSize = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return "Error in Audio Record Initialization";
        }

        AudioRecord audioRecord = null;
        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    44100,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
            );

            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();

            int readBytes = audioRecord.read(buffer, 0, bufferSize);
            if (readBytes > 0) {
                double sum = 0.0;
                for (short s : buffer) {
                    sum += s * s;
                }

                double rms = Math.sqrt(sum / readBytes);
                double dB = 20 * Math.log10(rms);

                if (dB < 0) {
                    dB = 0;
                }
                return String.format(Locale.getDefault(), "%.2f dB", dB);
            } else {
                return "Error reading audio data";
            }
        } finally {
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
            }
        }
    }


    // Sync data to Firebase
    private void syncToFirebase(String jsonData) {
        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get the unique UID of the user

            try {
                // Generate a unique key for the sensor data
                String key = mDatabase.child("users").child(userId).child("sensorData").push().getKey();

               String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()); // 🔥 Get today's date (YYYYMMDD)
//                String uniqueFirebaseKey = mDatabase.child("users").child(userId).child("sensorData").push().getKey(); // 🔥 Get Firebase push key
//
//                String key = date + "_" + uniqueFirebaseKey;
                if (key != null) {
                    // Save the data under the user's UID
                    if(keyQueue.size()>=20){
                        String remv;
                        remv = keyQueue.poll();
                    }
                    keyQueue.add(key);
                    mDatabase.child("users").child(userId).child("sensorData").child(date).child(key).setValue(jsonData)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Data synced successfully for user: " + userId))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to sync data for user: " + userId, e));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while syncing data for user: " + userId, e);
            }
        } else {
            Log.e(TAG, "User is not authenticated. Cannot sync data.");
        }
    }


    // Create notification for foreground service
    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Unified Service Running")
                .setContentText("Collecting and syncing data every 3 seconds.")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .build();
    }

    // Create notification channel
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Unified Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    // Check and request DND permission


    private String getAccelerometerData() {
        return String.format("X: %.2f, Y: %.2f, Z: %.2f", accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]);
    }

    private String getGyroscopeData() {
        return String.format("X: %.2f, Y: %.2f, Z: %.2f", gyroscopeValues[0], gyroscopeValues[1], gyroscopeValues[2]);
    }

    private String getAmbientLightData() {
        return ambientLightValue >= 0 ? String.valueOf(ambientLightValue) : "Unknown";
    }

    private void registerSensorListeners() {
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            if (accelerometer != null) {
                sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (gyroscope != null) {
                sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            }

            if (lightSensor != null) {
                sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }


    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues[0] = event.values[0];
                accelerometerValues[1] = event.values[1];
                accelerometerValues[2] = event.values[2];
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroscopeValues[0] = event.values[0];
                gyroscopeValues[1] = event.values[1];
                gyroscopeValues[2] = event.values[2];
            } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                ambientLightValue = event.values[0];
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // No action needed for this implementation
        }
    };
    private Map<String, Integer> getNotificationSources() {
        if (NotificationListener.notificationSources == null) {
            Log.e("UnifiedService", "Notification sources are null");
            return new HashMap<>(); // Return an empty map if null
        }

        Log.d("UnifiedService", "Notification sources: " + NotificationListener.notificationSources);
        return new HashMap<>(NotificationListener.notificationSources);
    }
    private String serializeNotificationSources() {
        Map<String, Integer> sources = getNotificationSources();
        return new Gson().toJson(sources); // Convert to JSON string
    }

    private int getNotificationCount() {
        return NotificationListener.notificationCount;
    }
    private void broadcastSensorData() {
        Intent intent = new Intent("com.example.stress_detection_app.DATA_UPDATE");
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("address", address);
        intent.putExtra("foregroundApp", currentForegroundApp);

        intent.putExtra("accelerometerX", accelerometerValues[0]);
        intent.putExtra("accelerometerY", accelerometerValues[1]);
        intent.putExtra("accelerometerZ", accelerometerValues[2]);
        intent.putExtra("gyroscopeX", gyroscopeValues[0]);
        intent.putExtra("gyroscopeY", gyroscopeValues[1]);
        intent.putExtra("gyroscopeZ", gyroscopeValues[2]);
        intent.putExtra("ambientLight", ambientLightValue);
        intent.putExtra("notificationCount", getNotificationCount());
        intent.putExtra("notificationSources", new HashMap<>(getNotificationSources()));
        intent.putExtra("phoneCallStatusValue", getPhoneCallStatusValue());
        intent.putExtra("ringerModeValue", getRingerModeValue());
        intent.putExtra("screenStatusValue", getScreenStatusValue());
        intent.putExtra("Bluetooth",bluetooth_data);
        sendBroadcast(intent);
    }
    private String convertDataToJson(StressDetectionData data) {
        try {
            Map<String, Object> dataset = new HashMap<>();

            // Time-series data block
            Map<String, Map<String, Object>> timeSeries = new HashMap<>();
            Map<String, Object> sensorData = new HashMap<>();
            sensorData.put("accelerometerX", data.getAccelerometerX());
            sensorData.put("accelerometerY", data.getAccelerometerY());
            sensorData.put("accelerometerZ", data.getAccelerometerZ());
            sensorData.put("gyroscopeX", data.getGyroscopeX());
            sensorData.put("gyroscopeY", data.getGyroscopeY());
            sensorData.put("gyroscopeZ", data.getGyroscopeZ());
            sensorData.put("ambientLight", data.getAmbientLight());
            sensorData.put("noiseLevel", data.getNoiseLevel());
            sensorData.put("batteryLevel", data.getBatteryData());
            sensorData.put("batteryStatus", data.getPhoneStatus());
            sensorData.put("dndStatus", data.getDndstatus());
            sensorData.put("notificationCount", data.getNotificationCount());
            sensorData.put("notificationSources", data.getNotificationSources());
            dataset.put("latitude", data.getLatitude());
            dataset.put("longitude", data.getLongitude());
            dataset.put("address", data.getAddress());
            dataset.put("foregroundApp", data.getForegroundApp());
            dataset.put("bluetooth",data.getBluetooth());


            // Add sensor data with a timestamp as the key
            timeSeries.put(data.getTimestamp(), sensorData);
            dataset.put("dataset", timeSeries);

            // Wrap the dataset into the desired JSON format
//            Map<String, Object> jsonStructure = new HashMap<>();
//            jsonStructure.put("dataset", dataset);

            return new Gson().toJson(data);
        } catch (Exception e) {
            Log.e(TAG, "Error in JSON conversion: " + e.getMessage(), e);
            return "{}"; // Return empty JSON if there's an error
        }
    }

    private int getPhoneCallStatusValue() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int callState = telephonyManager.getCallState();
            switch (callState) {
                case TelephonyManager.CALL_STATE_IDLE:
                    return 0; // Idle
                case TelephonyManager.CALL_STATE_RINGING:
                    return 1; // Ringing
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    return 2; // In Call
                default:
                    return -1; // Unknown
            }
        }
        return -1; // Unavailable
    }

    private int getRingerModeValue() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int ringerMode = audioManager.getRingerMode();
            switch (ringerMode) {
                case AudioManager.RINGER_MODE_SILENT:
                    return 0; // Silent
                case AudioManager.RINGER_MODE_VIBRATE:
                    return 1; // Vibrate
                case AudioManager.RINGER_MODE_NORMAL:
                    return 2; // Normal
                default:
                    return -1; // Unknown
            }
        }
        return -1; // Unavailable
    }

    private int getScreenStatusValue() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            return powerManager.isInteractive() ? 1 : 0; // On: 1, Off: 0
        }
        return -1; // Unavailable
    }


    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(INTERVAL) // Update every 10 seconds
                .setFastestInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle missing permissions (request them if needed)
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unable to fetch address";
    }
    private String getForegroundApp() {
        String currentApp = "Unknown";
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager != null) {
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 1000 * 60 * 5; // Check usage within the last 5 minutes

            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);

            if (usageStatsList != null && !usageStatsList.isEmpty()) {
                SortedMap<Long, UsageStats> sortedUsageStats = new TreeMap<>();
                for (UsageStats usageStats : usageStatsList) {
                    sortedUsageStats.put(usageStats.getLastTimeUsed(), usageStats);
                }

                if (!sortedUsageStats.isEmpty()) {
                    currentApp = sortedUsageStats.get(sortedUsageStats.lastKey()).getPackageName();
                }
            } else {
                Log.e(TAG, "No usage stats available. Check if permission is granted.");
            }
        }
        return currentApp;
    }



}