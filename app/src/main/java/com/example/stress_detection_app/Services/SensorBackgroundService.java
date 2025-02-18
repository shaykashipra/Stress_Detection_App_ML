package com.example.stress_detection_app.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SensorBackgroundService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor lightSensor;

    private static float[] accelerometerValues = new float[3];
    private static float[] gyroscopeValues = new float[3];
    private static float lightLevel;

    private Handler handler = new Handler();
    private Runnable dataUpdateRunnable;
    private static final int UPDATE_INTERVAL_MS = 3000; // 3 seconds

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }

        startSensorDataUpdates();
    }

    private void startSensorDataUpdates() {
        dataUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (accelerometer != null) {
                    sensorManager.registerListener(SensorBackgroundService.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                }
                if (gyroscope != null) {
                    sensorManager.registerListener(SensorBackgroundService.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                }
                if (lightSensor != null) {
                    sensorManager.registerListener(SensorBackgroundService.this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };
        handler.post(dataUpdateRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(dataUpdateRunnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeValues = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            lightLevel = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing here
    }

    public static float[] getAccelerometerValues() {
        return accelerometerValues;
    }

    public static float[] getGyroscopeValues() {
        return gyroscopeValues;
    }

    public static float getLightLevel() {
        return lightLevel;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
