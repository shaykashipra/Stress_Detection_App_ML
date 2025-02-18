package com.example.stress_detection_app.Model;

public class userData {
    private float accelerometerX;
    private float accelerometerY;
    private float accelerometerZ;
    private String timestamp; // Optional, for tracking time

    public userData() { } // Required for Firebase

    public userData(float accelerometerX, float accelerometerY, float accelerometerZ, String timestamp) {
        this.accelerometerX = accelerometerX;
        this.accelerometerY = accelerometerY;
        this.accelerometerZ = accelerometerZ;
        this.timestamp = timestamp;
    }

    public float getAccelerometerX() { return accelerometerX; }
    public float getAccelerometerY() { return accelerometerY; }
    public float getAccelerometerZ() { return accelerometerZ; }
    public String getTimestamp() { return timestamp; }
}
