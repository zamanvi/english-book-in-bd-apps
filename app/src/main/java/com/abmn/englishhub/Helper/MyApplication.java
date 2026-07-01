package com.abmn.englishhub.Helper;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import com.abmn.utility.Core.Config;

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
            version = pInfo.versionName;
        } catch (Exception e) {
            // ignored
        }
        return version;
    }
}
