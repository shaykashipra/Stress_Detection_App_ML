package com.example.stress_detection_app;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Gyroscope extends AppCompatActivity {

    private LineChart gyroscopeChart;
    private DatabaseReference databaseReference;
    FirebaseUser user;
    private ArrayList<Entry> gxEntries, gyEntries, gzEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gyroscope);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gyroscopeChart=findViewById(R.id.gyroscopeChart);

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()); // Example: "20240217"
        fetchGyroscopeData(todayDate);
        //Log.d("Debug", "xEntries: " + xEntries.toString());
        // accelerometerData.setText(UnifiedService.keyQueue.size());
        updateGyroGraph();
    }

    private void fetchGyroscopeData(String selectedDate) {
        gxEntries.clear();
        gyEntries.clear();
        gzEntries.clear();
        //gyroscopeData.setText("Fetching...");

        // 🔥 Ensure user is logged in
        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            //gyroscopeData.setText("User not logged in!");
            return;
        }

        // 🔥 Reference the selected date node in Firebase
        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    //gyroscopeData.setText("No data found for this date!");
                    return;
                }

                // 🔥 Loop through all unique IDs under the selected date
                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        // ✅ Extract JSON data
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // ✅ Extract Gyroscope values
                        float gx = (float) jsonObject.getDouble("gyroscopeX");
                        float gy = (float) jsonObject.getDouble("gyroscopeY");
                        float gz = (float) jsonObject.getDouble("gyroscopeZ");

                        // ✅ Update Graph Data
                        float index = gxEntries.size();
                        gxEntries.add(new Entry(index, gx));
                        gyEntries.add(new Entry(index, gy));
                        gzEntries.add(new Entry(index, gz));

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // ✅ Update UI and graph after all data is fetched
                runOnUiThread(() -> {
                    //gyroscopeData.setText("Data fetched for date: " + selectedDate);
                    updateGyroGraph(); // 🔥 Update gyroscope graph
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }



    private void updateGyroGraph() {
        if (gxEntries.isEmpty() || gyEntries.isEmpty() || gzEntries.isEmpty()) {
            Log.e("Graph", "No gyroscope data to update graph!");
            return;
        }

        LineDataSet gxDataSet = new LineDataSet(gxEntries, "Gyro X-Axis");
        gxDataSet.setColor(Color.RED);
        gxDataSet.setValueTextSize(10f);
        gxDataSet.setDrawCircles(false);
        gxDataSet.setDrawValues(false);

        LineDataSet gyDataSet = new LineDataSet(gyEntries, "Gyro Y-Axis");
        gyDataSet.setColor(Color.GREEN);
        gyDataSet.setValueTextSize(10f);
        gyDataSet.setDrawCircles(false);
        gyDataSet.setDrawValues(false);

        LineDataSet gzDataSet = new LineDataSet(gzEntries, "Gyro Z-Axis");
        gzDataSet.setColor(Color.BLUE);
        gzDataSet.setValueTextSize(10f);
        gzDataSet.setDrawCircles(false);
        gzDataSet.setDrawValues(false);

        LineData lineData = new LineData(gxDataSet, gyDataSet, gzDataSet);
        gyroscopeChart.setData(lineData);

        // Customize chart
        XAxis xAxis = gyroscopeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = gyroscopeChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        gyroscopeChart.getAxisRight().setEnabled(false);

        Legend legend = gyroscopeChart.getLegend();
        legend.setTextSize(12f);

        gyroscopeChart.invalidate(); // 🔥 Refresh the graph
    }

}