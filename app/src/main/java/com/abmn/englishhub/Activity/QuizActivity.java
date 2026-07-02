package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    // Views
    private TextView questionNumberTV, questionTV, timerTV, xpCountTV;
    private TextView optionATV, optionBTV, optionCTV, optionDTV;
    private TextView labelA, labelB, labelC, labelD;
    private TextView feedbackTitleTV, feedbackDetailTV, feedbackIconTV, xpEarnedTV;
    private CardView optionA, optionB, optionC, optionD;
    private CardView closeBtn;
    private ProgressBar timerBar;
    private LinearLayout feedbackStrip, progressDots, bottomActionRow;
    private com.google.android.material.button.MaterialButton nextBtn;
    private ProgressBar quizLoadingBar;

    // Quiz state
    private List<JSONObject> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int totalXpEarned = 0;
    private int correctCount = 0;
    private boolean answered = false;
    private CountDownTimer countDownTimer;
    private int lessonId = 1;

    // Option card references in array for easy iteration
    private CardView[] optionCards;
    private TextView[] optionLabels;
    private TextView[] optionTexts;

    private static final int TIMER_SECONDS = 15;
    private static final int TOTAL_QUESTIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        lessonId = getIntent().getIntExtra("lesson_id", 1);

        bindViews();
        setupClickListeners();
        fetchQuiz();
    }

    private void bindViews() {
        questionNumberTV = findViewById(R.id.questionNumberTV);
        questionTV       = findViewById(R.id.questionTV);
        timerTV          = findViewById(R.id.timerTV);
        xpCountTV        = findViewById(R.id.xpCountTV);
        timerBar         = findViewById(R.id.timerBar);
        progressDots     = findViewById(R.id.progressDots);
        feedbackStrip    = findViewById(R.id.feedbackStrip);
        feedbackTitleTV  = findViewById(R.id.feedbackTitleTV);
        feedbackDetailTV = findViewById(R.id.feedbackDetailTV);
        feedbackIconTV   = findViewById(R.id.feedbackIconTV);
        xpEarnedTV       = findViewById(R.id.xpEarnedTV);
        nextBtn          = findViewById(R.id.nextBtn);
        closeBtn         = findViewById(R.id.closeBtn);
        quizLoadingBar   = findViewById(R.id.quizLoadingBar);

        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);

        labelA = findViewById(R.id.labelA);
        labelB = findViewById(R.id.labelB);
        labelC = findViewById(R.id.labelC);
        labelD = findViewById(R.id.labelD);

        optionATV = findViewById(R.id.optionATV);
        optionBTV = findViewById(R.id.optionBTV);
        optionCTV = findViewById(R.id.optionCTV);
        optionDTV = findViewById(R.id.optionDTV);

        optionCards  = new CardView[]{optionA, optionB, optionC, optionD};
        optionLabels = new TextView[]{labelA, labelB, labelC, labelD};
        optionTexts  = new TextView[]{optionATV, optionBTV, optionCTV, optionDTV};
    }

    private void setupClickListeners() {
        closeBtn.setOnClickListener(v -> finish());

        for (int i = 0; i < optionCards.length; i++) {
            final int idx = i;
            optionCards[i].setOnClickListener(v -> {
                if (!answered) onOptionSelected(idx);
            });
        }

        nextBtn.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < questions.size()) {
                showQuestion(currentIndex);
            } else {
                goToResult();
            }
        });
    }

    // ── API ──────────────────────────────────────────────────────

    private void fetchQuiz() {
        quizLoadingBar.setVisibility(View.VISIBLE);
        String url = Constant.GAME_QUIZ + lessonId + "?count=" + TOTAL_QUESTIONS;
        ApiConfig.getRequest(this, url, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) {
                    JSONArray data = json.optJSONArray(Constant.DATA);
                    if (data == null) return;
                    questions.clear();
                    for (int i = 0; i < data.length(); i++) {
                        questions.add(data.getJSONObject(i));
                    }
                    runOnUiThread(() -> {
                        quizLoadingBar.setVisibility(View.GONE);
                        buildProgressDots();
                        showQuestion(0);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> quizLoadingBar.setVisibility(View.GONE));
            }
        }, error -> runOnUiThread(() -> quizLoadingBar.setVisibility(View.GONE)));
    }

    // ── Question display ─────────────────────────────────────────

    private void showQuestion(int index) {
        if (index >= questions.size()) { goToResult(); return; }

        answered = false;
        resetOptionStyles();
        feedbackStrip.setVisibility(View.GONE);
        nextBtn.setVisibility(View.GONE);

        try {
            JSONObject q = questions.get(index);
            questionNumberTV.setText("প্রশ্ন " + (index + 1) + " / " + questions.size());
            questionTV.setText(q.getString("question"));

            JSONArray opts = q.getJSONArray("options");
            for (int i = 0; i < opts.length() && i < 4; i++) {
                optionTexts[i].setText(opts.getString(i));
            }

            updateProgressDots(index);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startTimer();
    }

    private void onOptionSelected(int selectedIdx) {
        answered = true;
        if (countDownTimer != null) countDownTimer.cancel();

        try {
            JSONObject q = questions.get(currentIndex);
            int correctIdx = q.getInt("correct_index");
            boolean isCorrect = (selectedIdx == correctIdx);

            if (isCorrect) {
                correctCount++;
                totalXpEarned++;
                showCorrectFeedback(selectedIdx, q.optString("explanation", ""));
            } else {
                showWrongFeedback(selectedIdx, correctIdx, q.optString("explanation", ""));
            }

            xpCountTV.setText(totalXpEarned + " XP");
            markProgressDot(currentIndex, isCorrect);

        } catch (Exception e) {
            e.printStackTrace();
        }

        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setText(currentIndex + 1 >= questions.size() ? "See Results 🏆" : "Next →");
    }

    private void onTimerExpired() {
        if (answered) return;
        answered = true;
        try {
            int correctIdx = questions.get(currentIndex).getInt("correct_index");
            showTimedOutFeedback(correctIdx);
            markProgressDot(currentIndex, false);
        } catch (Exception ignored) {}
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setText(currentIndex + 1 >= questions.size() ? "See Results 🏆" : "Next →");
    }

    // ── Feedback states ──────────────────────────────────────────

    private void showCorrectFeedback(int idx, String explanation) {
        // Green teal on correct card
        optionCards[idx].setCardBackgroundColor(Color.parseColor("#1400E8B8")); // teal_dim
        optionLabels[idx].setTextColor(Color.parseColor("#00E8B8"));
        optionLabels[idx].setBackgroundResource(R.drawable.option_label_correct);
        optionTexts[idx].setTextColor(Color.parseColor("#00E8B8"));

        feedbackStrip.setBackgroundResource(R.drawable.bg_teal_pill);
        feedbackIconTV.setText("✓");
        feedbackIconTV.setTextColor(Color.parseColor("#00E8B8"));
        feedbackTitleTV.setText("সঠিক! +1 XP ✓");
        feedbackTitleTV.setTextColor(Color.parseColor("#00E8B8"));
        feedbackDetailTV.setText(explanation.isEmpty() ? "চমৎকার! তুমি ঠিকই ধরেছ।" : explanation);
        xpEarnedTV.setVisibility(View.GONE);
        feedbackStrip.setVisibility(View.VISIBLE);
    }

    private void showWrongFeedback(int selectedIdx, int correctIdx, String explanation) {
        // Red on wrong card
        optionCards[selectedIdx].setCardBackgroundColor(Color.parseColor("#14FF3F6C")); // red_dim
        optionLabels[selectedIdx].setTextColor(Color.parseColor("#FF3F6C"));
        optionLabels[selectedIdx].setBackgroundResource(R.drawable.option_label_wrong);
        optionTexts[selectedIdx].setTextColor(Color.parseColor("#FF3F6C"));
        optionCards[selectedIdx].startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));

        // Teal on correct card
        optionCards[correctIdx].setCardBackgroundColor(Color.parseColor("#1400E8B8"));
        optionLabels[correctIdx].setTextColor(Color.parseColor("#00E8B8"));
        optionLabels[correctIdx].setBackgroundResource(R.drawable.option_label_correct);
        optionTexts[correctIdx].setTextColor(Color.parseColor("#00E8B8"));

        feedbackStrip.setBackgroundResource(R.drawable.bg_red_pill);
        feedbackIconTV.setText("✕");
        feedbackIconTV.setTextColor(Color.parseColor("#FF3F6C"));
        feedbackTitleTV.setText("ভুল হয়েছে");
        feedbackTitleTV.setTextColor(Color.parseColor("#FF3F6C"));
        feedbackDetailTV.setText(explanation.isEmpty() ? "হতাশ হয়ো না, চালিয়ে যাও!" : explanation);
        xpEarnedTV.setVisibility(View.GONE);
        feedbackStrip.setVisibility(View.VISIBLE);
    }

    private void showTimedOutFeedback(int correctIdx) {
        optionCards[correctIdx].setCardBackgroundColor(Color.parseColor("#1400E8B8"));
        optionLabels[correctIdx].setTextColor(Color.parseColor("#00E8B8"));
        optionLabels[correctIdx].setBackgroundResource(R.drawable.option_label_correct);
        optionTexts[correctIdx].setTextColor(Color.parseColor("#00E8B8"));

        feedbackStrip.setBackgroundResource(R.drawable.bg_red_pill);
        feedbackIconTV.setText("⏱");
        feedbackIconTV.setTextColor(Color.parseColor("#FF3F6C"));
        feedbackTitleTV.setText("সময় শেষ! ⏱");
        feedbackTitleTV.setTextColor(Color.parseColor("#FF3F6C"));
        feedbackDetailTV.setText("সঠিক উত্তরটি দেখো");
        xpEarnedTV.setVisibility(View.GONE);
        feedbackStrip.setVisibility(View.VISIBLE);
    }

    // ── Timer ────────────────────────────────────────────────────

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerBar.setMax(100);
        timerBar.setProgress(100);
        timerTV.setText(String.valueOf(TIMER_SECONDS));
        timerTV.setTextColor(ContextCompat.getColor(this, R.color.gold));

        countDownTimer = new CountDownTimer(TIMER_SECONDS * 1000L, 100) {
            @Override
            public void onTick(long millisLeft) {
                int secsLeft = (int) (millisLeft / 1000) + 1;
                int progress = (int) ((millisLeft * 100) / (TIMER_SECONDS * 1000L));
                timerBar.setProgress(progress);
                timerTV.setText(String.valueOf(secsLeft));
                // Turn timer red when ≤ 5 seconds
                if (secsLeft <= 5) {
                    timerTV.setTextColor(Color.parseColor("#FF3F6C"));
                }
            }
            @Override
            public void onFinish() {
                timerBar.setProgress(0);
                timerTV.setText("0");
                onTimerExpired();
            }
        }.start();
    }

    // ── Progress dots ─────────────────────────────────────────────

    private void buildProgressDots() {
        progressDots.removeAllViews();
        int size = Math.min(questions.size(), TOTAL_QUESTIONS);
        int dotPx = (int) (8 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (3 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < size; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotPx, dotPx);
            lp.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dot.setTag("dot_" + i);
            progressDots.addView(dot);
        }
    }

    private void updateProgressDots(int activeIndex) {
        for (int i = 0; i < progressDots.getChildCount(); i++) {
            View dot = progressDots.getChildAt(i);
            if (i == activeIndex) {
                dot.setBackgroundResource(R.drawable.dot_active);
            }
        }
    }

    private void markProgressDot(int index, boolean correct) {
        View dot = progressDots.getChildAt(index);
        if (dot != null) {
            dot.setBackgroundResource(correct ? R.drawable.dot_correct : R.drawable.dot_wrong);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void resetOptionStyles() {
        int cardBg    = ContextCompat.getColor(this, R.color.bg_card);
        int labelColor = ContextCompat.getColor(this, R.color.indigo);
        int textColor  = ContextCompat.getColor(this, R.color.text_primary);
        for (int i = 0; i < optionCards.length; i++) {
            optionCards[i].setCardBackgroundColor(cardBg);
            optionLabels[i].setTextColor(labelColor);
            optionLabels[i].setBackgroundResource(R.drawable.option_label_bg);
            optionTexts[i].setTextColor(textColor);
        }
    }

    private void goToResult() {
        if (countDownTimer != null) countDownTimer.cancel();
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct", correctCount);
        intent.putExtra("total", questions.size());
        intent.putExtra("xp", totalXpEarned);
        intent.putExtra("lesson_id", lessonId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
