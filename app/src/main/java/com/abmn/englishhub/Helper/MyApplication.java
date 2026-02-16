package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.util.Log;

import com.abmn.utility.Core.Config;

import java.util.Objects;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Config.setProgressColors(
            Color.parseColor("#FF5722"), // Start color
            Color.parseColor("#FFC107"), // Center color
            Color.parseColor("#4CAF50")  // End color
        );
    }
    public static String getPackageInfo(Activity activity){
        String version = "";
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            version = pInfo.versionName; // Get version number
            Log.d("AppVersion", "Version: " + version);
        } catch (Exception e) {
            Log.d("AppVersion", Objects.requireNonNull(e.getMessage()));
        }
        return version;
    }
}
