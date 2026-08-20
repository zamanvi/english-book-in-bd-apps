package com.abmn.englishhub.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.WordAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.GuestNudgeHelper;
import com.abmn.englishhub.Helper.InterstitialAdManager;
import com.abmn.englishhub.Model.WordModel;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.abmn.englishhub.R;
import com.android.volley.Request;

public class WordActivity extends AppCompatActivity {

    private Activity activity;
    private UConfig uConfig;
    private RecyclerView wordRV;
    private List<WordModel> wordList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;
    private TextView synonymsTvW;
    private TextView antonymsTvW;
    private TextView wordTvW, meaningTvW, counterTvW;
    private TextView lessonTitleTV;
    private CardView headerCV;
    private View headerDividerV, headerSynAntLL;
    private String getLessonId;
    private WordAdapter adapter;
    private ImageView wordCloseIV, meaningCloseIV;
    private Boolean isWordClose = false, isMeaningClose = false;
    private InterstitialAdManager interstitialAdManager;
    private CardView unlockCardCV;
    private TextView unlockSubTV, unlockErrorTV;
    private com.google.android.material.button.MaterialButton unlockBtn;
    private CardView guestNudgeCV;
    private TextView guestNudgeCloseTV;
    private com.google.android.material.button.MaterialButton guestNudgeBtn;
    private CardView takeQuizBtnCV;
    private View wordEmptyLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_word);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        define();
        wordList = new ArrayList<>();
        adapter = new WordAdapter(wordList, activity);
        adapter.setStorybook(isStorybookTheme());
        wordRV.setAdapter(adapter);
        fetchData(currentPage);
        applyScreenTheme(isStorybookTheme());
    }

    @SuppressLint("SetTextI18n")
    private void define() {

        activity = this;
        uConfig = new UConfig(activity);

        getLessonId = getIntent().getStringExtra(Constant.FROM);
        String getLessonTitle = getIntent().getStringExtra(Constant.FROM_TITLE);
        String getLessonType = getIntent().getStringExtra(Constant.FROM_TYPE);

        wordTvW = findViewById(R.id.wordTvW);
        meaningTvW = findViewById(R.id.meaningTvW);
        synonymsTvW = findViewById(R.id.synonymsTvW);
        antonymsTvW = findViewById(R.id.antonymsTvW);
        counterTvW = findViewById(R.id.counterTvW);
        headerDividerV = findViewById(R.id.headerDividerV);
        headerSynAntLL = findViewById(R.id.headerSynAntLL);
        headerCV = findViewById(R.id.headerCV);

        wordCloseIV = findViewById(R.id.wordCloseIV);
        meaningCloseIV = findViewById(R.id.meaningCloseIV);

        unlockCardCV = findViewById(R.id.unlockCardCV);
        unlockSubTV = findViewById(R.id.unlockSubTV);
        unlockErrorTV = findViewById(R.id.unlockErrorTV);
        unlockBtn = findViewById(R.id.unlockBtn);
        unlockBtn.setOnClickListener(v -> unlockLesson());

        guestNudgeCV = findViewById(R.id.guestNudgeCV);
        guestNudgeCloseTV = findViewById(R.id.guestNudgeCloseTV);
        guestNudgeBtn = findViewById(R.id.guestNudgeBtn);

        wordEmptyLL = findViewById(R.id.wordEmptyLL);

        takeQuizBtnCV = findViewById(R.id.takeQuizBtnCV);
        takeQuizBtnCV.setOnClickListener(v -> {
            try {
                startActivity(new Intent(activity, LevelMapActivity.class)
                        .putExtra("lesson_id", Integer.parseInt(getLessonId)));
            } catch (NumberFormatException ignored) {}
        });

        wordTvW.setOnClickListener(this::wordChange);
        wordCloseIV.setOnClickListener(this::wordChange);
        meaningTvW.setOnClickListener(this::meaningChange);
        meaningCloseIV.setOnClickListener(this::meaningChange);

        if ("american_british".equals(getLessonType)) {
            wordTvW.setText("শব্দ");
            meaningTvW.setText("অর্থ");
            synonymsTvW.setText("🇺🇸 American");
            antonymsTvW.setText("🇬🇧 British");
        } else if ("exam_vocab_appeared".equals(getLessonType) || "exam_vocab_upcoming".equals(getLessonType)) {
            wordTvW.setText("শব্দ");
            meaningTvW.setText("অর্থ");
            synonymsTvW.setText("📋 পরীক্ষা / বিষয়");
            antonymsTvW.setText("🔤 English Synonym");
        } else if ("verb".equals(getLessonType)) {
            wordTvW.setText("Verb 1");
            meaningTvW.setText("অর্থ");
            synonymsTvW.setText("Verb 2");
            antonymsTvW.setText("Verb 3");
        } else if ("vocabulary".equals(getLessonType)) {
            wordTvW.setText("শব্দ");
            meaningTvW.setText("অর্থ");
            synonymsTvW.setText("উৎস / বিষয়");
            antonymsTvW.setText("গুরুত্ব %");
        } else {
            wordTvW.setText("শব্দ");
            meaningTvW.setText("অর্থ");
            synonymsTvW.setText("সমার্থক শব্দ");
            antonymsTvW.setText("বিপরীত শব্দ");
        }
        setToolbar(getLessonTitle);

        wordRV = findViewById(R.id.wordRV);

        // No unconditional "no internet" check here - fetchData() below goes
        // through ApiConfig, which serves cached content silently when
        // offline and only alerts if there's truly nothing cached to show.

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        wordRV.setLayoutManager(linearLayout);
        wordRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    assert layoutManager != null;
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreData();
                    }
                }
            }
        });
        CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onFinish() {
                callAds();
            }

            @Override
            public void onTick(long l) {

            }
        };
        countDownTimer.start();
    }

    private void callAds() {
        String interstitialAdId;
        if (uConfig.getBoolean(Constant.IS_TEST_ADS)){
            interstitialAdId = this.getString(R.string.INTERSTITIAL_UNIT_ID_LOCAL);
        }else {
            interstitialAdId = getString(R.string.INTERSTITIAL_UNIT_ID);
        }
        interstitialAdManager = new InterstitialAdManager(activity, interstitialAdId);
        interstitialAdManager.loadInterstitialAd();
    }

    private void wordChange(View view){
        if (isWordClose) {
            isWordClose = false;
            wordCloseIV.setImageResource(R.drawable.ic_eye_closed);
            adapter.removeBlurWordText();
        } else {
            isWordClose = true;
            wordCloseIV.setImageResource(R.drawable.ic_eye_open);
            adapter.blurWordText();
        }
    }
    private void meaningChange(View view){
        if (isMeaningClose) {
            isMeaningClose = false;
            meaningCloseIV.setImageResource(R.drawable.ic_eye_closed);
            adapter.removeBlurMeaningText();
        } else {
            isMeaningClose = true;
            meaningCloseIV.setImageResource(R.drawable.ic_eye_open);
            adapter.blurMeaningText();
        }
    }

    private void setToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        lessonTitleTV = findViewById(R.id.lessonTitleTV);
        lessonTitleTV.setText(title);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (interstitialAdManager != null) {
            interstitialAdManager.showOnExit(() -> super.onBackPressed());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.word_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.nav_voice_id) {
            openVoiceControlPopUp();
            return true;
        }
        if (item.getItemId() == R.id.themeToggleId) {
            toggleTheme();
            return true;
        }
        if (item.getItemId() == R.id.shareLessonId) {
            shareLesson();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Lets a friend see the chapter title and a few sample words before
    // installing - same share pattern as ProfileFragment's generic app
    // share, but built around this specific lesson's content.
    private void shareLesson() {
        String title = lessonTitleTV.getText() != null ? lessonTitleTV.getText().toString() : "";
        StringBuilder wordsPreview = new StringBuilder();
        int sampleCount = Math.min(5, wordList.size());
        for (int i = 0; i < sampleCount; i++) {
            if (wordsPreview.length() > 0) wordsPreview.append(", ");
            wordsPreview.append(wordList.get(i).getWord());
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareMessage = "📚 \"" + title + "\" চ্যাপ্টারটা দেখো!\n\n"
                + (wordsPreview.length() > 0 ? "কিছু শব্দ: " + wordsPreview + "...\n\n" : "")
                + "🎁 বন্ধুকে invite করুন এবং 50 LIPTO পান!\n\n"
                + "পুরো চ্যাপ্টার আর কুইজ খেলতে English Grammar Book অ্যাপ নামাও 👇\n"
                + "https://play.google.com/store/apps/details?id=" + activity.getPackageName()
                + "\n#EnglishGrammarBook #LearnEnglish";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "শেয়ার করো"));
    }

    private boolean isStorybookTheme() {
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return Constant.CONTENT_THEME_STORYBOOK.equals(
                prefs.getString(Constant.CONTENT_THEME, Constant.CONTENT_THEME_STORYBOOK));
    }

    private void toggleTheme() {
        boolean next = !isStorybookTheme();
        activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(Constant.CONTENT_THEME, next ? Constant.CONTENT_THEME_STORYBOOK : Constant.CONTENT_THEME_APP)
                .apply();
        adapter.setStorybook(next);
        applyScreenTheme(next);
    }

    private void applyScreenTheme(boolean storybook) {
        findViewById(R.id.main).setBackgroundColor(android.graphics.Color.parseColor(
                storybook ? "#F7F1E1" : "#07081A"));
        lessonTitleTV.setTextColor(storybook
                ? android.graphics.Color.parseColor("#3A2E1A")
                : androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
        headerCV.setCardBackgroundColor(storybook
                ? android.graphics.Color.parseColor("#EFE6CC")
                : androidx.core.content.ContextCompat.getColor(this, R.color.bg_elevated));

        int synColor = storybook
                ? android.graphics.Color.parseColor("#1F7A5C")
                : androidx.core.content.ContextCompat.getColor(this, R.color.teal);
        int antColor = storybook
                ? android.graphics.Color.parseColor("#B0203A")
                : androidx.core.content.ContextCompat.getColor(this, R.color.red_wrong);
        int inactiveColor = storybook
                ? android.graphics.Color.parseColor("#8A7A5C")
                : androidx.core.content.ContextCompat.getColor(this, R.color.text_inactive);

        counterTvW.setTextColor(synColor);
        wordTvW.setTextColor(inactiveColor);
        meaningTvW.setTextColor(inactiveColor);
        synonymsTvW.setTextColor(synColor);
        antonymsTvW.setTextColor(antColor);
        wordCloseIV.setColorFilter(inactiveColor);
        meaningCloseIV.setColorFilter(inactiveColor);
    }

    private void openVoiceControlPopUp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_App_Dialog);
        builder.setTitle("Select Voice Speed");

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.lyt_voice, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupVoiceSpeed);

        // Set default checked value based on current speed
        switch (uConfig.getData(Constant.VOICE_SPEED)) {
            case "slower":
                ((RadioButton) dialogView.findViewById(R.id.radioSlower)).setChecked(true);
                break;
            case "slow":
                ((RadioButton) dialogView.findViewById(R.id.radioSlow)).setChecked(true);
                break;
            case "normal":
                ((RadioButton) dialogView.findViewById(R.id.radioNormal)).setChecked(true);
                break;
            case "fast":
                ((RadioButton) dialogView.findViewById(R.id.radioFast)).setChecked(true);
                break;
            case "faster":
                ((RadioButton) dialogView.findViewById(R.id.radioFaster)).setChecked(true);
                break;
        }

        builder.setPositiveButton("OK", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == R.id.radioSlower) {
                uConfig.setData(Constant.VOICE_SPEED, "slower");
            } else if (selectedId == R.id.radioSlow) {
                uConfig.setData(Constant.VOICE_SPEED, "slow");
            } else if (selectedId == R.id.radioNormal) {
                uConfig.setData(Constant.VOICE_SPEED, "normal");
            } else if (selectedId == R.id.radioFast) {
                uConfig.setData(Constant.VOICE_SPEED, "fast");
            } else if (selectedId == R.id.radioFaster) {
                uConfig.setData(Constant.VOICE_SPEED, "faster");
            }

            adapter.refreshTtsSpeed();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    // ── Premium unlock ────────────────────────────────────────────

    private void updatePremiumUnlockUI(boolean isPremium, boolean unlocked, int lockedRemaining) {
        if (isPremium && !unlocked) {
            unlockSubTV.setText(lockedRemaining + " টি শব্দ লক করা আছে — সম্পূর্ণ লেসন আনলক করে সব দেখো");
            unlockCardCV.setVisibility(View.VISIBLE);
        } else {
            unlockCardCV.setVisibility(View.GONE);
            maybeShowGuestNudge();
        }
        // Free lessons already get a quiz shortcut from the lesson list -
        // this is only needed for a Premium lesson once it's actually unlocked.
        takeQuizBtnCV.setVisibility(isPremium && unlocked ? View.VISIBLE : View.GONE);
    }

    // Only offered on content that's actually readable right now (Free, or
    // Premium-and-already-unlocked) - a locked Premium lesson shows the
    // unlock card instead, not this.
    private void maybeShowGuestNudge() {
        String token = uConfig.getData(Constant.TOKEN);
        if (GuestNudgeHelper.shouldShow(activity, token)) {
            GuestNudgeHelper.show(activity, guestNudgeCV, guestNudgeCloseTV, guestNudgeBtn);
        }
    }

    @SuppressLint("SetTextI18n")
    private void unlockLesson() {
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            showUnlockError("লগইন করো আগে");
            return;
        }

        unlockBtn.setEnabled(false);
        unlockErrorTV.setVisibility(View.GONE);
        String url = Constant.PREMIUM_LESSON_UNLOCK + getLessonId + "/unlock";

        ApiConfig.postRequest(activity, url, new JSONObject(), token, response -> runOnUiThread(() -> {
            unlockCardCV.setVisibility(View.GONE);
            wordList.clear();
            currentPage = 1;
            lastPage = 1;
            fetchData(1);
        }), error -> {
            String message = error.getMessage();
            // The backend's raw "Insufficient Lipto balance" is English and
            // dead-ends the user - point them toward the two ways to actually
            // get more Lipto instead.
            final String finalMessage = (message != null && message.toLowerCase().contains("insufficient"))
                    ? "💎 Lipto কম আছে? ফ্রি লেসন পড়ে অথবা বন্ধুর থেকে Lipto নিয়ে জমাও!"
                    : (message != null ? message : "নেটওয়ার্ক সমস্যা");
            runOnUiThread(() -> showUnlockError(finalMessage));
        });
    }

    private void showUnlockError(String msg) {
        unlockBtn.setEnabled(true);
        unlockErrorTV.setText(msg);
        unlockErrorTV.setVisibility(View.VISIBLE);
    }

    private void loadMoreData() {
        if (currentPage < lastPage) {
            isLoading = true;
            currentPage++;
            fetchData(currentPage);
        } else {
            isLoading = false;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchData(int page) {
        String url = Constant.ROOT_API2 + Constant.WORDS + "/" + getLessonId + "?page=" + page;
        String tag = "WordActivity";
        String cacheKey = "vocab_words_" + getLessonId + "_p" + page;
        try {
            ApiConfig.RequestToVolley((result, response, error) -> {
                try {
                    if (result) {
                        JSONObject root = new JSONObject(response);
                        JSONObject chaptersObject = root.getJSONObject(Constant.WORDS);
                        JSONArray dataArray = chaptersObject.getJSONArray(Constant.DATA);

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject wordObj = dataArray.getJSONObject(i);
                            int id = wordObj.getInt("id");
                            String word = wordObj.getString("word");
                            String meaning = wordObj.getString("meaning");
                            String synonyms = wordObj.getString("synonyms");
                            String antonyms = wordObj.getString("antonyms");
                            String lesson_id = wordObj.getString("lesson_id");
                            boolean status = wordObj.getBoolean("status");
                            String created_at = wordObj.getString("created_at");
                            String updated_at = wordObj.getString("updated_at");

                            WordModel model = new WordModel(id, word, meaning, synonyms, antonyms, lesson_id, status, created_at, updated_at);
                            wordList.add(model);
                        }
                        if (currentPage == 1) {
                            lastPage = chaptersObject.getInt("last_page");
                            boolean isPremium = root.optBoolean("is_premium", false);
                            boolean unlocked = root.optBoolean("unlocked", true);
                            int lockedRemaining = root.optInt("locked_remaining", 0);
                            runOnUiThread(() -> updatePremiumUnlockUI(isPremium, unlocked, lockedRemaining));
                            // A locked Premium lesson already explains itself via the
                            // unlock card - only show the "no words" empty state for
                            // content that's genuinely supposed to be readable now.
                            boolean readableNow = !(isPremium && !unlocked);
                            boolean isEmpty = wordList.isEmpty();
                            runOnUiThread(() -> wordEmptyLL.setVisibility(
                                    readableNow && isEmpty ? View.VISIBLE : View.GONE));
                        }
                        updateColumnHeaderVisibility();
                        Objects.requireNonNull(wordRV.getAdapter()).notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    // ignored
                } finally {
                    isLoading = false;
                }
            }, Request.Method.GET, activity, url, new HashMap<>(), true, cacheKey);
        } catch (Exception e) {
            // ignored
        }
    }

    // If not a single word in this lesson has a synonym/antonym value, the
    // column header for it is dead weight - hide it instead of showing an
    // empty label over a blank column.
    private void updateColumnHeaderVisibility() {
        boolean anySyn = false, anyAnt = false;
        for (WordModel model : wordList) {
            String syn = model.getSynonyms();
            String ant = model.getAntonyms();
            if (syn != null && !syn.isEmpty() && !"null".equals(syn)) anySyn = true;
            if (ant != null && !ant.isEmpty() && !"null".equals(ant)) anyAnt = true;
            if (anySyn && anyAnt) break;
        }
        synonymsTvW.setVisibility(anySyn ? View.VISIBLE : View.GONE);
        antonymsTvW.setVisibility(anyAnt ? View.VISIBLE : View.GONE);

        // Same collapse as the data rows below it (WordAdapter) - otherwise
        // the header keeps reserving the old column width + divider, so its
        // labels sit narrower than the full-width word/meaning cells beneath.
        boolean showSynAntCell = anySyn || anyAnt;
        headerDividerV.setVisibility(showSynAntCell ? View.VISIBLE : View.GONE);
        headerSynAntLL.setVisibility(showSynAntCell ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroy() {
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
}