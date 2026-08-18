package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.MistakeNotebook;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Round 4 (Writing) of the Quick Quiz level map — typed input, checked
// client-side with fuzzy matching against `expected_answer` from
// QuizQuestionBuilder::buildWritingQuestions(). Mirrors QuizActivity's
// hearts/timer/combo/sound mechanics so the whole 4-round flow feels like
// one consistent game, just with a text field instead of MCQ cards.
public class WritingActivity extends AppCompatActivity {

    private TextView questionNumberTV, timerTV, xpCountTV;
    private TextView directionHintTV, promptTV;
    private TextInputEditText answerET;
    private TextView feedbackTitleTV, feedbackDetailTV, feedbackIconTV;
    private LinearLayout feedbackStrip, progressDots, comboLL;
    private TextView comboCountTV;
    private MaterialButton actionBtn;
    private ProgressBar timerBar;
    private ProgressBar quizLoadingBar;
    private LinearLayout quizEmptyLL;
    private MaterialButton quizEmptyCloseBtn;
    private LinearLayout heartsRowLL;

    private final List<JSONObject> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int totalXpEarned = 0;
    private int correctCount = 0;
    private boolean answered = false;
    private CountDownTimer countDownTimer;
    private ToneGenerator toneGenerator;
    private android.media.SoundPool soundPool;
    private int soundCorrectId, soundWrongId;

    private int lessonId = 1;
    private long quizStartMs = 0;
    private int comboCount = 0;

    private static final int MAX_LIVES = 3;
    private int livesRemaining = MAX_LIVES;

    private static final int TIMER_SECONDS = 25; // typing needs more time than an MCQ tap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        lessonId = getIntent().getIntExtra("lesson_id", 1);

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }
        setupSoundPool();

        bindViews();
        setupClickListeners();
        setupHearts();
        fetchQuiz();
    }

    private void setupSoundPool() {
        try {
            android.media.AudioAttributes attrs = new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new android.media.SoundPool.Builder().setMaxStreams(2).setAudioAttributes(attrs).build();
            soundCorrectId = loadSoundIfExists("correct");
            soundWrongId   = loadSoundIfExists("wrong");
        } catch (Exception e) {
            soundPool = null;
        }
    }

    private int loadSoundIfExists(String rawName) {
        if (soundPool == null) return 0;
        int resId = getResources().getIdentifier(rawName, "raw", getPackageName());
        return resId != 0 ? soundPool.load(this, resId, 1) : 0;
    }

    private void playSound(int soundId) {
        if (!soundEnabled()) return;
        if (soundPool != null && soundId != 0) {
            try { soundPool.play(soundId, 1f, 1f, 1, 0, 1f); } catch (Exception ignored) {}
        }
    }

    private void playTone(int tone) {
        if (!soundEnabled() || toneGenerator == null) return;
        try { toneGenerator.startTone(tone, 150); } catch (Exception ignored) {}
    }

    // Shared mute toggle, set from LevelMapActivity's speaker icon.
    private boolean soundEnabled() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean(Constant.SOUND_ENABLED, true);
    }

    private void bindViews() {
        questionNumberTV = findViewById(R.id.questionNumberTV);
        timerTV          = findViewById(R.id.timerTV);
        timerBar         = findViewById(R.id.timerBar);
        xpCountTV        = findViewById(R.id.xpCountTV);
        comboLL          = findViewById(R.id.comboLL);
        comboCountTV     = findViewById(R.id.comboCountTV);
        progressDots     = findViewById(R.id.progressDots);
        directionHintTV  = findViewById(R.id.directionHintTV);
        promptTV         = findViewById(R.id.promptTV);
        answerET         = findViewById(R.id.answerET);
        feedbackStrip    = findViewById(R.id.feedbackStrip);
        feedbackTitleTV  = findViewById(R.id.feedbackTitleTV);
        feedbackDetailTV = findViewById(R.id.feedbackDetailTV);
        feedbackIconTV   = findViewById(R.id.feedbackIconTV);
        actionBtn        = findViewById(R.id.actionBtn);
        quizLoadingBar   = findViewById(R.id.quizLoadingBar);
        quizEmptyLL      = findViewById(R.id.quizEmptyLL);
        quizEmptyCloseBtn= findViewById(R.id.quizEmptyCloseBtn);
        heartsRowLL      = findViewById(R.id.heartsRowLL);
    }

    private void setupClickListeners() {
        findViewById(R.id.closeBtn).setOnClickListener(v -> finish());
        quizEmptyCloseBtn.setOnClickListener(v -> finish());
        actionBtn.setOnClickListener(v -> {
            if (!answered) {
                submitAnswer();
            } else {
                advance();
            }
        });
    }

    private void advance() {
        currentIndex++;
        if (livesRemaining <= 0) {
            goToResult();
        } else if (currentIndex < questions.size()) {
            showQuestion(currentIndex);
        } else {
            goToResult();
        }
    }

    // ── Hearts (same visual language as QuizActivity) ──────────────

    private void setupHearts() {
        heartsRowLL.setVisibility(View.VISIBLE);
        heartsRowLL.removeAllViews();
        int marginPx = (int) (3 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < MAX_LIVES; i++) {
            TextView heart = new TextView(this);
            heart.setText("❤️");
            heart.setTextSize(18);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(marginPx, 0, marginPx, 0);
            heart.setLayoutParams(lp);
            heartsRowLL.addView(heart);
        }
    }

    private void loseLife() {
        if (livesRemaining <= 0) return;
        livesRemaining--;
        View heart = heartsRowLL.getChildAt(livesRemaining);
        if (heart instanceof TextView) {
            TextView heartTV = (TextView) heart;
            heartTV.setText("🖤");
            android.animation.ValueAnimator anim = android.animation.ValueAnimator.ofFloat(1f, 0.4f);
            anim.setDuration(300);
            anim.addUpdateListener(a -> {
                float v = (float) a.getAnimatedValue();
                heartTV.setAlpha(v);
                heartTV.setScaleX(1f + (1f - v));
                heartTV.setScaleY(1f + (1f - v));
            });
            anim.start();
        }
    }

    // ── API ──────────────────────────────────────────────────────

    private void fetchQuiz() {
        quizLoadingBar.setVisibility(View.VISIBLE);
        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        String url = Constant.GAME_ROUND_QUIZ + lessonId + "/5";

        ApiConfig.getRequest(this, url, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                JSONObject payload = json.optJSONObject(Constant.DATA);
                JSONArray data = payload != null ? payload.optJSONArray("questions") : null;
                if (data == null) return;

                questions.clear();
                for (int i = 0; i < data.length(); i++) questions.add(data.getJSONObject(i));

                runOnUiThread(() -> {
                    quizLoadingBar.setVisibility(View.GONE);
                    if (questions.isEmpty()) {
                        quizEmptyLL.setVisibility(View.VISIBLE);
                        return;
                    }
                    buildProgressDots();
                    quizStartMs = System.currentTimeMillis();
                    showQuestion(0);
                });
            } catch (Exception e) {
                runOnUiThread(() -> quizLoadingBar.setVisibility(View.GONE));
            }
        }, error -> runOnUiThread(() -> {
            quizLoadingBar.setVisibility(View.GONE);
            android.widget.Toast.makeText(this,
                    error.getMessage() != null ? error.getMessage() : "নেটওয়ার্ক সমস্যা — আবার চেষ্টা করো",
                    android.widget.Toast.LENGTH_LONG).show();
            finish();
        }));
    }

    // ── Question display ────────────────────────────────────────

    private void showQuestion(int index) {
        if (index >= questions.size()) { goToResult(); return; }

        answered = false;
        feedbackStrip.setVisibility(View.GONE);
        answerET.setText("");
        answerET.setEnabled(true);
        actionBtn.setText("জমা দাও");

        try {
            JSONObject q = questions.get(index);
            questionNumberTV.setText("প্রশ্ন " + (index + 1) + " / " + questions.size());
            promptTV.setText(q.optString("prompt", ""));
            boolean bnToEn = "bn_to_en".equals(q.optString("direction", ""));
            directionHintTV.setText(bnToEn ? "এই বাংলা শব্দের ইংলিশ লেখো" : "এই ইংলিশ শব্দের বাংলা অর্থ লেখো");
            updateProgressDots(index);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startTimer();
    }

    private void submitAnswer() {
        if (currentIndex >= questions.size()) return;
        answered = true;
        if (countDownTimer != null) countDownTimer.cancel();
        answerET.setEnabled(false);

        JSONObject q = questions.get(currentIndex);
        String expected = q.optString("expected_answer", "");
        String typed = answerET.getText() != null ? answerET.getText().toString() : "";
        gradeAnswer(typed, expected);

        actionBtn.setText(isLastActionableQuestion() ? "ফলাফল দেখো 🏆" : "পরবর্তী →");
    }

    private void onTimerExpired() {
        if (answered) return;
        answered = true;
        answerET.setEnabled(false);
        JSONObject q = questions.get(currentIndex);
        String expected = q.optString("expected_answer", "");
        comboCount = 0;
        updateCombo();
        loseLife();
        recordMistake(q);
        playTone(ToneGenerator.TONE_PROP_NACK);
        playSound(soundWrongId);
        markProgressDot(currentIndex, false);
        showFeedback(false, false, "⏱", "সময় শেষ! ⏱", "সঠিক উত্তর: " + expected, "#FF3F6C", R.drawable.bg_red_pill);
        actionBtn.setText(isLastActionableQuestion() ? "ফলাফল দেখো 🏆" : "পরবর্তী →");
    }

    // ── Grading (normalize + Levenshtein fuzzy match) ────────────

    private void gradeAnswer(String typed, String expected) {
        String normTyped = normalize(typed);
        String normExpected = normalize(expected);

        if (!normTyped.isEmpty() && normTyped.equals(normExpected)) {
            correctCount++;
            totalXpEarned++;
            comboCount++;
            updateCombo();
            playTone(ToneGenerator.TONE_PROP_ACK);
            playSound(soundCorrectId);
            markProgressDot(currentIndex, true);
            showFeedback(true, false, "✓", "সঠিক! +1 XP ✓", "চমৎকার! তুমি ঠিকই লিখেছ।", "#00E8B8", R.drawable.bg_teal_pill);
            return;
        }

        int distance = levenshtein(normTyped, normExpected);
        boolean nearMiss = !normTyped.isEmpty() && distance > 0
                && distance <= Math.max(1, normExpected.length() / 5);

        comboCount = 0;
        updateCombo();
        loseLife();
        markProgressDot(currentIndex, false);
        recordMistake(questions.get(currentIndex));

        if (nearMiss) {
            playTone(ToneGenerator.TONE_PROP_NACK);
            playSound(soundWrongId);
            showFeedback(false, true, "😮", "একদম কাছাকাছি ছিলে!", "সঠিক উত্তর: " + expected, "#FB923C", R.drawable.bg_coral_pill);
        } else {
            playTone(ToneGenerator.TONE_PROP_NACK);
            playSound(soundWrongId);
            showFeedback(false, false, "✕", "ভুল হয়েছে", "সঠিক উত্তর: " + expected, "#FF3F6C", R.drawable.bg_red_pill);
        }
    }

    // Always stores as (english word, bangla meaning) regardless of which
    // direction this question asked, so the notebook reads consistently
    // whether the miss came from Writing, MCQ, Reading, or Listening.
    private void recordMistake(JSONObject q) {
        try {
            boolean bnToEn = "bn_to_en".equals(q.optString("direction", ""));
            String prompt = q.optString("prompt", "");
            String expected = q.optString("expected_answer", "");
            String word    = bnToEn ? expected : prompt;
            String meaning = bnToEn ? prompt : expected;
            MistakeNotebook.add(this, word, meaning);
        } catch (Exception ignored) {}
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    // Classic Levenshtein edit distance (insert/delete/substitute) — small
    // strings only (single words/short phrases), so the O(n*m) DP table is
    // cheap enough to run on the UI thread with no need for optimization.
    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    private void showFeedback(boolean correct, boolean nearMiss, String icon, String title, String detail, String color, int bgRes) {
        feedbackStrip.setBackgroundResource(bgRes);
        feedbackIconTV.setText(icon);
        int c = Color.parseColor(color);
        feedbackIconTV.setTextColor(c);
        feedbackTitleTV.setText(title);
        feedbackTitleTV.setTextColor(c);
        feedbackDetailTV.setText(detail);
        feedbackStrip.setVisibility(View.VISIBLE);
        xpCountTV.setText(totalXpEarned + " XP");
        if (!correct) {
            feedbackStrip.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        }
    }

    private boolean isLastActionableQuestion() {
        if (livesRemaining <= 0) return true;
        return currentIndex + 1 >= questions.size();
    }

    // ── Combo (identical behavior to QuizActivity#updateCombo) ───

    private void updateCombo() {
        if (comboCount >= 2) {
            comboCountTV.setText("🔥 x" + comboCount);
            comboLL.setVisibility(View.VISIBLE);
            comboLL.setScaleX(1f);
            comboLL.setScaleY(1f);
            comboLL.animate().scaleX(1.18f).scaleY(1.18f).setDuration(120)
                    .withEndAction(() -> comboLL.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                    .start();
        } else {
            comboLL.setVisibility(View.GONE);
        }
    }

    // ── Timer ────────────────────────────────────────────────────

    // Remaining time on the in-flight countdown, refreshed on every tick -
    // lets onResume() pick the timer back up after a backgrounding instead
    // of leaving it frozen (see onResume() below; mirrors QuizActivity).
    private long timerMillisRemaining = 0;

    private void startTimer() {
        timerMillisRemaining = TIMER_SECONDS * 1000L;
        if (timerBar != null) {
            timerBar.setMax(100);
            timerBar.setProgress(100);
        }
        timerTV.setText(String.valueOf(TIMER_SECONDS));
        timerTV.setTextColor(ContextCompat.getColor(this, R.color.gold));
        runTimer(timerMillisRemaining);
    }

    private void runTimer(long durationMillis) {
        if (countDownTimer != null) countDownTimer.cancel();
        // 100ms tick (not 1000ms) so timerBar drains smoothly instead of in
        // big visible jumps - this bar sat in the layout unwired before,
        // frozen at 100% for the whole round since nothing ever updated it.
        countDownTimer = new CountDownTimer(durationMillis, 100) {
            @Override
            public void onTick(long millisLeft) {
                timerMillisRemaining = millisLeft;
                int secsLeft = (int) (millisLeft / 1000) + 1;
                if (timerBar != null) {
                    timerBar.setProgress((int) ((millisLeft * 100) / (TIMER_SECONDS * 1000L)));
                }
                timerTV.setText(String.valueOf(secsLeft));
                if (secsLeft <= 5) timerTV.setTextColor(Color.parseColor("#FF3F6C"));
            }
            @Override
            public void onFinish() {
                timerMillisRemaining = 0;
                if (timerBar != null) timerBar.setProgress(0);
                timerTV.setText("0");
                onTimerExpired();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // See QuizActivity#onResume - same bug, same fix: onPause() only
        // cancels the countdown, so backgrounding mid-question used to
        // leave the timer frozen forever instead of resuming.
        if (!answered && !questions.isEmpty() && countDownTimer == null && timerMillisRemaining > 0) {
            runTimer(timerMillisRemaining);
        }
    }

    // ── Progress dots ────────────────────────────────────────────

    private void buildProgressDots() {
        progressDots.removeAllViews();
        int dotPx = (int) (8 * getResources().getDisplayMetrics().density);
        int marginPx = (int) (3 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < questions.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotPx, dotPx);
            lp.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            progressDots.addView(dot);
        }
        updateProgressDots(0);
    }

    private void updateProgressDots(int activeIndex) {
        for (int i = 0; i < progressDots.getChildCount(); i++) {
            if (i == activeIndex) progressDots.getChildAt(i).setBackgroundResource(R.drawable.dot_active);
        }
    }

    private void markProgressDot(int index, boolean correct) {
        View dot = progressDots.getChildAt(index);
        if (dot != null) dot.setBackgroundResource(correct ? R.drawable.dot_correct : R.drawable.dot_wrong);
    }

    // ── Result ───────────────────────────────────────────────────

    private int totalTimeSec() {
        return quizStartMs == 0 ? 0 : (int) ((System.currentTimeMillis() - quizStartMs) / 1000);
    }

    private void goToResult() {
        if (countDownTimer != null) countDownTimer.cancel();
        int attempted = Math.min(currentIndex, questions.size());
        if (attempted <= 0) attempted = questions.size();

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct", correctCount);
        intent.putExtra("total", attempted);
        intent.putExtra("xp", totalXpEarned);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("time_sec", totalTimeSec());
        intent.putExtra("round", 5);
        intent.putExtra("hearts_lost", MAX_LIVES - livesRemaining);
        intent.putExtra("max_lives", MAX_LIVES);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (toneGenerator != null) { toneGenerator.release(); toneGenerator = null; }
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }
}
