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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.LoginActivity;
import com.abmn.englishhub.Activity.SocialLinkActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.LevelHelper;
import com.abmn.englishhub.R;
import android.widget.ProgressBar;
import com.abmn.utility.UConfig;

import java.util.Calendar;

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

        // Show login prompt if not logged in
        String token = uConfig.getData(Constant.TOKEN);
        TextView nameTV = view.findViewById(R.id.profileNameTV);
        if (token == null || token.isEmpty()) {
            nameTV.setText("লগইন করোনি");
            nameTV.setOnClickListener(v ->
                    startActivity(new Intent(activity, LoginActivity.class)));
        }

        // Name
        String name = uConfig.getData("name");
        if (name != null && !name.isEmpty()) {
            nameTV.setText(name);
            int xp = prefs.getInt(Constant.TOTAL_XP, 0);
            TextView levelTV = view.findViewById(R.id.profileLevelTV);
            levelTV.setText(LevelHelper.getLevelTitle(xp));
        }

        // User ID — tap to copy (needed to challenge in battle)
        android.view.View userIdLayout = view.findViewById(R.id.profileUserIdTV);
        TextView userIdText = view.findViewById(R.id.profileUserIdText);
        int userId = prefs.getInt("user_id", 0);
        if (userId > 0) {
            userIdText.setText("ID: " + userId);
            userIdLayout.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("user_id", String.valueOf(userId)));
                Toast.makeText(activity, "✅ ID " + userId + " কপি হয়েছে!", Toast.LENGTH_SHORT).show();
            });
        } else {
            userIdLayout.setVisibility(android.view.View.GONE);
        }

        // XP / Streak / Rank / Lipto
        int xp     = prefs.getInt(Constant.TOTAL_XP, 0);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        int rank   = prefs.getInt(Constant.USER_RANK, 0);
        int lipto  = prefs.getInt(Constant.LIPTO_BALANCE, 0);

        ((TextView) view.findViewById(R.id.profileXpTV)).setText(String.valueOf(xp));
        ((TextView) view.findViewById(R.id.profileStreakTV)).setText(String.valueOf(streak));
        ((TextView) view.findViewById(R.id.profileRankTV)).setText(rank > 0 ? "#" + rank : "—");
        ((TextView) view.findViewById(R.id.profileLiptoTV)).setText(String.valueOf(lipto));

        // Level progress bar
        ((TextView) view.findViewById(R.id.profileLevelLabelTV)).setText(LevelHelper.getLevelLabel(xp));
        int toNext = LevelHelper.getXpToNextLevel(xp);
        ((TextView) view.findViewById(R.id.profileXpToNextTV)).setText(
                toNext > 0 ? toNext + " XP to next level" : "Max Level! 🏆");
        ((ProgressBar) view.findViewById(R.id.profileLevelProgressBar))
                .setProgress(LevelHelper.getLevelProgressPercent(xp));
    }


    // ── 7-day streak calendar ─────────────────────────────────────

    private void buildWeekCalendar(View view) {
        Activity activity = getActivity();
        if (activity == null) return;

        LinearLayout row = view.findViewById(R.id.weekDaysRow);
        TextView weekLabel = view.findViewById(R.id.weekStreakLabelTV);
        row.removeAllViews();

        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
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
                    ? ContextCompat.getColor(activity, R.color.active_color)
                    : ContextCompat.getColor(activity, R.color.inactive_color));
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
                dot.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.green));
                dotText.setText("✓");
                dotText.setTextColor(ContextCompat.getColor(activity, R.color.bg_deep));
            } else if (isToday) {
                dot.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.active_color));
                dotText.setText("•");
                dotText.setTextColor(Color.WHITE);
            } else if (isFuture) {
                dot.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.bg_stroke));
                dotText.setText("");
            } else {
                dot.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.bg_stroke)); // missed
                dotText.setText("✕");
                dotText.setTextColor(ContextCompat.getColor(activity, R.color.red_wrong));
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

        view.findViewById(R.id.menuSocialMediaCV).setOnClickListener(v ->
                startActivity(new Intent(activity, SocialLinkActivity.class)));

        view.findViewById(R.id.menuShareCV).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "📚 আমি English Grammar Book অ্যাপ দিয়ে ইংরেজি শিখছি!\n\n"
                    + "এখানে আছে ৫০০০+ শব্দ, সম্পূর্ণ গ্রামার কোর্স, quiz আর মজার সব গল্প।\n\n"
                    + "তুমিও শেখা শুরু করো 👇\n"
                    + "https://play.google.com/store/apps/details?id=" + activity.getPackageName()
                    + "\n#EnglishGrammarBook #LearnEnglish";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "শেয়ার করো"));
        });

        view.findViewById(R.id.menuUpdateCV).setOnClickListener(v -> openPlayStoreListing(activity));

        view.findViewById(R.id.menuReviewCV).setOnClickListener(v -> openPlayStoreListing(activity));

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

    private void openPlayStoreListing(Activity activity) {
        try {
            Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName());
            activity.startActivity(new Intent(Intent.ACTION_VIEW, uri)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
