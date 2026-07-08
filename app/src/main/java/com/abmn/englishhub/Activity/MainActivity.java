package com.abmn.englishhub.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import com.abmn.englishhub.Helper.StreakReminderReceiver;
import com.abmn.englishhub.R;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.fragment.HomeFragment;
import com.abmn.englishhub.fragment.LearnFragment;
import com.abmn.englishhub.fragment.NoticeFragment;
import com.abmn.englishhub.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
                .addOnCompleteListener(task -> {});

        // Schedule daily 8pm streak reminder if not already played today
        StreakReminderReceiver.schedule(this);

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
                fragment = new LearnFragment();
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
        } else if (fragment instanceof LearnFragment) {
            bottomNav.setSelectedItemId(R.id.navEducation);
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