package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.abmn.englishhub.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Activity activity;
    private DrawerLayout drawer;

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

        drawer = findViewById(R.id.drawer_layout);

//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawer, toolbar, R.string.open, R.string.close);
//        toggle.syncState();

        @SuppressLint("CutPasteId")
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        TextView listeningTV = findViewById(R.id.listeningTV);

        listeningTV.setOnClickListener(view -> startActivity(new Intent(activity, ChapterActivity.class)));

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_book) {
            startActivity(new Intent(activity, BookActivity.class));
            drawer.close();
            return true;
        }
        if (item.getItemId() == R.id.nav_vocabulary) {
            startActivity(new Intent(activity, VocabularyActivity.class));
            drawer.close();
            return true;
        }
        if (item.getItemId() == R.id.nav_share_id) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = getString(R.string.vocabulary_https_play_google_com_store_apps_details_id) + activity.getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            activity.startActivity(Intent.createChooser(shareIntent, "Share via"));
            drawer.close();
            return true;
        }
        if (item.getItemId() == R.id.nav_others_app_id) {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=MD+,+norozzaman")));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "Unable to open store", Toast.LENGTH_SHORT).show();
            }
            drawer.close();
            return true;
        }
        if (item.getItemId() == R.id.nav_review_id) {
            try {
                Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
            drawer.close();
            return true;
        }
        if (item.getItemId() == R.id.nav_contact_id) {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Contact Us")
                    .setMessage("norozzaman996@gmail.com")
                    .setPositiveButton("Copy", (dialog, which) -> {
                        Context context = activity.getApplicationContext(); // Use application context
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            ClipData clip = ClipData.newPlainText("Email", "norozzaman996@gmail.com");
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Clipboard not available", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(true)
                    .show();
            drawer.close();
            return true;
        }
        return false;
    }
}