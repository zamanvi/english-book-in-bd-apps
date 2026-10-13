package com.abmn.englishhub.Activity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.OfflineCache;
import com.abmn.englishhub.Helper.OfflineDownloadManager;
import com.abmn.englishhub.Helper.StatsRefreshGate;
import com.abmn.englishhub.Helper.ThemePrefs;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;

/**
 * Settings: the app-level rows (offline content, update, theme, rating,
 * contact, social, logout) that used to be 8 menu rows at the bottom of the
 * Profile tab. Behaviour is the code that lived in ProfileFragment, moved
 * as-is; only fragment lifecycle checks became Activity ones.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String CONTACT_EMAIL = "norozzaman996@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("সেটিংস");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        wireMenuItems();
        wireOfflineContentSection();
    }

    private boolean alive() {
        return !isFinishing() && !isDestroyed();
    }

    // ── Menu rows ─────────────────────────────────────────────────

    private void wireMenuItems() {
        findViewById(R.id.menuSocialMediaCV).setOnClickListener(v ->
                startActivity(new Intent(this, SocialLinkActivity.class)));

        findViewById(R.id.menuUpdateCV).setOnClickListener(v -> openPlayStoreListing());
        findViewById(R.id.menuReviewCV).setOnClickListener(v -> openPlayStoreListing());
        findViewById(R.id.menuThemeCV).setOnClickListener(v -> openThemePicker());

        // Logout only makes sense for a signed-in user.
        View logoutRow = findViewById(R.id.menuLogoutCV);
        String token = new UConfig(this).getData(Constant.TOKEN);
        boolean loggedIn = token != null && !token.isEmpty();
        logoutRow.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        logoutRow.setOnClickListener(v ->
                new AlertDialog.Builder(this, R.style.Theme_App_Dialog)
                        .setTitle("লগআউট করবে?")
                        .setMessage("তোমার account থেকে বের হয়ে যাবে।")
                        .setPositiveButton("লগআউট করো", (dialog, which) -> performLogout())
                        .setNegativeButton("বাতিল", null)
                        .show());

        findViewById(R.id.menuContactCV).setOnClickListener(v ->
                new AlertDialog.Builder(this, R.style.Theme_App_Dialog)
                        .setTitle("যোগাযোগ করো")
                        .setMessage(CONTACT_EMAIL)
                        .setPositiveButton("কপি করো", (dialog, which) -> {
                            ClipboardManager clipboard = (ClipboardManager)
                                    getSystemService(Context.CLIPBOARD_SERVICE);
                            if (clipboard != null) {
                                clipboard.setPrimaryClip(ClipData.newPlainText("Email", CONTACT_EMAIL));
                                Toast.makeText(this, "ইমেইল কপি হয়েছে", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("বাতিল", null)
                        .show());
    }

    // ── Offline content: download everything the app would normally cache
    // lazily on first view, so it all works with no connection. Reuses the
    // exact same OfflineCache keys ChapterActivity/ItemActivity/
    // ItemDetailsActivity already read from - no separate storage path. ──

    private void wireOfflineContentSection() {
        MaterialButton downloadBtn = findViewById(R.id.downloadOfflineBtn);
        MaterialButton removeBtn = findViewById(R.id.removeOfflineBtn);
        TextView storageInfoTV = findViewById(R.id.offlineStorageInfoTV);

        refreshOfflineStorageInfo(storageInfoTV);
        // A download started before leaving this screen may still be running.
        if (OfflineDownloadManager.isRunning()) {
            downloadBtn.setEnabled(false);
            removeBtn.setEnabled(false);
        }

        downloadBtn.setOnClickListener(v ->
                new AlertDialog.Builder(this, R.style.Theme_App_Dialog)
                        .setTitle("সব chapters ডাউনলোড করবে?")
                        .setMessage("এটি ইন্টারনেট ডেটা ব্যবহার করবে। ডাউনলোড শেষ হলে ইন্টারনেট ছাড়াই সব lesson পড়তে পারবে।")
                        .setPositiveButton("ডাউনলোড করো", (dialog, which) ->
                                startOfflineDownload(downloadBtn, removeBtn, storageInfoTV))
                        .setNegativeButton("বাতিল", null)
                        .show());

        removeBtn.setOnClickListener(v -> {
            if (OfflineCache.getFileCount(this) == 0) {
                Toast.makeText(this, "কোনো অফলাইন কন্টেন্ট নেই", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this, R.style.Theme_App_Dialog)
                    .setTitle("অফলাইন কন্টেন্ট মুছবে?")
                    .setMessage("ডাউনলোড করা সব chapter এই ডিভাইস থেকে মুছে যাবে। পরে আবার ইন্টারনেট দিয়ে দেখতে পারবে।")
                    .setPositiveButton("মুছে ফেলো", (dialog, which) -> {
                        OfflineCache.clearAll(this);
                        refreshOfflineStorageInfo(storageInfoTV);
                        Toast.makeText(this, "অফলাইন কন্টেন্ট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("বাতিল", null)
                    .show();
        });
    }

    private void startOfflineDownload(MaterialButton downloadBtn, MaterialButton removeBtn,
                                      TextView storageInfoTV) {
        if (OfflineDownloadManager.isRunning()) return;

        downloadBtn.setEnabled(false);
        removeBtn.setEnabled(false);
        downloadBtn.setText("ডাউনলোড শুরু হচ্ছে...");

        OfflineDownloadManager.downloadAll(this, new OfflineDownloadManager.Listener() {
            @Override
            public void onProgress(int done, int total) {
                if (!alive()) return;
                downloadBtn.setText("ডাউনলোড হচ্ছে... " + done + "/" + total);
            }

            @Override
            public void onComplete(int itemCount) {
                if (!alive()) return;
                downloadBtn.setEnabled(true);
                removeBtn.setEnabled(true);
                downloadBtn.setText("⬇️ সব chapters ডাউনলোড করো");
                refreshOfflineStorageInfo(storageInfoTV);
                Toast.makeText(SettingsActivity.this,
                        itemCount + "টি lesson ডাউনলোড হয়েছে", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!alive()) return;
                downloadBtn.setEnabled(true);
                removeBtn.setEnabled(true);
                downloadBtn.setText("⬇️ সব chapters ডাউনলোড করো");
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refreshOfflineStorageInfo(TextView storageInfoTV) {
        if (storageInfoTV == null) return;
        int fileCount = OfflineCache.getFileCount(this);
        long bytes = OfflineCache.getTotalSizeBytes(this);
        if (fileCount == 0) {
            storageInfoTV.setText("💾 এখনো কিছু ডাউনলোড করা হয়নি");
        } else {
            storageInfoTV.setText("💾 " + formatSize(bytes) + " ডাউনলোড করা আছে (" + fileCount + "টি ফাইল)");
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format(java.util.Locale.US, "%.0f KB", bytes / 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // ── Session / external links (moved unchanged from ProfileFragment) ──

    // Best-effort server-side token revoke, then always clear local session
    // state - a network failure here shouldn't trap the user in a "logged
    // in" app that no longer has a working token.
    private void performLogout() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);

        if (token != null && !token.isEmpty()) {
            ApiConfig.getRequest(this, Constant.ROOT_API + "logout", token,
                    response -> {}, error -> {});
        }

        uConfig.setData(Constant.TOKEN, "");
        uConfig.setData("name", "");

        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit()
                .remove("user_id")
                .remove(Constant.TOTAL_XP)
                .remove(Constant.STREAK_DAYS)
                .remove(Constant.USER_RANK)
                .remove(Constant.LIPTO_BALANCE)
                // Per-account leftovers that used to outlive a logout: the next
                // account on this device inherited the old Lipto tier ceiling
                // and "today's quiz" lesson.
                .remove(Constant.LIPTO_MAX_BALANCE)
                .remove(Constant.LAST_QUIZ_LESSON_ID)
                .remove(Constant.LAST_QUIZ_LESSON_TITLE)
                .apply();
        StatsRefreshGate.reset();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openPlayStoreListing() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            startActivity(new Intent(Intent.ACTION_VIEW, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(new Intent(Intent.ACTION_VIEW, uri).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private void openThemePicker() {
        String[] names = ThemePrefs.paletteNames();
        int current = ThemePrefs.getHomePalette(this);
        new AlertDialog.Builder(this, R.style.Theme_App_Dialog)
                .setTitle("হোম পেইজের থিম বেছে নাও")
                .setSingleChoiceItems(names, current, (dialog, which) -> {
                    ThemePrefs.setHomePalette(this, which);
                    dialog.dismiss();
                    Toast.makeText(this, "থিম পরিবর্তন হয়েছে — Home এ ফিরে গেলে দেখতে পাবে", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
