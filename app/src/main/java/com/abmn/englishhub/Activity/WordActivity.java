package com.abmn.englishhub.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import com.abmn.englishhub.Helper.InterstitialAdManager;
import com.abmn.englishhub.Model.WordModel;
import com.abmn.texttospeech.Base;
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
    private CardView headerCV;
    private String getLessonId;
    private WordAdapter adapter;
    private ImageView wordCloseIV, meaningCloseIV;
    private Boolean isWordClose = false, isMeaningClose = false;
    private InterstitialAdManager interstitialAdManager;

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
        setToolbar("Vocabulary: " + getLessonTitle);

        Base.setVoiceSpeed(uConfig.getData(Constant.VOICE_SPEED));

        wordTvW = findViewById(R.id.wordTvW);
        meaningTvW = findViewById(R.id.meaningTvW);
        synonymsTvW = findViewById(R.id.synonymsTvW);
        antonymsTvW = findViewById(R.id.antonymsTvW);
        counterTvW = findViewById(R.id.counterTvW);
        headerCV = findViewById(R.id.headerCV);

        wordCloseIV = findViewById(R.id.wordCloseIV);
        meaningCloseIV = findViewById(R.id.meaningCloseIV);

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
        } else {
            wordTvW.setText("শব্দ");
            meaningTvW.setText("অর্থ");
            synonymsTvW.setText("সমার্থক শব্দ");
            antonymsTvW.setText("বিপরীত শব্দ");
        }
        setToolbar(getLessonTitle);

        wordRV = findViewById(R.id.wordRV);

        if (!uConfig.isConnected()){
            uConfig.isConnectedAlert("", "");
        }

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
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        allowMultilineTitle(toolbar);
    }

    private void allowMultilineTitle(Toolbar toolbar) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                TextView titleView = (TextView) child;
                titleView.setSingleLine(false);
                titleView.setMaxLines(2);
                titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                break;
            }
        }
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
        return super.onOptionsItemSelected(item);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

            Base.setVoiceSpeed(uConfig.getData(Constant.VOICE_SPEED));
            adapter.refreshTtsSpeed();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();

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
        try {
            ApiConfig.RequestToVolley((result, response, error) -> {
                try {
                    if (result) {
                        JSONObject chaptersObject = new JSONObject(response).getJSONObject(Constant.WORDS);
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
                        }
                        updateColumnHeaderVisibility();
                        Objects.requireNonNull(wordRV.getAdapter()).notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    // ignored
                } finally {
                    isLoading = false;
                }
            }, Request.Method.GET, activity, url, new HashMap<>(), true);
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
    }

    @Override
    public void onDestroy() {
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
}