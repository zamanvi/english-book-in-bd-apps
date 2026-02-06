package com.abmn.englishhub.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.abmn.englishhub.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.fragment.HomeFragment;
import com.abmn.englishhub.fragment.NoticeFragment;
import com.abmn.englishhub.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Activity activity;
    private BottomNavigationView bottomNav;
    private boolean doubleBackToExitPressedOnce = false;
    private boolean isNavigatingProgrammatically = false;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        define();
    }

    private void define() {
        activity = this;

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
        }

        FirebaseMessaging.getInstance()
                .subscribeToTopic("abmnmenglish")
                .addOnCompleteListener(task -> {
                    Log.d("FCM", task.isSuccessful()
                            ? "Subscribed to topic"
                            : "Topic subscription failed");
                });


        fragment = new HomeFragment();

        bottomNav = findViewById(R.id.bottomNav);

        loadFragment(fragment);

        bottomNav.setOnItemSelectedListener(item -> {
            if (isNavigatingProgrammatically) return true;
            int id = item.getItemId();
            fragment = null;
            if (id == R.id.navHome) {
                fragment = new HomeFragment();
            } else if (id == R.id.navEducation) {
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "writing_reading"));
            } else if (id == R.id.navPlus) {
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar"));
            } else if (id == R.id.navNotice) {
                fragment = new NoticeFragment();
            } else if (id == R.id.navProfile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });

    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit();

        isNavigatingProgrammatically = true;
        if (fragment instanceof HomeFragment) {
            bottomNav.setSelectedItemId(R.id.navHome);
        } else if (fragment instanceof NoticeFragment) {
            bottomNav.setSelectedItemId(R.id.navNotice);
        } else if (fragment instanceof ProfileFragment) {
            bottomNav.setSelectedItemId(R.id.navProfile);
        }
        isNavigatingProgrammatically = false;
    }
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

}