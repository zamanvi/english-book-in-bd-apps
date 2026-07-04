package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.BookActivity;
import com.abmn.englishhub.Activity.SocialLinkActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String[] DAY_LABELS = {"M", "T", "W", "T", "F", "S", "S"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);
            loadStats(view);
            buildWeekCalendar(view);
            wireMenuItems(view);
            return view;
        } catch (Throwable t) {
            android.widget.Toast.makeText(getContext(),
                "Profile error: " + t.getClass().getSimpleName() + ": " + t.getMessage(),
                android.widget.Toast.LENGTH_LONG).show();
            return new android.view.View(getContext());
        }
    }

    // ── Stats ────────────────────────────────────────────────────

    private void loadStats(View view) {
        Activity activity = getActivity();
        if (activity == null) return;

        UConfig uConfig = new UConfig(activity);
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        // Name
        String name = uConfig.getData("name");
        TextView nameTV = view.findViewById(R.id.profileNameTV);
        if (name != null && !name.isEmpty()) {
            nameTV.setText(name);
            // Level label based on XP
            int xp = prefs.getInt(Constant.TOTAL_XP, 0);
            TextView levelTV = view.findViewById(R.id.profileLevelTV);
            levelTV.setText(xpToLevel(xp));
        }

        // XP / Streak / Rank
        int xp     = prefs.getInt(Constant.TOTAL_XP, 0);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        int rank   = prefs.getInt(Constant.USER_RANK, 0);

        ((TextView) view.findViewById(R.id.profileXpTV)).setText(String.valueOf(xp));
        ((TextView) view.findViewById(R.id.profileStreakTV)).setText(String.valueOf(streak));
        ((TextView) view.findViewById(R.id.profileRankTV)).setText(rank > 0 ? "#" + rank : "—");
    }

    private String xpToLevel(int xp) {
        if (xp >= 500) return "🏆 চ্যাম্পিয়ন";
        if (xp >= 200) return "⭐ অ্যাডভান্সড";
        if (xp >= 80)  return "📈 মধ্যবর্তী";
        if (xp >= 20)  return "🌱 শিক্ষানবিশ";
        return "ইংরেজি শিক্ষার্থী";
    }

    // ── 7-day streak calendar ─────────────────────────────────────

    private void buildWeekCalendar(View view) {
        Activity activity = getActivity();
        if (activity == null) return;

        LinearLayout row = view.findViewById(R.id.weekDaysRow);
        TextView weekLabel = view.findViewById(R.id.weekStreakLabelTV);
        row.removeAllViews();

        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String lastPlayed = prefs.getString(Constant.LAST_PLAYED_DATE, "");
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);

        // Build which days in current Mon–Sun week are "played"
        Calendar cal = Calendar.getInstance();
        int todayDow = cal.get(Calendar.DAY_OF_WEEK); // 1=Sun,2=Mon...7=Sat
        // Convert to Mon=0 … Sun=6
        int todayIdx = (todayDow + 5) % 7;

        // Mark days active: today and the streak days before today
        int activeDays = 0;
        float dpScale = getResources().getDisplayMetrics().density;
        int dotSize   = (int) (34 * dpScale);
        int margin    = (int) (4 * dpScale);

        for (int i = 0; i < 7; i++) {
            boolean isToday  = (i == todayIdx);
            boolean isPlayed = (i <= todayIdx) && (todayIdx - i < streak);
            boolean isFuture = (i > todayIdx);

            if (isPlayed) activeDays++;

            // Outer container
            LinearLayout cell = new LinearLayout(activity);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams cellLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            cell.setLayoutParams(cellLp);

            // Day label (M T W T F S S)
            TextView dayLabel = new TextView(activity);
            dayLabel.setText(DAY_LABELS[i]);
            dayLabel.setTextSize(9);
            dayLabel.setGravity(android.view.Gravity.CENTER);
            dayLabel.setTextColor(isToday
                    ? Color.parseColor("#8B7FFF")
                    : Color.parseColor("#2E3060"));
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            labelLp.bottomMargin = (int) (4 * dpScale);
            dayLabel.setLayoutParams(labelLp);
            cell.addView(dayLabel);

            // Dot / circle
            CardView dot = new CardView(activity);
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
            dotLp.leftMargin = margin / 2;
            dotLp.rightMargin = margin / 2;
            dot.setLayoutParams(dotLp);
            dot.setRadius(dotSize / 2f);
            dot.setCardElevation(0);

            TextView dotText = new TextView(activity);
            dotText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            dotText.setGravity(android.view.Gravity.CENTER);
            dotText.setTextSize(14);

            if (isPlayed) {
                dot.setCardBackgroundColor(Color.parseColor("#00E8B8")); // teal
                dotText.setText("✓");
                dotText.setTextColor(Color.parseColor("#07081A"));
            } else if (isToday) {
                dot.setCardBackgroundColor(Color.parseColor("#8B7FFF")); // indigo
                dotText.setText("•");
                dotText.setTextColor(Color.WHITE);
            } else if (isFuture) {
                dot.setCardBackgroundColor(Color.parseColor("#0F1030")); // stroke
                dotText.setText("");
            } else {
                dot.setCardBackgroundColor(Color.parseColor("#0F1030")); // missed
                dotText.setText("✕");
                dotText.setTextColor(Color.parseColor("#FF3F6C"));
                dotText.setTextSize(10);
            }

            dot.addView(dotText);
            cell.addView(dot);
            row.addView(cell);
        }

        weekLabel.setText(activeDays + " / 7 Days");
    }

    // ── Existing menu items (all logic preserved) ─────────────────

    private void wireMenuItems(View view) {
        Activity activity = getActivity();
        if (activity == null) return;

        view.findViewById(R.id.menuBookCV).setOnClickListener(v ->
                startActivity(new Intent(activity, BookActivity.class)));

        view.findViewById(R.id.menuVocabularyCV).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyActivity.class)));

        view.findViewById(R.id.menuSocialMediaCV).setOnClickListener(v ->
                startActivity(new Intent(activity, SocialLinkActivity.class)));

        view.findViewById(R.id.menuShareCV).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = getString(R.string.vocabulary_https_play_google_com_store_apps_details_id)
                    + activity.getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        view.findViewById(R.id.menuUpdateCV).setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                activity.startActivity(new Intent(Intent.ACTION_VIEW, uri)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        view.findViewById(R.id.menuReviewCV).setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                activity.startActivity(new Intent(Intent.ACTION_VIEW, uri)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        view.findViewById(R.id.menuContactCV).setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("যোগাযোগ করো")
                    .setMessage("norozzaman996@gmail.com")
                    .setPositiveButton("কপি করো", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager)
                                activity.getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Email", "norozzaman996@gmail.com"));
                            Toast.makeText(activity, "ইমেইল কপি হয়েছে", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("বাতিল", null)
                    .show();
        });
    }
}
