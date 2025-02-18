package com.example.stress_detection_app.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Locale;
import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class SoundLevelService extends Service {
    private static final int SAMPLE_RATE = 44100;
    private static final int COLLECTION_INTERVAL_MS = 3000;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private static double currentDecibels;
    private Handler handler = new Handler();
    private Runnable updateSoundLevelRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        startSoundLevelMonitoring();
    }

    private void startSoundLevelMonitoring() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRecord.startRecording();
            isRecording = true;
            updateSoundLevelRunnable = this::updateSoundLevel;
            handler.post(updateSoundLevelRunnable);
            Log.d("SoundLevelService", "Sound level monitoring started.");
        } else {
            Log.e("SoundLevelService", "Failed to initialize AudioRecord.");
        }
    }

    private void updateSoundLevel() {
        if (isRecording) {
            short[] buffer = new short[4096];
            int read = audioRecord.read(buffer, 0, buffer.length);

            if (read > 0) {
                double sum = 0;
                for (short sample : buffer) {
                    sum += sample * sample;
                }
                double amplitude = Math.sqrt(sum / read);
                if (amplitude < 1) amplitude = 1; // Avoid division by zero
                currentDecibels = 20 * Math.log10(amplitude);

                // Send the noise level to UnifiedService
                Intent intent = new Intent("com.example.stress_detection_app.NOISE_LEVEL");
                intent.putExtra("noise_level", currentDecibels);
                sendBroadcast(intent);
            }

            handler.postDelayed(updateSoundLevelRunnable, COLLECTION_INTERVAL_MS);
        }
    }

    public static double getCurrentDecibels() {
        return currentDecibels;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSoundLevelMonitoring();
    }

    private void stopSoundLevelMonitoring() {
        if (audioRecord != null && isRecording) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        handler.removeCallbacks(updateSoundLevelRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
