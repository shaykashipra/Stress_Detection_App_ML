//package com.example.stress_detection_app;
//
//import android.content.Context;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class SensorActivity extends AppCompatActivity implements SensorEventListener {
//
//    private SensorManager sensorManager;
//    private Sensor accelerometer;
//    private Sensor gyroscope;
//    private Sensor lightSensor;
//
//    private TextView accelerometerData;
//    private TextView gyroscopeData;
//    private TextView lightSensorData;
//
//    private static final int UPDATE_INTERVAL_MS = 3000; // 3 seconds
//    private Handler handler = new Handler();
//    private Runnable updateSensorDataRunnable;
//
//    private float[] accelerometerValues = new float[3];
//    private float[] gyroscopeValues = new float[3];
//    private float lightLevel;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sensor); // Ensure layout matches
//
//        // Initialize TextViews for displaying sensor data
//        accelerometerData = findViewById(R.id.accelerometerData);
//        gyroscopeData = findViewById(R.id.gyroscopeData);
//        lightSensorData = findViewById(R.id.lightSensorData); // New TextView for light sensor
//
//        // Initialize SensorManager and the individual sensors with try-catch
//        try {
//            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            if (sensorManager != null) {
//                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); // Ambient Light Sensor
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Error accessing sensors", Toast.LENGTH_SHORT).show();
//            Log.e("SensorActivity", "Error initializing sensors", e);
//        }
//
//        setupSensorDataUpdater();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Register listeners for each sensor
//        if (accelerometer != null) {
//            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//        if (gyroscope != null) {
//            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//        if (lightSensor != null) {
//            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
//        }
//        handler.post(updateSensorDataRunnable); // Start 3-second updates
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        // Unregister listeners to save battery
//        sensorManager.unregisterListener(this);
//        handler.removeCallbacks(updateSensorDataRunnable); // Stop 3-second updates
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        // Update values based on the sensor type with try-catch for error handling
//        try {
//            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                accelerometerValues = event.values.clone();
//            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//                gyroscopeValues = event.values.clone();
//            } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
//                lightLevel = event.values[0];
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Error reading sensor data", Toast.LENGTH_SHORT).show();
//            Log.e("SensorActivity", "Error in onSensorChanged", e);
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        // Handle accuracy changes if needed
//    }
//
//    // Method to set up a 3-second interval for updating UI with sensor data
//    private void setupSensorDataUpdater() {
//        updateSensorDataRunnable = new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // Update TextViews with the latest sensor data
//                    accelerometerData.setText(String.format("Accelerometer: X=%.2f, Y=%.2f, Z=%.2f",
//                            accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]));
//                    gyroscopeData.setText(String.format("Gyroscope: X=%.2f, Y=%.2f, Z=%.2f",
//                            gyroscopeValues[0], gyroscopeValues[1], gyroscopeValues[2]));
//                    lightSensorData.setText(String.format("Ambient Light Level: %.2f lx", lightLevel));
//                } catch (Exception e) {
//                    Toast.makeText(SensorActivity.this, "Error updating sensor data", Toast.LENGTH_SHORT).show();
//                    Log.e("SensorActivity", "Error in updateSensorDataRunnable", e);
//                }
//
//                // Schedule the next update after 3 seconds
//                handler.postDelayed(this, UPDATE_INTERVAL_MS);
//            }
//        };
//    }
//}
package com.example.stress_detection_app;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stress_detection_app.Services.SensorBackgroundService;
import com.example.stress_detection_app.Services.UnifiedService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;

public class SensorActivity extends AppCompatActivity {

    private TextView accelerometerData;
    private TextView gyroscopeData;
    private TextView lightSensorData;
    Queue<String>queue;
    private LineChart accelerometerChart;
    private DatabaseReference databaseReference;
    FirebaseUser user;

    private ArrayList<Entry> xEntries, yEntries, zEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);




        accelerometerData = findViewById(R.id.accelerometerData);
        gyroscopeData = findViewById(R.id.gyroscopeData);
        lightSensorData = findViewById(R.id.lightSensorData);

        accelerometerChart=findViewById(R.id.accelerometerChart);
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId=user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("sensorData");

        xEntries = new ArrayList<>();
        yEntries = new ArrayList<>();
        zEntries = new ArrayList<>();
        queue= UnifiedService.keyQueue;

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()); // Example: "20240217"
        fetchAccelerometerData(todayDate);
        Log.d("Debug", "xEntries: " + xEntries.toString());
       // accelerometerData.setText(UnifiedService.keyQueue.size());
        updateGraph();

        // Display the most recent data from the background service
        //displaySensorData();
        //accelerometerData.setText(MainActivity.jsonDataList.get(0));
//        ArrayList<String> jsonDataList = getIntent().getStringArrayListExtra("jsonDataList");
//
//        // ✅ Display the data (Join JSON strings with line breaks)
//        if (jsonDataList != null && !jsonDataList.isEmpty()) {
//            String formattedData = TextUtils.join("\n\n", jsonDataList); // Join with spacing
//            accelerometerData.setText(formattedData);
//        } else {
//            accelerometerData.setText("No data available");
//        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        //fetchAccelerometerData();
        //displaySensorData(); // Update data whenever the activity is resumed
    }

//    private void displaySensorData() {
//        float[] accValues = SensorBackgroundService.getAccelerometerValues();
//        float[] gyroValues = SensorBackgroundService.getGyroscopeValues();
//        float lightValue = SensorBackgroundService.getLightLevel();
//
//        accelerometerData.setText(String.format("Accelerometer: X=%.2f, Y=%.2f, Z=%.2f", accValues[0], accValues[1], accValues[2]));
//        gyroscopeData.setText(String.format("Gyroscope: X=%.2f, Y=%.2f, Z=%.2f", gyroValues[0], gyroValues[1], gyroValues[2]));
//        lightSensorData.setText(String.format("Ambient Light Level: %.2f lx", lightValue));
//    }


//    private void fetchAccelerometerData() {
//
//
//        databaseReference.addValueEventListener(new ValueEventListener() {
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                xEntries.clear();
////                yEntries.clear();
////                zEntries.clear();
//
//                // Parse JSON object from database
//                if (dataSnapshot.exists()) {
//                    float x = dataSnapshot.child("accelerometerX").getValue(Float.class);
//                    float y = dataSnapshot.child("accelerometerY").getValue(Float.class);
//                    float z = dataSnapshot.child("accelerometerZ").getValue(Float.class);
//                    String timestamp = dataSnapshot.child("timestamp").getValue(String.class);
//
//                    // Use timestamp index as X-axis (for simplicity, use size)
//                    float index = xEntries.size();
//
//                    // Add data points
////                    xEntries.add(new Entry(index, x));
////                    yEntries.add(new Entry(index, y));
////                    zEntries.add(new Entry(index, z));
//                    accelerometerData.setText(String.valueOf(x));
//
//                    //updateGraph();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Firebase", "Database Error: " + error.getMessage());
//            }
//        });
//    }


    private void fetchAccelerometerData(String selectedDate) {
        xEntries.clear();
        yEntries.clear();
        zEntries.clear();
        accelerometerData.setText("Fetching...");

        // 🔥 Ensure user is logged in
        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            accelerometerData.setText("User not logged in!");
            return;
        }

        // 🔥 Reference the selected date node in Firebase
        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    accelerometerData.setText("No data found for this date!");
                    return;
                }

                // 🔥 Loop through all unique IDs under the selected date
                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        // ✅ Extract JSON data
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // ✅ Get Accelerometer values
                        float x = (float) jsonObject.getDouble("accelerometerX");
                        float y = (float) jsonObject.getDouble("accelerometerY");
                        float z = (float) jsonObject.getDouble("accelerometerZ");

                        // ✅ Update Graph Data
                        float index = xEntries.size();
                        xEntries.add(new Entry(index, x));
                        yEntries.add(new Entry(index, y));
                        zEntries.add(new Entry(index, z));

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // ✅ Update UI and graph after all data is fetched
                runOnUiThread(() -> {
                    accelerometerData.setText("Data fetched for date: " + selectedDate);
                    updateGraph(); // 🔥 Update graph after fetching all entries
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }






    //updating work
    private void updateGraph() {
        if (xEntries.isEmpty() || yEntries.isEmpty() || zEntries.isEmpty()) {
            Log.e("Graph", "No data to update graph!");
            return;
        }

        LineDataSet xDataSet = new LineDataSet(xEntries, "X-Axis");
        xDataSet.setColor(Color.RED);
        xDataSet.setValueTextSize(10f);
        xDataSet.setDrawCircles(false); // 🔥 No data points
        xDataSet.setDrawValues(false);  // 🔥 No values displayed

        LineDataSet yDataSet = new LineDataSet(yEntries, "Y-Axis");
        yDataSet.setColor(Color.GREEN);
        yDataSet.setValueTextSize(10f);
        yDataSet.setDrawCircles(false); // 🔥 No data points
        yDataSet.setDrawValues(false);  // 🔥 No values displayed

        LineDataSet zDataSet = new LineDataSet(zEntries, "Z-Axis");
        zDataSet.setColor(Color.BLUE);
        zDataSet.setValueTextSize(10f);
        zDataSet.setDrawCircles(false); // 🔥 No data points
        zDataSet.setDrawValues(false);  // 🔥 No values displayed

        LineData lineData = new LineData(xDataSet, yDataSet, zDataSet);
        accelerometerChart.setData(lineData);

        // Customize chart
        XAxis xAxis = accelerometerChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = accelerometerChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        accelerometerChart.getAxisRight().setEnabled(false);

        Legend legend = accelerometerChart.getLegend();
        legend.setTextSize(12f);

        accelerometerChart.invalidate(); // 🔥 Refresh the graph
    }


}

