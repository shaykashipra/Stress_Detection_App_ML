//package com.example.stress_detection_app.Listener;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import com.example.stress_detection_app.PhoneRingerActivity;
//
//public class ScreenStatusReceiver extends BroadcastReceiver {
//
//    private final PhoneRingerActivity phoneRingerActivity;
//
//    // Constructor that accepts PhoneRingerActivity reference
//    public ScreenStatusReceiver(PhoneRingerActivity activity) {
//        this.phoneRingerActivity = activity;
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // Check the action of the received intent
//        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
//            phoneRingerActivity.updateScreenStatus(true); // Update to ON
//        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
//            phoneRingerActivity.updateScreenStatus(false); // Update to OFF
//        }
//    }
//}
