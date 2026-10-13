package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.LoginActivity;
import com.abmn.englishhub.Activity.SettingsActivity;
import com.abmn.englishhub.Activity.SocialLinkActivity;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.LevelHelper;
import com.abmn.englishhub.Helper.OfflineCache;
import com.abmn.englishhub.Helper.OfflineDownloadManager;
import com.abmn.englishhub.R;
import android.widget.ProgressBar;
import com.abmn.utility.UConfig;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.yalantis.ucrop.UCrop;

import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private static final String[] DAY_LABELS = {"M", "T", "W", "T", "F", "S", "S"};

    private File pendingAvatarFile;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) launchAvatarCrop(uri);
            });

    private final ActivityResultLauncher<Intent> cropAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && pendingAvatarFile != null) {
                    uploadAvatar(pendingAvatarFile);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // MainActivity already pads its root by the system-bar insets; applying
        // them again here doubled the gap under the toolbar on every tab.
            loadStats(view);
            buildWeekCalendar(view);
            wireProfileActions(view);
            bindAvatarPicker(view);
            return view;
        } catch (Throwable t) {
            android.widget.Toast.makeText(getContext(),
                "Profile error: " + t.getClass().getSimpleName() + ": " + t.getMessage(),
                android.widget.Toast.LENGTH_LONG).show();
            return new android.view.View(getContext());
        }
    }

    // ── Lipto tier badge + progress ring ──────────────────────────

    private void bindLiptoTier(View view, Activity activity, int lipto) {
        String tierName = com.abmn.englishhub.Helper.LiptoTierHelper.getTierName(lipto);
        String emoji    = com.abmn.englishhub.Helper.LiptoTierHelper.getTierEmoji(lipto);
        int tierColor    = com.abmn.englishhub.Helper.LiptoTierHelper.getTierColor(activity, lipto);
        int progress     = com.abmn.englishhub.Helper.LiptoTierHelper.getProgressPercent(lipto);
        int nextThreshold = com.abmn.englishhub.Helper.LiptoTierHelper.getNextThreshold(lipto);
        String nextTierName = com.abmn.englishhub.Helper.LiptoTierHelper.getNextTierName(lipto);

        ((TextView) view.findViewById(R.id.liptoTierEmojiTV)).setText(emoji);
        ((TextView) view.findViewById(R.id.liptoTierNameTV)).setText(tierName + " Tier");

        TextView progressTV = view.findViewById(R.id.liptoTierProgressTV);
        if (nextThreshold == -1) {
            progressTV.setText("সর্বোচ্চ ধাপে পৌঁছে গেছো! 🏆");
        } else {
            progressTV.setText("পরবর্তী ধাপ: " + nextTierName + " (" + nextThreshold + " Lipto)");
        }

        com.google.android.material.progressindicator.CircularProgressIndicator cpi =
                view.findViewById(R.id.liptoTierProgressCPI);
        cpi.setIndicatorColor(tierColor);
        cpi.setProgress(progress);
    }

    // ── Avatar upload ──────────────────────────────────────────────

    private void bindAvatarPicker(View view) {
        Activity activity = getActivity();
        if (activity == null) return;

        View.OnClickListener launchPicker = v -> pickAvatarLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());

        view.findViewById(R.id.profileAvatarEditBtn).setOnClickListener(launchPicker);
    }

    private void launchAvatarCrop(Uri sourceUri) {
        Activity activity = getActivity();
        if (activity == null) return;

        pendingAvatarFile = new File(activity.getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
        Uri destUri = FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".fileprovider", pendingAvatarFile);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(80);
        options.setToolbarColor(ContextCompat.getColor(activity, R.color.bg_card));
        options.setStatusBarColor(ContextCompat.getColor(activity, R.color.bg_deep));
        options.setToolbarWidgetColor(ContextCompat.getColor(activity, R.color.text_primary));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(activity, R.color.indigo));
        options.setToolbarTitle("প্রোফাইল ছবি ক্রপ করো");
        options.setHideBottomControls(true);

        Intent cropIntent = UCrop.of(sourceUri, destUri)
                .withAspectRatio(1, 1)
                // Small on purpose - this is just a circular avatar, never
                // shown larger than a thumbnail, so there's no reason to let
                // the upload balloon in size or slow the app down.
                .withMaxResultSize(512, 512)
                .withOptions(options)
                .getIntent(activity);
        cropAvatarLauncher.launch(cropIntent);
    }

    private void uploadAvatar(File file) {
        Activity activity = getActivity();
        if (activity == null) return;
        String token = new UConfig(activity).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        Toast.makeText(activity, "আপলোড হচ্ছে...", Toast.LENGTH_SHORT).show();

        ApiConfig.uploadFile(Constant.PROFILE_PHOTO_UPLOAD, token, file, "photo", response -> {
            try {
                JSONObject r = new JSONObject(response);
                if ("success".equals(r.optString("status"))) {
                    String photoUrl = r.optString("profile_photo", "");
                    activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            .edit().putString("profile_photo", photoUrl).apply();
                    Toast.makeText(activity, "✅ প্রোফাইল ছবি পরিবর্তন হয়েছে!", Toast.LENGTH_SHORT).show();
                    loadAvatarImage(getView(), photoUrl);
                } else {
                    Toast.makeText(activity, "ছবি আপলোড করা যায়নি", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "ছবি আপলোড করা যায়নি", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(activity, "নেটওয়ার্ক সমস্যা, আবার চেষ্টা করো", Toast.LENGTH_SHORT).show());
    }

    private void loadAvatarImage(View view, String url) {
        if (view == null || url == null || url.isEmpty()) return;
        ImageView iv = view.findViewById(R.id.profileAvatarIV);
        TextView emoji = view.findViewById(R.id.profileAvatarTV);
        if (iv == null || emoji == null) return;
        Glide.with(view).load(url).into(iv);
        iv.setVisibility(View.VISIBLE);
        emoji.setVisibility(View.GONE);
    }

    // ── Stats ────────────────────────────────────────────────────

    private void loadStats(View view) {
        Activity activity = getActivity();
        if (activity == null) return;

        UConfig uConfig = new UConfig(activity);
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        // Guest vs logged-in: swap the whole hero block instead of just the
        // name text - a guest seeing "0 XP / 0 Days / 0 Rank" everywhere
        // below reads like a bug, not "you're not logged in". Hide those
        // rows entirely and replace the hero content with a real CTA.
        String token = uConfig.getData(Constant.TOKEN);
        boolean loggedIn = token != null && !token.isEmpty();

        TextView avatarTV = view.findViewById(R.id.profileAvatarTV);
        View loggedInInfoLL = view.findViewById(R.id.loggedInInfoLL);
        View guestAccountCtaLL = view.findViewById(R.id.guestAccountCtaLL);
        View statsRowLL = view.findViewById(R.id.statsRowLL);
        View streakCalendarCV = view.findViewById(R.id.streakCalendarCV);

        if (!loggedIn) {
            avatarTV.setText("🔐");
            loggedInInfoLL.setVisibility(android.view.View.GONE);
            guestAccountCtaLL.setVisibility(android.view.View.VISIBLE);
            statsRowLL.setVisibility(android.view.View.GONE);
            streakCalendarCV.setVisibility(android.view.View.GONE);
            view.findViewById(R.id.liptoBannerCV).setVisibility(android.view.View.GONE);
            view.findViewById(R.id.liptoTierCV).setVisibility(android.view.View.GONE);
            view.findViewById(R.id.profileAvatarEditBtn).setVisibility(android.view.View.GONE);
            view.findViewById(R.id.guestCtaBtn).setOnClickListener(v ->
                    startActivity(new Intent(activity, LoginActivity.class)));
            return;
        }

        avatarTV.setText("👤");
        loggedInInfoLL.setVisibility(android.view.View.VISIBLE);
        guestAccountCtaLL.setVisibility(android.view.View.GONE);
        statsRowLL.setVisibility(android.view.View.VISIBLE);
        streakCalendarCV.setVisibility(android.view.View.VISIBLE);
        view.findViewById(R.id.liptoBannerCV).setVisibility(android.view.View.VISIBLE);
        view.findViewById(R.id.liptoTierCV).setVisibility(android.view.View.VISIBLE);
        view.findViewById(R.id.profileAvatarEditBtn).setVisibility(android.view.View.VISIBLE);

        TextView nameTV = view.findViewById(R.id.profileNameTV);

        // Name
        String name = uConfig.getData("name");
        if (name != null && !name.isEmpty()) {
            nameTV.setText(name);
            int xp = prefs.getInt(Constant.TOTAL_XP, 0);
            TextView levelTV = view.findViewById(R.id.profileLevelTV);
            levelTV.setText(LevelHelper.getLevelTitle(xp));
        }

        // Friend Code — tap to copy; used for both battle challenges and Lipto gifting
        // Also loads/caches the profile photo (same fallback fetch covers both).
        bindFriendCode(view, prefs, activity);

        String cachedPhoto = prefs.getString("profile_photo", "");
        if (!cachedPhoto.isEmpty()) loadAvatarImage(view, cachedPhoto);

        view.findViewById(R.id.liptoBannerCV).setOnClickListener(v ->
                startActivity(new Intent(activity, com.abmn.englishhub.Activity.LiptoPassbookActivity.class)));

        bindStatNumbers(view, activity, prefs);
    }

    // XP / Streak / Rank / Lipto / tier / level, all from the cached prefs.
    // Split out of loadStats() so refreshFromServer() can repaint just these
    // numbers after the server answers, without re-binding avatar/friend code.
    private void bindStatNumbers(View view, Activity activity, SharedPreferences prefs) {
        int xp     = prefs.getInt(Constant.TOTAL_XP, 0);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        int rank   = prefs.getInt(Constant.USER_RANK, 0);
        int lipto  = prefs.getInt(Constant.LIPTO_BALANCE, 0);
        // Tier badge uses the highest balance ever reached, not the live
        // spendable balance, so it never demotes after spending/gifting Lipto.
        int liptoMax = Math.max(lipto, prefs.getInt(Constant.LIPTO_MAX_BALANCE, 0));

        ((TextView) view.findViewById(R.id.profileXpTV)).setText(String.valueOf(xp));
        ((TextView) view.findViewById(R.id.profileStreakTV)).setText(String.valueOf(streak));
        ((TextView) view.findViewById(R.id.profileRankTV)).setText(rank > 0 ? "#" + rank : "—");
        ((TextView) view.findViewById(R.id.profileLiptoTV)).setText(String.valueOf(lipto));

        bindLiptoTier(view, activity, liptoMax);

        // Level progress bar
        ((TextView) view.findViewById(R.id.profileLevelLabelTV)).setText(LevelHelper.getLevelLabel(xp));
        int toNext = LevelHelper.getXpToNextLevel(xp);
        ((TextView) view.findViewById(R.id.profileXpToNextTV)).setText(
                toNext > 0 ? toNext + " XP to next level" : "Max Level! 🏆");
        ((ProgressBar) view.findViewById(R.id.profileLevelProgressBar))
                .setProgress(LevelHelper.getLevelProgressPercent(xp));
        ((TextView) view.findViewById(R.id.profileLevelTV)).setText(LevelHelper.getLevelTitle(xp));
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromServer();
    }

    // The numbers above come from SharedPreferences written by other screens,
    // so rank/streak/Lipto here went stale (rank #27 on Profile while the
    // Leaderboard said #23). Ask the server once per time the tab is shown
    // and repaint - the cached values stay on screen until it answers.
    private void refreshFromServer() {
        Activity activity = getActivity();
        View root = getView();
        if (activity == null || root == null) return;
        String token = new UConfig(activity).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        // Shared with Home: both fetch the same endpoints and write the same
        // prefs, so one server round-trip per window is enough for both.
        if (!com.abmn.englishhub.Helper.StatsRefreshGate.tryAcquire()) return;

        ApiConfig.getRequest(activity, Constant.GAME_STREAK, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!Constant.SUCCESS.equals(json.optString(Constant.STATUS, ""))) return;
                JSONObject data = json.optJSONObject(Constant.DATA);
                if (data == null) return;
                prefs.edit()
                        .putInt(Constant.STREAK_DAYS, data.optInt("streak_days", 0))
                        .putInt(Constant.TOTAL_XP, data.optInt("total_xp", 0))
                        .putInt(Constant.USER_RANK, data.optInt("rank", 0))
                        .apply();
                repaintStats(activity, prefs);
            } catch (Exception ignored) {}
        }, error -> {});

        ApiConfig.getRequest(activity, Constant.GAME_LIPTO_BALANCE, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!Constant.SUCCESS.equals(json.optString(Constant.STATUS, ""))) return;
                int balance = json.optInt("balance", 0);
                int maxBalance = json.optInt("max_balance", balance);
                prefs.edit()
                        .putInt(Constant.LIPTO_BALANCE, balance)
                        .putInt(Constant.LIPTO_MAX_BALANCE,
                                Math.max(maxBalance, prefs.getInt(Constant.LIPTO_MAX_BALANCE, 0)))
                        .apply();
                repaintStats(activity, prefs);
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void repaintStats(Activity activity, SharedPreferences prefs) {
        activity.runOnUiThread(() -> {
            View root = getView();
            if (!isAdded() || root == null) return;
            bindStatNumbers(root, activity, prefs);
            buildWeekCalendar(root);
        });
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
            dayLabel.setTextSize(10);
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

    // ── Profile actions ───────────────────────────────────────────
    // The 8 app-level menu rows (offline, update, theme, rating, contact,
    // social, logout) moved to SettingsActivity; Profile keeps only the invite
    // card and the entry to Settings.

    private void wireProfileActions(View view) {
        final Activity activity = getActivity();
        if (activity == null) return;

        view.findViewById(R.id.menuSettingsCV).setOnClickListener(v ->
                startActivity(new Intent(activity, SettingsActivity.class)));

        View invite = view.findViewById(R.id.menuInviteCV);
        String token = new UConfig(activity).getData(Constant.TOKEN);
        boolean loggedIn = token != null && !token.isEmpty();
        // Invites only make sense when there is a friend code to share.
        invite.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        invite.setOnClickListener(v -> shareInvite(activity));
    }

    private void shareInvite(Activity activity) {
        String code = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("friend_code", "");
        StringBuilder msg = new StringBuilder()
                .append("\uD83D\uDCDA \u0986\u09ae\u09bf English Grammar Book \u0985\u09cd\u09af\u09be\u09aa \u09a6\u09bf\u09af\u09bc\u09c7 \u0987\u0982\u09b0\u09c7\u099c\u09bf \u09b6\u09bf\u0996\u099b\u09bf!\n\n")
                .append("\u098f\u0996\u09be\u09a8\u09c7 \u0986\u099b\u09c7 \u09eb\u09e6\u09e6\u09e6+ \u09b6\u09ac\u09cd\u09a6, \u09b8\u09ae\u09cd\u09aa\u09c2\u09b0\u09cd\u09a3 \u0997\u09cd\u09b0\u09be\u09ae\u09be\u09b0 \u0995\u09cb\u09b0\u09cd\u09b8, quiz \u0986\u09b0 \u09ae\u099c\u09be\u09b0 \u09b8\u09ac \u0997\u09b2\u09cd\u09aa\u0964\n\n")
                .append("\u09a4\u09c1\u09ae\u09bf\u0993 \u09b6\u09c7\u0996\u09be \u09b6\u09c1\u09b0\u09c1 \u0995\u09b0\u09cb \uD83D\uDC47\n")
                .append("https://play.google.com/store/apps/details?id=").append(activity.getPackageName());
        if (code != null && !code.isEmpty()) {
            // Sign-up has a friend-code field; the referrer earns Lipto when a
            // new account registers with it.
            msg.append("\n\n\uD83C\uDF81 \u09b8\u09be\u0987\u09a8-\u0986\u09aa\u09c7\u09b0 \u09b8\u09ae\u09af\u09bc \u0986\u09ae\u09be\u09b0 \u0995\u09cb\u09a1 \u09a6\u09bf\u0993: ").append(code);
        }
        msg.append("\n#EnglishGrammarBook #LearnEnglish");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
        startActivity(Intent.createChooser(shareIntent, "\u09b6\u09c7\u09af\u09bc\u09be\u09b0 \u0995\u09b0\u09cb"));
    }

    // Older installs that logged in before friend_code/profile_photo existed
    // won't have them cached locally yet (only saved on fresh Login/Register
    // responses) - fall back to Sanctum's bare GET /user route to pick both
    // up in one call and cache them.
    private void bindFriendCode(View view, SharedPreferences prefs, Activity activity) {
        if (activity == null) return;
        View codeLayout = view.findViewById(R.id.profileFriendCodeTV);
        TextView codeText = view.findViewById(R.id.profileFriendCodeText);

        boolean hasCode  = !prefs.getString("friend_code", "").isEmpty();
        boolean hasPhoto = !prefs.getString("profile_photo", "").isEmpty();

        if (hasCode) {
            showFriendCode(codeLayout, codeText, prefs.getString("friend_code", ""), activity);
        }
        if (hasCode && hasPhoto) return;

        String token = new UConfig(activity).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        String url = Constant.ROOT_DOMAIN + Constant.API2 + "user";
        ApiConfig.getRequest(activity, url, token, response -> {
            try {
                JSONObject user = new JSONObject(response);
                String code = user.optString("friend_code", "");

                // profile_photo is a computed accessor that ALWAYS returns a URL
                // (falls back to a default placeholder asset when the user has no
                // custom photo) - profile_photo_path is the raw column, only
                // non-null once a real photo has actually been uploaded. Must gate
                // on that, or every user with no avatar would start fetching and
                // showing the generic default placeholder image over the network
                // instead of the current zero-network emoji fallback.
                boolean hasRealPhoto = user.has("profile_photo_path") && !user.isNull("profile_photo_path");
                String photo = hasRealPhoto ? user.optString("profile_photo", "") : "";

                SharedPreferences.Editor editor = activity
                        .getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit();
                if (!code.isEmpty()) editor.putString("friend_code", code);
                if (!photo.isEmpty()) editor.putString("profile_photo", photo);
                editor.apply();

                activity.runOnUiThread(() -> {
                    if (!code.isEmpty()) showFriendCode(codeLayout, codeText, code, activity);
                    if (!photo.isEmpty()) loadAvatarImage(getView(), photo);
                });
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void showFriendCode(View layout, TextView text, String code, Activity activity) {
        text.setText("Code: " + code);
        layout.setVisibility(View.VISIBLE);
        layout.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("friend_code", code));
            Toast.makeText(activity, "✅ Friend Code কপি হয়েছে!", Toast.LENGTH_SHORT).show();
        });
    }
}
