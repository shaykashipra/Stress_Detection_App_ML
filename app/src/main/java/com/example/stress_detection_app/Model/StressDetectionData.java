
package com.example.stress_detection_app.Model;

import java.util.List;
import java.util.Map;

public class StressDetectionData {
    private String timestamp;
    private String batteryData;
    private String notificationStatus;
    private String phoneStatus;
    private String sensors;
    private String noiseLevel;
    private String dndstatus;
    private Float gyroscopeX;
    private Float gyroscopeY;
    private Float gyroscopeZ;
    private Float accelerometerX;
    private Float accelerometerY;
    private Float accelerometerZ;
    private Float ambientLight;
    private int notificationCount;
    private String notificationSources;
    private int phoneCallStatusValue;
    private int ringerModeValue;
    private int screenStatusValue;
    private double latitude;
    private double longitude;
    private String address;
    String foregroundApp;
    String bluetooth_data;

    public StressDetectionData() {
    }

    public Float getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Float ambientLight) {
        this.ambientLight = ambientLight;
    }

    // Full constructor
    public StressDetectionData(
            String timestamp,
            String batteryData,
            String notificationStatus,
            String phoneStatus,
            String sensors,
            String noiseLevel,
            String dndstatus,
            Float ambientLight,
            Float gyroscopeX,
            Float gyroscopeY,
            Float gyroscopeZ,
            Float accelerometerX,
            Float accelerometerY,
            Float accelerometerZ,
            String bluetooth_data
    ) {
        this.timestamp = timestamp;
        this.batteryData = batteryData;
        this.notificationStatus = notificationStatus;
        this.phoneStatus = phoneStatus;
        this.sensors = sensors;
        this.noiseLevel = noiseLevel;
        this.dndstatus = dndstatus;
        this.ambientLight = ambientLight;
        this.gyroscopeX = gyroscopeX;
        this.gyroscopeY = gyroscopeY;
        this.gyroscopeZ = gyroscopeZ;
        this.accelerometerX = accelerometerX;
        this.accelerometerY = accelerometerY;
        this.accelerometerZ = accelerometerZ;
        this.bluetooth_data=bluetooth_data;
    }
    // Getters and Setters for gyroscope and accelerometer data
    public String getBluetooth() {return bluetooth_data;}
    public void setBluetooth(String bluetooth_data){this.bluetooth_data=bluetooth_data;}
    public Float getGyroscopeX() { return gyroscopeX; }
    public void setGyroscopeX(Float gyroscopeX) { this.gyroscopeX = gyroscopeX; }
    public Float getGyroscopeY() { return gyroscopeY; }
    public void setGyroscopeY(Float gyroscopeY) { this.gyroscopeY = gyroscopeY; }
    public Float getGyroscopeZ() { return gyroscopeZ; }
    public void setGyroscopeZ(Float gyroscopeZ) { this.gyroscopeZ = gyroscopeZ; }

    public Float getAccelerometerX() { return accelerometerX; }
    public void setAccelerometerX(Float accelerometerX) { this.accelerometerX = accelerometerX; }
    public Float getAccelerometerY() { return accelerometerY; }
    public void setAccelerometerY(Float accelerometerY) { this.accelerometerY = accelerometerY; }
    public Float getAccelerometerZ() { return accelerometerZ; }
    public void setAccelerometerZ(Float accelerometerZ) { this.accelerometerZ = accelerometerZ; }

    // Other getters and setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getBatteryData() { return batteryData; }
    public void setBatteryData(String batteryData) { this.batteryData = batteryData; }

    public String getNotificationStatus() { return notificationStatus; }
    public void setNotificationStatus(String notificationStatus) { this.notificationStatus = notificationStatus; }

    public String getPhoneStatus() { return phoneStatus; }
    public void setPhoneStatus(String phoneStatus) { this.phoneStatus = phoneStatus; }

    public String getSensors() { return sensors; }
    public void setSensors(String sensors) { this.sensors = sensors; }

    public String getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(String noiseLevel) { this.noiseLevel = noiseLevel; }

    public String getDndstatus() { return dndstatus; }
    public void setDndstatus(String dndstatus) { this.dndstatus = dndstatus; }
    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public String getNotificationSources() {
        return notificationSources;
    }

    public void setNotificationSources(String notificationSources) {
        this.notificationSources = notificationSources;
    }

    public int getPhoneCallStatusValue() {
        return phoneCallStatusValue;
    }

    public void setPhoneCallStatusValue(int phoneCallStatusValue) {
        this.phoneCallStatusValue = phoneCallStatusValue;
    }

    public int getRingerModeValue() {
        return ringerModeValue;
    }

    public void setRingerModeValue(int ringerModeValue) {
        this.ringerModeValue = ringerModeValue;
    }

    public int getScreenStatusValue() {
        return screenStatusValue;
    }

    public void setScreenStatusValue(int screenStatusValue) {
        this.screenStatusValue = screenStatusValue;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getForegroundApp() {
        return foregroundApp;
    }

    public void setForegroundApp(String foregroundApp) {
        this.foregroundApp = foregroundApp;
    }


}
