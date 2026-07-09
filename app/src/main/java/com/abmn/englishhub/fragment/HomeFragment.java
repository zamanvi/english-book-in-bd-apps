package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.BattleActivity;
import com.abmn.englishhub.Activity.ChapterActivity;
import com.abmn.englishhub.Activity.GroupActivity;
import com.abmn.englishhub.Activity.LeaderboardActivity;
import com.abmn.englishhub.Activity.QuizActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.LevelHelper;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private Activity activity;

    // Word of day
    private TextView wordOfDayTV, wordMeaningTV;
    private CardView ttsBtn;
    private String currentWord = "";

    // Stats
    private TextView totalXpTV, streakCountTV, rankTV, streakTV, greetingNameTV, liptoBalanceTV;
    private TextView levelLabelTV, levelTitleTV, xpToNextTV;
    private ProgressBar levelProgressBar;

    // Continue learning
    private TextView lessonCountTV, continueLessonTV;

    // Quick actions
    private CardView quickQuizBtn, leaderboardBtn, groupBtn, battleBtn;

    // TTS engine
    private TextToSpeech tts;
    private boolean ttsReady = false;

    // Last lesson id for Quick Quiz
    private int lastLessonId = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        activity = getActivity();
        bindViews(view);
        initTts();
        setupClickListeners();
        loadUserStats();
        loadDailyWord();
        loadGrammarProgress();
        return view;
    }

    // ── View binding ─────────────────────────────────────────────

    private void bindViews(View view) {
        // Time-aware greeting
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String timeGreeting = hour < 12 ? "Good morning ☀️"
                : hour < 17 ? "Good afternoon 👋"
                : "Good evening 🌙";
        ((android.widget.TextView) view.findViewById(R.id.greetingTimeTV)).setText(timeGreeting);

        greetingNameTV  = view.findViewById(R.id.greetingNameTV);
        streakTV        = view.findViewById(R.id.streakTV);
        totalXpTV       = view.findViewById(R.id.totalXpTV);
        streakCountTV   = view.findViewById(R.id.streakCountTV);
        rankTV          = view.findViewById(R.id.rankTV);
        liptoBalanceTV  = view.findViewById(R.id.liptoBalanceTV);
        wordOfDayTV     = view.findViewById(R.id.wordOfDayTV);
        wordMeaningTV   = view.findViewById(R.id.wordMeaningTV);
        ttsBtn          = view.findViewById(R.id.ttsBtn);
        lessonCountTV   = view.findViewById(R.id.lessonCountTV);
        continueLessonTV  = view.findViewById(R.id.continueLessonTV);
        quickQuizBtn    = view.findViewById(R.id.quickQuizBtn);
        leaderboardBtn  = view.findViewById(R.id.leaderboardBtn);
        groupBtn        = view.findViewById(R.id.groupBtn);
        battleBtn       = view.findViewById(R.id.battleBtn);
        levelLabelTV    = view.findViewById(R.id.levelLabelTV);
        levelTitleTV    = view.findViewById(R.id.levelTitleTV);
        xpToNextTV      = view.findViewById(R.id.xpToNextTV);
        levelProgressBar = view.findViewById(R.id.levelProgressBar);

        // Category cards
        view.findViewById(R.id.vocabularyLL).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyActivity.class)));
        view.findViewById(R.id.grammarLL).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar")));
        view.findViewById(R.id.dailyVocabularyLL).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "daily_vocabulary")));
        view.findViewById(R.id.writingAndReadingLL).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "writing_reading")));
        view.findViewById(R.id.continueLearningCard).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar")));
    }

    // ── Click listeners ──────────────────────────────────────────

    private void setupClickListeners() {
        quickQuizBtn.setOnClickListener(v ->
                startActivity(new Intent(activity, QuizActivity.class)
                        .putExtra("lesson_id", lastLessonId)));

        leaderboardBtn.setOnClickListener(v ->
                startActivity(new Intent(activity, LeaderboardActivity.class)));

        groupBtn.setOnClickListener(v ->
                startActivity(new Intent(activity, GroupActivity.class)));

        battleBtn.setOnClickListener(v ->
                startActivity(new Intent(activity, BattleActivity.class)));

        ttsBtn.setOnClickListener(v -> speakWord());
    }

    // ── TTS ──────────────────────────────────────────────────────

    private void initTts() {
        tts = new TextToSpeech(activity, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                ttsReady = true;
            }
        });
    }

    private void speakWord() {
        if (!ttsReady || currentWord.isEmpty()) return;
        tts.speak(currentWord, TextToSpeech.QUEUE_FLUSH, null, "word_tts");
    }

    @Override
    public void onDestroyView() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onDestroyView();
    }

    // ── User stats ────────────────────────────────────────────────

    private void loadUserStats() {
        UConfig uConfig = new UConfig(activity);
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = uConfig.getData("name");
        if (name != null && !name.isEmpty()) {
            greetingNameTV.setText("Hey, " + name + " 👋");
        }

        int xp     = prefs.getInt(Constant.TOTAL_XP, 0);
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        int rank   = prefs.getInt(Constant.USER_RANK, 0);
        int lipto  = prefs.getInt(Constant.LIPTO_BALANCE, 0);

        totalXpTV.setText(String.valueOf(xp));
        streakCountTV.setText("🔥 " + streak);
        rankTV.setText(rank > 0 ? "#" + rank : "—");
        streakTV.setText("🔥 " + streak + " দিন");
        liptoBalanceTV.setText(String.valueOf(lipto));
        updateLevelUI(xp);

        // Refresh from server if logged in
        String token = uConfig.getData(Constant.TOKEN);
        if (token != null && !token.isEmpty()) {
            fetchStreakFromServer(prefs);
            fetchLiptoBalance(prefs);
        }
    }

    private void fetchStreakFromServer(SharedPreferences prefs) {
        ApiConfig.getRequest(activity, Constant.GAME_STREAK, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                JSONObject data = json.optJSONObject(Constant.DATA);
                if (data == null) return;
                int streak = data.optInt("streak_days", 0);
                int xp     = data.optInt("total_xp", 0);
                int rank   = data.optInt("rank", 0);

                prefs.edit()
                        .putInt(Constant.STREAK_DAYS, streak)
                        .putInt(Constant.TOTAL_XP, xp)
                        .putInt(Constant.USER_RANK, rank)
                        .apply();

                if (activity != null) activity.runOnUiThread(() -> {
                    totalXpTV.setText(String.valueOf(xp));
                    streakCountTV.setText("🔥 " + streak);
                    rankTV.setText(rank > 0 ? "#" + rank : "—");
                    streakTV.setText("🔥 " + streak + " দিন");
                    updateLevelUI(xp);
                });
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void fetchLiptoBalance(SharedPreferences prefs) {
        ApiConfig.getRequest(activity, Constant.GAME_LIPTO_BALANCE, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                int balance = json.optInt("balance", 0);
                prefs.edit().putInt(Constant.LIPTO_BALANCE, balance).apply();
                if (activity != null) activity.runOnUiThread(() ->
                        liptoBalanceTV.setText(String.valueOf(balance)));
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void updateLevelUI(int xp) {
        levelLabelTV.setText(LevelHelper.getLevelLabel(xp));
        levelTitleTV.setText(" · " + LevelHelper.getLevelTitle(xp));
        int toNext = LevelHelper.getXpToNextLevel(xp);
        xpToNextTV.setText(toNext > 0 ? toNext + " XP to next" : "Max Level!");
        levelProgressBar.setProgress(LevelHelper.getLevelProgressPercent(xp));
    }

    // ── Daily word ────────────────────────────────────────────────

    private void loadDailyWord() {
        ApiConfig.getRequest(activity, Constant.GAME_DAILY_WORD, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                JSONObject data = json.optJSONObject(Constant.DATA);
                if (data == null) return;

                String word    = data.optString("word", "");
                String meaning = data.optString("meaning", "");
                int lessonId   = data.optInt("lesson_id", 1);

                currentWord  = word;
                lastLessonId = lessonId > 0 ? lessonId : 1;

                if (activity != null) activity.runOnUiThread(() -> {
                    if (!word.isEmpty())    wordOfDayTV.setText(word);
                    if (!meaning.isEmpty()) wordMeaningTV.setText(meaning);
                });
            } catch (Exception ignored) {}
        }, error -> {});
    }

    // ── Grammar progress (lesson count + continue learning) ───────

    private void loadGrammarProgress() {
        // Fetch chapters to get total lesson count
        ApiConfig.getRequest(activity, Constant.GRAMMAR_CHAPTERS, response -> {
            try {
                JSONObject json = new JSONObject(response);
                // Grammar API uses different envelope — try both formats
                JSONArray chapters = null;
                if (json.has("data")) {
                    chapters = json.getJSONArray("data");
                } else if (json.has("chapters")) {
                    chapters = json.getJSONArray("chapters");
                }
                if (chapters == null) return;

                int totalLessons = 0;
                String firstChapterName = "";

                for (int i = 0; i < chapters.length(); i++) {
                    JSONObject ch = chapters.getJSONObject(i);
                    totalLessons += ch.optInt("lesson_count", ch.optInt("lessons_count", 0));
                    if (i == 0) {
                        firstChapterName = ch.optString("title", ch.optString("name", ""));
                    }
                }

                final int total = totalLessons;
                final String chapName = firstChapterName;

                if (activity != null) activity.runOnUiThread(() -> {
                    lessonCountTV.setText(total + " lessons");
                    if (!chapName.isEmpty()) continueLessonTV.setText(chapName);
                });

            } catch (Exception ignored) {}
        }, error -> {});
    }
}
