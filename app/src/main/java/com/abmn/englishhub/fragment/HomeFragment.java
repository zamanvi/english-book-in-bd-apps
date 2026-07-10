package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.BattleActivity;
import com.abmn.englishhub.Activity.ChapterActivity;
import com.abmn.englishhub.Activity.GroupActivity;
import com.abmn.englishhub.Activity.ItemActivity;
import com.abmn.englishhub.Activity.LeaderboardActivity;
import com.abmn.englishhub.Activity.QuizActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.Activity.WizardChapterActivity;
import com.abmn.englishhub.Activity.WordActivity;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.texttospeech.TextToSpeechHelper;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private Activity activity;

    // Word of day
    private TextView wordOfDayTV, wordMeaningTV;
    private CardView ttsBtn;
    private String currentWord = "";

    // Header
    private TextView streakTV, greetingNameTV;

    // Continue learning
    private TextView lessonCountTV, continueLessonTV;

    // Quick actions
    private CardView quickQuizBtn, leaderboardBtn, groupBtn, battleBtn;

    // TTS engine
    private TextToSpeechHelper tts;

    // Last lesson id for Quick Quiz + Word of the Day
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
        wordOfDayTV     = view.findViewById(R.id.wordOfDayTV);
        wordMeaningTV   = view.findViewById(R.id.wordMeaningTV);
        ttsBtn          = view.findViewById(R.id.ttsBtn);
        lessonCountTV   = view.findViewById(R.id.lessonCountTV);
        continueLessonTV  = view.findViewById(R.id.continueLessonTV);
        quickQuizBtn    = view.findViewById(R.id.quickQuizBtn);
        leaderboardBtn  = view.findViewById(R.id.leaderboardBtn);
        groupBtn        = view.findViewById(R.id.groupBtn);
        battleBtn       = view.findViewById(R.id.battleBtn);

        // Category cards
        view.findViewById(R.id.vocabularyLL).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyActivity.class)));
        view.findViewById(R.id.grammarLL).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar")));
        view.findViewById(R.id.wizardLL).setOnClickListener(v ->
                startActivity(new Intent(activity, WizardChapterActivity.class)));
        view.findViewById(R.id.writingAndReadingLL).setOnClickListener(v ->
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "writing_reading")));
        view.findViewById(R.id.continueLearningCard).setOnClickListener(v -> {
            SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            String lastSlug = prefs.getString(Constant.LAST_CHAPTER_SLUG, null);
            String lastTitle = prefs.getString(Constant.LAST_CHAPTER_TITLE, null);
            if (lastSlug != null && lastTitle != null) {
                startActivity(new Intent(activity, ItemActivity.class)
                        .putExtra(Constant.FROM, lastSlug)
                        .putExtra(Constant.FROM_TITLE, lastTitle));
            } else {
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar"));
            }
        });
        view.findViewById(R.id.wordOfDayCard).setOnClickListener(v ->
                startActivity(new Intent(activity, WordActivity.class)
                        .putExtra(Constant.FROM, String.valueOf(lastLessonId))
                        .putExtra(Constant.FROM_TITLE, "Word of the Day")));
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
        UConfig uConfig = new UConfig(activity);
        tts = new TextToSpeechHelper(activity, uConfig.getData(Constant.VOICE_SPEED));
    }

    private void speakWord() {
        if (currentWord.isEmpty()) return;
        tts.speak(currentWord);
    }

    // ── User stats ────────────────────────────────────────────────

    private void loadUserStats() {
        UConfig uConfig = new UConfig(activity);
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String name = uConfig.getData("name");
        if (name != null && !name.isEmpty()) {
            greetingNameTV.setText("Hey, " + name + " 👋");
        }

        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        streakTV.setText("🔥 " + streak + " দিন");

        // Refresh from server if logged in
        String token = uConfig.getData(Constant.TOKEN);
        if (token != null && !token.isEmpty()) {
            fetchStreakFromServer(prefs, token);
            fetchLiptoBalance(prefs, token);
        }
    }

    private void fetchStreakFromServer(SharedPreferences prefs, String token) {
        ApiConfig.getRequest(activity, Constant.GAME_STREAK, token, response -> {
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

                if (activity != null) activity.runOnUiThread(() ->
                        streakTV.setText("🔥 " + streak + " দিন"));
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void fetchLiptoBalance(SharedPreferences prefs, String token) {
        ApiConfig.getRequest(activity, Constant.GAME_LIPTO_BALANCE, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                int balance = json.optInt("balance", 0);
                prefs.edit().putInt(Constant.LIPTO_BALANCE, balance).apply();
            } catch (Exception ignored) {}
        }, error -> {});
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
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String lastTitle = prefs.getString(Constant.LAST_CHAPTER_TITLE, null);
        if (lastTitle != null && !lastTitle.isEmpty()) {
            continueLessonTV.setText(lastTitle);
        }

        // Fetch chapters to get total lesson count, and (for users who
        // haven't opened anything yet) a sensible default chapter name.
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
                    if (lastTitle == null && !chapName.isEmpty()) continueLessonTV.setText(chapName);
                });

            } catch (Exception ignored) {}
        }, error -> {});
    }
}
