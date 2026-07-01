package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.BuildConfig;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.android.volley.Request;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private Activity activity;
    private ProgressBar setProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        define();
    }

    private void define() {
        activity = this;
        UConfig uConfig = new UConfig(activity);
        uConfig.setBoolean(Constant.IS_TEST_ADS, false);
        if (!uConfig.getBoolean(Constant.IS_SET_VOICE_SPEED)) {
            uConfig.setData(Constant.VOICE_SPEED, "normal");
            uConfig.setBoolean(Constant.IS_SET_VOICE_SPEED, true);
        }
        setProgressBar = findViewById(R.id.setProgressBarId);
        initialWork();
    }

    private void initialWork() {
        Thread thread = new Thread(() -> {
            setProgressStage();
            runOnUiThread(this::checkAppVersion);
        });
        thread.start();
    }

    private void checkAppVersion() {
        String url = Constant.ROOT_API2 + "settings";
        ApiConfig.RequestToVolley((result, response, error) -> {
            if (result) {
                try {
                    JSONObject settings = new JSONObject(response).getJSONObject("settings");
                    String serverVersion = settings.optString("app_version", "");
                    String updateText = settings.optString("app_version_text", "A new update is available. Please update now.");
                    String currentVersion = BuildConfig.VERSION_NAME;

                    if (!serverVersion.isEmpty() && !serverVersion.equals(currentVersion)) {
                        showUpdateDialog(updateText);
                    } else {
                        goToNext();
                    }
                } catch (Exception e) {
                    Log.d("SplashVersion", Objects.requireNonNull(e.getMessage()));
                    goToNext();
                }
            } else {
                goToNext();
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), false);
    }

    private void showUpdateDialog(String message) {
        new AlertDialog.Builder(activity)
                .setTitle("Update Available")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Update Now", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + getPackageName())));
                    } catch (Exception e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                })
                .setNegativeButton("Later", (dialog, which) -> goToNext())
                .show();
    }

    private void goToNext() {
        handleNotificationIntent(getIntent());
    }

    private void handleNotificationIntent(Intent intent) {
        String type = intent.getStringExtra("type");
        String slug = intent.getStringExtra("slug");

        Intent startIntent;
        if ("item".equals(type) && slug != null) {
            startIntent = new Intent(activity, ItemDetailsActivity.class);
            startIntent.putExtra(Constant.FROM, slug);
        } else {
            startIntent = new Intent(activity, MainActivity.class);
        }

        startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startIntent);
    }

    private void setProgressStage() {
        for (int progress = 1; progress <= 21; progress++) {
            try {
                Thread.sleep(10);
                setProgressBar.setProgress(progress);
            } catch (Exception e) {
                Log.d("setProgress", Objects.requireNonNull(e.getMessage()));
            }
        }
    }
}