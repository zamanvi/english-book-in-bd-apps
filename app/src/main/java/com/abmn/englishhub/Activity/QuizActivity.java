package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.OfflineCache;
import com.abmn.englishhub.R;
import com.abmn.texttospeech.TextToSpeechHelper;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    // Views
    private TextView questionNumberTV, questionTV, timerTV, xpCountTV, questionCategoryTV, questionHintTV;
    private LinearLayout listeningPromptLL;
    private CardView replayBtn, modeToggleBtn;
    private ImageView modeToggleIV;
    private TextToSpeechHelper ttsHelper;
    private boolean isListeningMode = false;
    private TextView optionATV, optionBTV, optionCTV, optionDTV;
    private TextView labelA, labelB, labelC, labelD;
    private TextView feedbackTitleTV, feedbackDetailTV, feedbackIconTV, xpEarnedTV;
    private CardView optionA, optionB, optionC, optionD;
    private CardView closeBtn;
    private ProgressBar timerBar;
    private LinearLayout feedbackStrip, progressDots, bottomActionRow;
    private com.google.android.material.button.MaterialButton nextBtn;
    private ProgressBar quizLoadingBar;
    private LinearLayout quizEmptyLL;
    private com.google.android.material.button.MaterialButton quizEmptyCloseBtn;

    // Quiz state
    private List<JSONObject> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int totalXpEarned = 0;
    private int correctCount = 0;
    private boolean answered = false;
    private CountDownTimer countDownTimer;
    private ToneGenerator toneGenerator;
    private int lessonId = 1;
    private int battleId = 0;  // 0 = normal quiz, >0 = part of a battle
    private long quizStartMs = 0;

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
        battleId = getIntent().getIntExtra("battle_id", 0);

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }

        UConfig uConfig = new UConfig(this);
        ttsHelper = new TextToSpeechHelper(this, uConfig.getData(Constant.VOICE_SPEED));

        bindViews();
        setupClickListeners();
        fetchQuiz();
    }

    private void bindViews() {
        questionNumberTV   = findViewById(R.id.questionNumberTV);
        questionTV         = findViewById(R.id.questionTV);
        questionCategoryTV = findViewById(R.id.questionCategoryTV);
        questionHintTV     = findViewById(R.id.questionHintTV);
        listeningPromptLL  = findViewById(R.id.listeningPromptLL);
        replayBtn          = findViewById(R.id.replayBtn);
        modeToggleBtn      = findViewById(R.id.modeToggleBtn);
        modeToggleIV       = findViewById(R.id.modeToggleIV);
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
        quizEmptyLL      = findViewById(R.id.quizEmptyLL);
        quizEmptyCloseBtn= findViewById(R.id.quizEmptyCloseBtn);

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
        quizEmptyCloseBtn.setOnClickListener(v -> finish());
        modeToggleBtn.setOnClickListener(v -> toggleListeningMode());
        replayBtn.setOnClickListener(v -> speakCurrentWord());

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
        UConfig uConfig = new UConfig(this);
        if (!uConfig.isConnected()) {
            buildOfflineQuiz();
            return;
        }
        String url = Constant.GAME_QUIZ + lessonId + "?count=" + TOTAL_QUESTIONS;
        // This is a public (no-login-required) endpoint, but the server now
        // needs to know WHO's asking to tell a locked Premium lesson apart
        // from one this exact user already unlocked - send the token when
        // we have one; guests simply get treated as not-unlocked.
        String token = uConfig.getData(Constant.TOKEN);
        ApiConfig.getRequest(this, url, token, response -> {
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
                        if (questions.isEmpty()) {
                            quizEmptyLL.setVisibility(View.VISIBLE);
                            return;
                        }
                        buildProgressDots();
                        quizStartMs = System.currentTimeMillis();
                        showQuestion(0);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> quizLoadingBar.setVisibility(View.GONE));
            }
        }, error -> runOnUiThread(() -> {
            String message = error.getMessage();
            if (message == null) {
                // No parseable server error body - a genuine connectivity
                // failure (mid-air disconnect after the isConnected() check
                // above passed), not a real business-rule rejection. Fall
                // back to a locally-generated quiz instead of a dead end.
                buildOfflineQuiz();
                return;
            }
            // A real message was parsed from the server's error response
            // (e.g. a locked Premium lesson) - surface it, don't silently
            // bypass it with an offline quiz built from cached word data.
            quizLoadingBar.setVisibility(View.GONE);
            android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show();
            finish();
        }));
    }

    // Builds a quiz the same way the server does (GameController::quiz) -
    // random word sample, random wrong-answer distractors, shuffled options -
    // but from the word list already cached on-device by WordActivity, so a
    // lesson viewed once online can still be quizzed with no connection.
    private void buildOfflineQuiz() {
        List<JSONObject> allWords = new ArrayList<>();
        int page = 1;
        while (true) {
            String cached = OfflineCache.load(this, "vocab_words_" + lessonId + "_p" + page);
            if (cached == null) break;
            try {
                JSONObject root = new JSONObject(cached);
                JSONArray dataArray = root.getJSONObject(Constant.WORDS).getJSONArray(Constant.DATA);
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject w = dataArray.getJSONObject(i);
                    if (w.optBoolean("status", true)) {
                        allWords.add(w);
                    }
                }
            } catch (Exception e) {
                break;
            }
            page++;
        }

        runOnUiThread(() -> {
            quizLoadingBar.setVisibility(View.GONE);
            if (allWords.isEmpty()) {
                quizEmptyLL.setVisibility(View.VISIBLE);
                return;
            }

            List<String> allMeanings = new ArrayList<>();
            for (JSONObject w : allWords) {
                allMeanings.add(w.optString("meaning", ""));
            }

            List<JSONObject> shuffled = new ArrayList<>(allWords);
            Collections.shuffle(shuffled);
            int count = Math.min(TOTAL_QUESTIONS, shuffled.size());

            questions.clear();
            for (int qi = 0; qi < count; qi++) {
                JSONObject w = shuffled.get(qi);
                String word = w.optString("word", "");
                String meaning = w.optString("meaning", "");
                String synonyms = w.optString("synonyms", "");

                List<String> wrongPool = new ArrayList<>(allMeanings);
                wrongPool.remove(meaning);
                Collections.shuffle(wrongPool);

                List<String> options = new ArrayList<>();
                options.add(meaning);
                for (int i = 0; i < 3; i++) {
                    options.add(i < wrongPool.size() ? wrongPool.get(i) : "N/A");
                }
                Collections.shuffle(options);
                int correctIndex = options.indexOf(meaning);

                try {
                    JSONObject q = new JSONObject();
                    q.put("question", word);
                    q.put("correct_index", correctIndex);
                    JSONArray optArr = new JSONArray();
                    for (String o : options) optArr.put(o);
                    q.put("options", optArr);
                    q.put("explanation", synonyms == null ? "" : synonyms);
                    questions.add(q);
                } catch (Exception ignored) {}
            }

            if (questions.isEmpty()) {
                quizEmptyLL.setVisibility(View.VISIBLE);
                return;
            }
            buildProgressDots();
            quizStartMs = System.currentTimeMillis();
            showQuestion(0);
        });
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

        applyModeVisibility();
        startTimer();
    }

    // ── Listening mode ───────────────────────────────────────────

    private void toggleListeningMode() {
        isListeningMode = !isListeningMode;
        modeToggleIV.setColorFilter(ContextCompat.getColor(this,
                isListeningMode ? R.color.indigo : R.color.text_secondary));
        applyModeVisibility();
        if (isListeningMode) speakCurrentWord();
    }

    private void applyModeVisibility() {
        questionTV.setVisibility(isListeningMode ? View.GONE : View.VISIBLE);
        listeningPromptLL.setVisibility(isListeningMode ? View.VISIBLE : View.GONE);
        questionCategoryTV.setText(isListeningMode ? "LISTENING" : "VOCABULARY");
        questionHintTV.setText(isListeningMode ? "অডিও শুনে সঠিক অর্থ বেছে নাও" : "সঠিক উত্তরটি বেছে নাও");
    }

    private void speakCurrentWord() {
        if (currentIndex >= questions.size() || ttsHelper == null) return;
        try {
            String word = questions.get(currentIndex).getString("question");
            ttsHelper.speak(word);
        } catch (Exception ignored) {}
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
        playTone(ToneGenerator.TONE_PROP_ACK);
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
        playTone(ToneGenerator.TONE_PROP_NACK);
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
        playTone(ToneGenerator.TONE_PROP_NACK);
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

    // Per-option colors for Option C design: purple / blue / amber / pink
    private static final int[] OPTION_CARD_BG   = {0xFF1E1060, 0xFF0A1E30, 0xFF1E1400, 0xFF1E0A18};
    private static final int[] OPTION_LABEL_RES = {R.drawable.option_label_a, R.drawable.option_label_b, R.drawable.option_label_c, R.drawable.option_label_d};
    private static final int[] OPTION_LABEL_TXT = {0xFF1A1060, 0xFF0A1830, 0xFF1E1000, 0xFF1E0A18};
    private static final int[] OPTION_TEXT_CLR  = {0xFFC4BEFF, 0xFF7DD3FC, 0xFFFDE68A, 0xFFFBCFE8};

    private void resetOptionStyles() {
        for (int i = 0; i < optionCards.length; i++) {
            optionCards[i].setCardBackgroundColor(OPTION_CARD_BG[i]);
            optionLabels[i].setBackgroundResource(OPTION_LABEL_RES[i]);
            optionLabels[i].setTextColor(OPTION_LABEL_TXT[i]);
            optionTexts[i].setTextColor(OPTION_TEXT_CLR[i]);
        }
    }

    private void playTone(int tone) {
        if (toneGenerator == null) return;
        try {
            toneGenerator.startTone(tone, 150);
        } catch (Exception ignored) {}
    }

    private int totalTimeSec() {
        if (quizStartMs == 0) return 0;
        return (int) ((System.currentTimeMillis() - quizStartMs) / 1000);
    }

    private void goToResult() {
        if (countDownTimer != null) countDownTimer.cancel();
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct", correctCount);
        intent.putExtra("total", questions.size());
        intent.putExtra("xp", totalXpEarned);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("battle_id", battleId);
        intent.putExtra("time_sec", totalTimeSec());
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
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
