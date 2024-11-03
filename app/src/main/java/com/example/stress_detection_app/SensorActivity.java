package com.example.stress_detection_app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor lightSensor;

    private TextView accelerometerData;
    private TextView gyroscopeData;
    private TextView lightSensorData;

    private static final int UPDATE_INTERVAL_MS = 3000; // 3 seconds
    private Handler handler = new Handler();
    private Runnable updateSensorDataRunnable;

    private float[] accelerometerValues = new float[3];
    private float[] gyroscopeValues = new float[3];
    private float lightLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor); // Ensure layout matches

        // Initialize TextViews for displaying sensor data
        accelerometerData = findViewById(R.id.accelerometerData);
        gyroscopeData = findViewById(R.id.gyroscopeData);
        lightSensorData = findViewById(R.id.lightSensorData); // New TextView for light sensor

        // Initialize SensorManager and the individual sensors with try-catch
        try {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); // Ambient Light Sensor
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error accessing sensors", Toast.LENGTH_SHORT).show();
            Log.e("SensorActivity", "Error initializing sensors", e);
        }

        setupSensorDataUpdater();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register listeners for each sensor
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        handler.post(updateSensorDataRunnable); // Start 3-second updates
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listeners to save battery
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(updateSensorDataRunnable); // Stop 3-second updates
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Update values based on the sensor type with try-catch for error handling
        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroscopeValues = event.values.clone();
            } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                lightLevel = event.values[0];
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error reading sensor data", Toast.LENGTH_SHORT).show();
            Log.e("SensorActivity", "Error in onSensorChanged", e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

    // Method to set up a 3-second interval for updating UI with sensor data
    private void setupSensorDataUpdater() {
        updateSensorDataRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // Update TextViews with the latest sensor data
                    accelerometerData.setText(String.format("Accelerometer: X=%.2f, Y=%.2f, Z=%.2f",
                            accelerometerValues[0], accelerometerValues[1], accelerometerValues[2]));
                    gyroscopeData.setText(String.format("Gyroscope: X=%.2f, Y=%.2f, Z=%.2f",
                            gyroscopeValues[0], gyroscopeValues[1], gyroscopeValues[2]));
                    lightSensorData.setText(String.format("Ambient Light Level: %.2f lx", lightLevel));
                } catch (Exception e) {
                    Toast.makeText(SensorActivity.this, "Error updating sensor data", Toast.LENGTH_SHORT).show();
                    Log.e("SensorActivity", "Error in updateSensorDataRunnable", e);
                }

                // Schedule the next update after 3 seconds
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };
    }
}
