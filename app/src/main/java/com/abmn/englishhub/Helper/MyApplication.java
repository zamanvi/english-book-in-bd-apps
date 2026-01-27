package com.abmn.englishhub.Helper;

import android.Manifest;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.abmn.utility.Core.Config;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Config.setProgressColors(
            Color.parseColor("#FF5722"), // Start color
            Color.parseColor("#FFC107"), // Center color
            Color.parseColor("#4CAF50")  // End color
        );

        FirebaseMessaging.getInstance()
                .subscribeToTopic("abmnmenglish")
                .addOnCompleteListener(task -> {
                    Log.d("FCM", task.isSuccessful()
                            ? "Subscribed to topic"
                            : "Topic subscription failed");
                });

    }

}
