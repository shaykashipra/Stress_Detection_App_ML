package com.example.stress_detection_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class SoundLevelActivity extends AppCompatActivity {
    private static final int REQUEST_MICROPHONE = 1;
    private static final int SAMPLE_RATE = 44100;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private TextView soundLevelTextView;
    private Handler handler;
    private Runnable updateSoundLevel;
    private static final int COLLECTION_INTERVAL_MS = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_level);

        soundLevelTextView = findViewById(R.id.soundLevelTextView);

        // Initialize the handler before any use
        handler = new Handler();
        updateSoundLevel = this::updateSoundLevel;

        // Check for microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.startRecording();
                isRecording = true;
                handler.post(updateSoundLevel);
                Log.d("SoundLevelActivity", "Recording started successfully.");
            } else {
                throw new IllegalStateException("Failed to initialize AudioRecord.");
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException | IllegalArgumentException e) {
            Toast.makeText(this, "AudioRecord initialization failed. Microphone may not be available.", Toast.LENGTH_SHORT).show();
            Log.e("SoundLevelActivity", "Error initializing AudioRecord: ", e);
        }
    }

    private void updateSoundLevel() {
        try {
            if (isRecording) {
                short[] buffer = new short[4096];
                int read = audioRecord.read(buffer, 0, buffer.length);

                if (read > 0) {
                    double sum = 0;
                    for (short sample : buffer) {
                        sum += sample * sample;
                    }
                    double amplitude = Math.sqrt(sum / read);
                    if (amplitude < 1) amplitude = 1;
                    double decibels = 20 * Math.log10(amplitude);

                    // Display the noise level
                    soundLevelTextView.setText(String.format(Locale.getDefault(), "Noise Level: %.2f dB", decibels));

                    // TODO: Store the decibel value and timestamp in a dataset for further analysis
                }

                // Schedule the next update after the specified interval
                handler.postDelayed(updateSoundLevel, COLLECTION_INTERVAL_MS);
            }
        } catch (IllegalStateException e) {
            Toast.makeText(this, "Error reading sound level. Recording may have stopped.", Toast.LENGTH_SHORT).show();
            Log.e("SoundLevelActivity", "Error reading sound level: ", e);
            stopRecording();
        }
    }

    private void stopRecording() {
        try {
            if (audioRecord != null && isRecording) {
                isRecording = false;
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            handler.removeCallbacks(updateSoundLevel);
        } catch (IllegalStateException e) {
            Log.e("SoundLevelActivity", "Error stopping AudioRecord: ", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Added super call
        if (requestCode == REQUEST_MICROPHONE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, "Microphone permission is required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
