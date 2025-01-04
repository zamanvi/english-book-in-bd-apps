package com.abmn.englishhub.Helper;

import android.app.Application;
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

}
