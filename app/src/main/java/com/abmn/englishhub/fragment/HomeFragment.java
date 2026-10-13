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

import com.abmn.englishhub.Activity.ChapterActivity;
import com.abmn.englishhub.Activity.ItemActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.Activity.WordActivity;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.englishhub.Helper.AppTextToSpeechHelper;
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
    private TextView streakTV, greetingNameTV, greetingTimeTV;

    // Continue learning
    private TextView continueLessonTV, continueLabelTV;

    // TTS engine
    private AppTextToSpeechHelper tts;

    // Last lesson id for Quick Quiz + Word of the Day
    private int lastLessonId = 1;

    // Phase-1 shortcuts: Lipto chip, today's quiz, mistake-notebook review
    private View liptoBadge, dailyQuizCard, reviewCard;
    private TextView liptoChipTV, dailyQuizSubTV, reviewSubTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        activity = getActivity();
        // MainActivity already pads its root by the system-bar insets; applying
        // them again here doubled the gap under the toolbar on every tab.
        applyHomeTheme(view);
        bindViews(view);
        initTts();
        setupClickListeners();
        loadUserStats();
        loadDailyWord();
        loadGrammarProgress();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // The fragment's view can stay alive for hours (bottom-nav tab switches,
        // app left open across a meal break, etc.) while onCreateView only runs
        // once, so the time-of-day greeting was going stale. Recompute it every
        // time the tab becomes visible again.
        updateTimeGreeting();
        // Coming back from a quiz/lesson resumes this same fragment (no
        // onCreateView), so streak / "continue where you left off" stayed at
        // whatever they were before playing. Refresh both on every resume.
        refreshUserStats();
        refreshContinueCard();
        refreshShortcuts();
    }

    // ── Theme ─────────────────────────────────────────────────────

    private void applyHomeTheme(View root) {
        if (activity == null) return;
        int index = com.abmn.englishhub.Helper.ThemePrefs.getHomePalette(activity);
        root.setBackgroundColor(com.abmn.englishhub.Helper.ThemePrefs.getPalette(index).background);
    }

    // ── View binding ─────────────────────────────────────────────

    private void bindViews(View view) {
        greetingTimeTV  = view.findViewById(R.id.greetingTimeTV);
        updateTimeGreeting();

        greetingNameTV  = view.findViewById(R.id.greetingNameTV);
        streakTV        = view.findViewById(R.id.streakTV);
        wordOfDayTV     = view.findViewById(R.id.wordOfDayTV);
        wordMeaningTV   = view.findViewById(R.id.wordMeaningTV);
        ttsBtn          = view.findViewById(R.id.ttsBtn);
        continueLessonTV  = view.findViewById(R.id.continueLessonTV);
        continueLabelTV   = view.findViewById(R.id.continueLabelTV);

        liptoBadge      = view.findViewById(R.id.liptoBadge);
        liptoChipTV     = view.findViewById(R.id.liptoChipTV);
        dailyQuizCard   = view.findViewById(R.id.dailyQuizCard);
        dailyQuizSubTV  = view.findViewById(R.id.dailyQuizSubTV);
        reviewCard      = view.findViewById(R.id.reviewCard);
        reviewSubTV     = view.findViewById(R.id.reviewSubTV);

        liptoBadge.setOnClickListener(v -> startActivity(
                new Intent(activity, com.abmn.englishhub.Activity.LiptoPassbookActivity.class)));
        reviewCard.setOnClickListener(v -> startActivity(
                new Intent(activity, com.abmn.englishhub.Activity.MistakeNotebookActivity.class)));
        // Straight into the next round of the last lesson (or the chapter
        // picker for a first-time player) - see QuizLauncher.
        dailyQuizCard.setOnClickListener(v ->
                com.abmn.englishhub.Helper.QuizLauncher.startQuickQuiz(activity));

        // One-time pointer to the new Practice tab (the quiz / battle /
        // leaderboard tiles that used to sit here moved there).
        View hintCard = view.findViewById(R.id.practiceHintCard);
        SharedPreferences hintPrefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        hintCard.setVisibility(hintPrefs.getBoolean(Constant.PRACTICE_HINT_DISMISSED, false)
                ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.practiceHintCloseTV).setOnClickListener(v -> {
            hintPrefs.edit().putBoolean(Constant.PRACTICE_HINT_DISMISSED, true).apply();
            hintCard.setVisibility(View.GONE);
        });
        hintCard.setOnClickListener(v -> {
            hintPrefs.edit().putBoolean(Constant.PRACTICE_HINT_DISMISSED, true).apply();
            if (activity instanceof com.abmn.englishhub.Activity.MainActivity) {
                ((com.abmn.englishhub.Activity.MainActivity) activity).openTab(R.id.navPractice);
            }
        });
        view.findViewById(R.id.continueLearningCard).setOnClickListener(v -> {
            SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            String lastSlug = prefs.getString(Constant.LAST_CHAPTER_SLUG, null);
            String lastTitle = prefs.getString(Constant.LAST_CHAPTER_TITLE, null);
            String lastItemSlug = prefs.getString(Constant.LAST_ITEM_SLUG, "");
            if (lastSlug != null && lastTitle != null) {
                Intent chapterIntent = new Intent(activity, ItemActivity.class)
                        .putExtra(Constant.FROM, lastSlug)
                        .putExtra(Constant.FROM_TITLE, lastTitle);
                if (!lastItemSlug.isEmpty()) {
                    // Reopen the exact lesson, with the chapter list underneath
                    // so Back still lands where the user expects.
                    Intent itemIntent = new Intent(activity, com.abmn.englishhub.Activity.ItemDetailsActivity.class)
                            .putExtra(Constant.FROM, lastItemSlug)
                            .putExtra("from_chapter", true);
                    activity.startActivities(new Intent[]{chapterIntent, itemIntent});
                } else {
                    startActivity(chapterIntent);
                }
            } else {
                startActivity(new Intent(activity, ChapterActivity.class).putExtra("type", "grammar"));
            }
        });
        view.findViewById(R.id.wordOfDayCard).setOnClickListener(v ->
                startActivity(new Intent(activity, WordActivity.class)
                        .putExtra(Constant.FROM, String.valueOf(lastLessonId))
                        .putExtra(Constant.FROM_TITLE, "Word of the Day")));
    }

    // ── Greeting ─────────────────────────────────────────────────

    private void updateTimeGreeting() {
        if (greetingTimeTV == null) return;
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String timeGreeting = hour < 12 ? "Good morning ☀️"
                : hour < 17 ? "Good afternoon 👋"
                : "Good evening 🌙";
        greetingTimeTV.setText(timeGreeting);
    }

    // ── Click listeners ──────────────────────────────────────────

    private void setupClickListeners() {
        // Quiz / leaderboard / battle / tournament entry points now live in
        // PracticeFragment (the Practice tab).
        ttsBtn.setOnClickListener(v -> speakWord());
    }

    // ── TTS ──────────────────────────────────────────────────────

    private void initTts() {
        UConfig uConfig = new UConfig(activity);
        tts = new AppTextToSpeechHelper(activity, uConfig.getData(Constant.VOICE_SPEED));
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

        // The layout already draws a flame icon next to this text - adding a
        // 🔥 here showed two flames.
        int streak = prefs.getInt(Constant.STREAK_DAYS, 0);
        streakTV.setText(streak + " দিন");
    }

    // Server refresh of streak / XP / rank / Lipto. Runs from onResume (not
    // onCreateView) so it fires exactly once per time the tab is shown,
    // including right after returning from a quiz.
    private void refreshUserStats() {
        if (activity == null) return;
        UConfig uConfig = new UConfig(activity);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        // Always repaint from prefs (cheap, and a finished quiz has just
        // written fresh values there); only hit the network once per gate window.
        streakTV.setText(prefs.getInt(Constant.STREAK_DAYS, 0) + " দিন");
        if (!com.abmn.englishhub.Helper.StatsRefreshGate.tryAcquire()) return;
        fetchStreakFromServer(prefs, token);
        fetchLiptoBalance(prefs, token);
    }

    // Lipto chip + today's-quiz subtitle + mistake-review card, all from
    // local prefs / the local notebook - no network.
    private void refreshShortcuts() {
        if (activity == null || liptoBadge == null) return;
        UConfig uConfig = new UConfig(activity);
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String token = uConfig.getData(Constant.TOKEN);
        boolean loggedIn = token != null && !token.isEmpty();
        if (loggedIn && prefs.contains(Constant.LIPTO_BALANCE)) {
            liptoChipTV.setText(String.valueOf(prefs.getInt(Constant.LIPTO_BALANCE, 0)));
            liptoBadge.setVisibility(View.VISIBLE);
        } else {
            liptoBadge.setVisibility(View.GONE);
        }

        String quizTitle = prefs.getString(Constant.LAST_QUIZ_LESSON_TITLE, "");
        boolean hasLesson = prefs.getInt(Constant.LAST_QUIZ_LESSON_ID, 0) > 0;
        dailyQuizSubTV.setText(hasLesson && !quizTitle.isEmpty()
                ? quizTitle
                : hasLesson ? "আগের কুইজ চালিয়ে যাও"
                            : "নতুন কুইজ শুরু করো");

        int mistakes = com.abmn.englishhub.Helper.MistakeNotebook.getAll(activity).size();
        if (mistakes > 0) {
            reviewSubTV.setText(mistakes + " টি শব্দ রিভিউ করো");
            reviewCard.setVisibility(View.VISIBLE);
        } else {
            reviewCard.setVisibility(View.GONE);
        }
    }

    private void refreshContinueCard() {
        if (activity == null || continueLessonTV == null) return;
        SharedPreferences cp = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String shown = cp.getString(Constant.LAST_ITEM_TITLE, "");
        if (shown == null || shown.isEmpty()) shown = cp.getString(Constant.LAST_CHAPTER_TITLE, "");
        if (shown != null && !shown.isEmpty()) {
            if (continueLabelTV != null) continueLabelTV.setText("যেখানে ছেড়েছিলে");
            continueLessonTV.setText(shown);
        } else {
            // Nothing opened yet: "where you left off" would be a lie, and the
            // card opens the Grammar chapter list, so say exactly that.
            if (continueLabelTV != null) continueLabelTV.setText("শেখা শুরু করো");
            continueLessonTV.setText("Grammar দিয়ে শুরু করো");
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

                if (activity != null) activity.runOnUiThread(() -> {
                    if (isAdded() && streakTV != null) streakTV.setText(streak + " দিন");
                });
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void fetchLiptoBalance(SharedPreferences prefs, String token) {
        ApiConfig.getRequest(activity, Constant.GAME_LIPTO_BALANCE, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                int balance = json.optInt("balance", 0);
                int maxBalance = json.optInt("max_balance", balance);
                prefs.edit()
                        .putInt(Constant.LIPTO_BALANCE, balance)
                        .putInt(Constant.LIPTO_MAX_BALANCE, Math.max(maxBalance, prefs.getInt(Constant.LIPTO_MAX_BALANCE, 0)))
                        .apply();
                if (activity != null) activity.runOnUiThread(() -> {
                    if (isAdded()) refreshShortcuts();
                });
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

    // Chapter list barely changes; re-fetching it every time the user
    // switches back to the Home tab is wasted network/parsing work.
    private static final long GRAMMAR_PROGRESS_CACHE_TTL_MS = 30 * 60 * 1000L;

    private void loadGrammarProgress() {
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String lastTitle = prefs.getString(Constant.LAST_CHAPTER_TITLE, null);
        refreshContinueCard();

        long cacheTime = prefs.getLong(Constant.GRAMMAR_PROGRESS_CACHE_TIME, 0);
        if (System.currentTimeMillis() - cacheTime < GRAMMAR_PROGRESS_CACHE_TTL_MS) {
            int cachedTotal = prefs.getInt(Constant.GRAMMAR_PROGRESS_TOTAL_LESSONS, -1);
            if (cachedTotal > 0) {
                return;
            } else if (cachedTotal == 0) {
                return;
            }
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

                prefs.edit()
                        .putInt(Constant.GRAMMAR_PROGRESS_TOTAL_LESSONS, total)
                        .putString(Constant.GRAMMAR_PROGRESS_FIRST_CHAPTER, chapName)
                        .putLong(Constant.GRAMMAR_PROGRESS_CACHE_TIME, System.currentTimeMillis())
                        .apply();

            } catch (Exception ignored) {}
        }, error -> {});
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (tts != null) tts.shutdown();
    }
}
