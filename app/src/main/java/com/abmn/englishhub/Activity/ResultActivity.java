package com.abmn.englishhub.Activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONObject;

public class ResultActivity extends AppCompatActivity {

    private TextView resultEmojiTV, resultGradeTV, resultSubtitleTV;
    private TextView correctCountTV, totalCountTV, scorePercentTV;
    private TextView xpEarnedBigTV, streakResultTV, accuracyTV;
    private TextView newRankTV;
    private CardView rankCard;
    private ProgressBar scoreBar;

    private int correct, total, xpEarned, lessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        correct  = getIntent().getIntExtra("correct", 0);
        total    = getIntent().getIntExtra("total", 10);
        xpEarned = getIntent().getIntExtra("xp", 0);
        lessonId = getIntent().getIntExtra("lesson_id", 1);

        bindViews();
        populateStatic();
        animateScore();
        submitXpAndStreak();
        setupButtons();
    }

    private void bindViews() {
        resultEmojiTV    = findViewById(R.id.resultEmojiTV);
        resultGradeTV    = findViewById(R.id.resultGradeTV);
        resultSubtitleTV = findViewById(R.id.resultSubtitleTV);
        correctCountTV   = findViewById(R.id.correctCountTV);
        totalCountTV     = findViewById(R.id.totalCountTV);
        scorePercentTV   = findViewById(R.id.scorePercentTV);
        xpEarnedBigTV    = findViewById(R.id.xpEarnedBigTV);
        streakResultTV   = findViewById(R.id.streakResultTV);
        accuracyTV       = findViewById(R.id.accuracyTV);
        newRankTV        = findViewById(R.id.newRankTV);
        rankCard         = findViewById(R.id.rankCard);
        scoreBar         = findViewById(R.id.scoreBar);
    }

    private void populateStatic() {
        int pct = total > 0 ? (correct * 100) / total : 0;

        totalCountTV.setText(String.valueOf(total));
        correctCountTV.setText(String.valueOf(correct));
        scorePercentTV.setText(pct + "%");
        accuracyTV.setText(pct + "%");
        xpEarnedBigTV.setText("+" + xpEarned);

        // Mark today as played — suppresses the 8pm streak reminder
        String today = new java.text.SimpleDateFormat("yyyy-M-d", java.util.Locale.US)
                .format(new java.util.Date());
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit().putString(Constant.LAST_PLAYED_DATE, today).apply();

        // Grade + emoji based on score
        if (pct >= 90) {
            resultEmojiTV.setText("🏆");
            resultGradeTV.setText("অসাধারণ!");
            resultSubtitleTV.setText("তুমি আজ দারুণ করেছ! একদম perfect! 🔥");
        } else if (pct >= 70) {
            resultEmojiTV.setText("⭐");
            resultGradeTV.setText("বাহ, ভালো হয়েছে!");
            resultSubtitleTV.setText("প্রায় নিখুঁত! চালিয়ে যাও, তুমি পারবে!");
        } else if (pct >= 50) {
            resultEmojiTV.setText("👍");
            resultGradeTV.setText("চেষ্টা ভালো ছিল!");
            resultSubtitleTV.setText("আরও অনুশীলন করো — তুমি আরও ভালো করবে!");
        } else {
            resultEmojiTV.setText("💪");
            resultGradeTV.setText("থামো না!");
            resultSubtitleTV.setText("প্রতিটি চেষ্টায় তুমি শিখছ। হাল ছাড়ো না!");
        }

        // Streak from prefs (updated after API call)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        streakResultTV.setText("🔥 " + streak);
    }

    // ── Animations ───────────────────────────────────────────────

    private void animateScore() {
        int pct = total > 0 ? (correct * 100) / total : 0;

        // Score bar fill
        ValueAnimator barAnim = ValueAnimator.ofInt(0, pct);
        barAnim.setDuration(1000);
        barAnim.setStartDelay(300);
        barAnim.setInterpolator(new DecelerateInterpolator());
        barAnim.addUpdateListener(a -> scoreBar.setProgress((int) a.getAnimatedValue()));
        barAnim.start();

        // XP counter count-up
        ValueAnimator xpAnim = ValueAnimator.ofInt(0, xpEarned);
        xpAnim.setDuration(800);
        xpAnim.setStartDelay(500);
        xpAnim.setInterpolator(new DecelerateInterpolator());
        xpAnim.addUpdateListener(a -> xpEarnedBigTV.setText("+" + a.getAnimatedValue()));
        xpAnim.start();
    }

    // ── API calls ────────────────────────────────────────────────

    private void submitXpAndStreak() {
        submitXp();
        updateStreak();
    }

    private void submitXp() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        JSONObject body = new JSONObject();
        try {
            int pct = total > 0 ? (correct * 100) / total : 0;
            body.put("score", correct);
            body.put("total", total);
            body.put("lesson_id", lessonId);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GAME_XP, body, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    JSONObject data = json.optJSONObject(Constant.DATA);
                    if (data == null) return;
                    int newRank = data.optInt("rank", 0);
                    if (newRank > 0) {
                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit().putInt(Constant.USER_RANK, newRank).apply();
                        runOnUiThread(() -> {
                            newRankTV.setText("#" + newRank);
                            rankCard.setVisibility(View.VISIBLE);
                        });
                    }
                }
            } catch (Exception ignored) {}
        }, error -> {
            // XP submit failed silently — will retry next quiz
        });
    }

    private void updateStreak() {
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        ApiConfig.postRequest(this, Constant.GAME_STREAK_UPDATE, new JSONObject(), response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    JSONObject data = json.optJSONObject(Constant.DATA);
                    if (data == null) return;
                    int streak = data.optInt("streak_days", 0);
                    // Save to prefs and update UI
                    getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            .edit().putInt(Constant.STREAK_DAYS, streak).apply();
                    runOnUiThread(() -> streakResultTV.setText("🔥 " + streak));
                }
            } catch (Exception ignored) {}
        }, error -> {
            // Streak update failed silently — will retry next quiz
        });
    }

    // ── Buttons ──────────────────────────────────────────────────

    private void setupButtons() {
        findViewById(R.id.playAgainBtn).setOnClickListener(v -> {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("lesson_id", lessonId);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        finish();
    }
}
