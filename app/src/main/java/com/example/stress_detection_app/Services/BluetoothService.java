package com.example.stress_detection_app.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private static final String DEVICE_NAME = "HC-05"; // Replace with your Bluetooth device name
    private static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getApplicationContext(), "mew mew " , Toast.LENGTH_SHORT).show();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported. Stopping service.");
            stopSelf();
        }
        connectToDevice();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectBluetooth();
    }

    // Discover and connect to the Bluetooth device
    public void connectToDevice() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled.");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Log.e(TAG, "No paired devices found. Pair with the target device first.");
            return;
        }

        BluetoothDevice targetDevice = null;

        // Find the target device by name
        for (BluetoothDevice device : pairedDevices) {
            Log.d(TAG, "Paired Device: " + device.getName() + " [" + device.getAddress() + "]");
            if (DEVICE_NAME.equals(device.getName())) {
                targetDevice = device;
                Toast.makeText(getApplicationContext(), "Target device " + DEVICE_NAME + " found!", Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (targetDevice == null) {
            Log.e(TAG, "Target device " + DEVICE_NAME + " not found. Pair with the device first.");
            return;
        }

        try {
            // Create socket and connect
            bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(DEVICE_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            Log.d(TAG, "Successfully connected to device: " + DEVICE_NAME);

            // Start listening for data
            listenForData();
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device: " + e.getMessage(), e);
            disconnectBluetooth();
        }
    }

    private void listenForData() {
        new Thread(() -> {
            try {
                if (inputStream == null) {
                    Log.e(TAG, "Input stream is null. Cannot read data.");
                    return;
                }

                byte[] buffer = new byte[1024];
                int bytes;

                while (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String receivedData = new String(buffer, 0, bytes);
                        Log.d(TAG, "Data received: " + receivedData);

                        // Send data to UnifiedService
                        sendDataToUnifiedService(receivedData);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading data: " + e.getMessage(), e);
            }
        }).start();
    }

    private void sendDataToUnifiedService(String data) {
        if (data == null || data.isEmpty()) {
            Log.e(TAG, "Received empty or null data. Skipping broadcast.");
            return;
        }

        Intent intent = new Intent("com.example.stress_detection_app.BLUETOOTH_DATA");
        intent.putExtra("data", data);
        sendBroadcast(intent);
        Log.d(TAG, "Broadcasted data to UnifiedService: " + data);
    }

    private void disconnectBluetooth() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
            Log.d(TAG, "Bluetooth disconnected successfully.");
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting Bluetooth: " + e.getMessage(), e);
        }
    }
}
