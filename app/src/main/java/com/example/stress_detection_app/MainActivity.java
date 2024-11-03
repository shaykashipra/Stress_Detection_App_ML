package com.example.stress_detection_app;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private CardView notificationcountcard, batteryCard, phoneringerCard, sensorCard, soundCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


            // Initialize the CardViews
            notificationcountcard = findViewById(R.id.NotificationCountCard);
            batteryCard = findViewById(R.id.batteryCard);
        phoneringerCard = findViewById(R.id.phoneringerCard);
            sensorCard = findViewById(R.id.sensorCard);
         soundCard = findViewById(R.id.soundCard);


        // Set click listeners for each CardView
            notificationcountcard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, NotificationCountActivity.class);
                startActivity(intent);
            });

            batteryCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, BatteryActivity.class);
                startActivity(intent);
            });

        phoneringerCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PhoneRingerActivity.class);
                startActivity(intent);
            });
//
            sensorCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SensorActivity.class);
                startActivity(intent);
            });
        soundCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SoundLevelActivity.class);
            startActivity(intent);
        });
        }


    }


