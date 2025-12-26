package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

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
        if (!uConfig.getBoolean(Constant.IS_SET_VOICE_SPEED)){
            uConfig.setData(Constant.VOICE_SPEED, "normal");
            uConfig.setBoolean(Constant.IS_SET_VOICE_SPEED, true);
        }
        setProgressBar = findViewById(R.id.setProgressBarId);
        initialWork();
    }

    private void initialWork() {
        Thread thread = new Thread(() -> {
            setProgressStage();
            work();
        });
        thread.start();
    }

    private void work() {
        Intent intent;
        intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setProgressStage() {
        int progress;
        for (progress = 1; progress <= 101; progress = progress + 1) {
            try {
                Thread.sleep(20);
                setProgressBar.setProgress(progress);
            } catch (Exception e) {
                Log.d("setProgress", Objects.requireNonNull(e.getMessage()));
            }
        }
    }
}