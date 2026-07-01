package com.abmn.englishhub.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;
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
        wordRV.setAdapter(adapter);
        fetchData(currentPage);
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

        TextView wordTvW = findViewById(R.id.wordTvW);
        TextView meaningTvW = findViewById(R.id.meaningTvW);
        synonymsTvW = findViewById(R.id.synonymsTvW);
        antonymsTvW = findViewById(R.id.antonymsTvW);

        wordCloseIV = findViewById(R.id.wordCloseIV);
        meaningCloseIV = findViewById(R.id.meaningCloseIV);

        wordTvW.setOnClickListener(this::wordChange);
        wordCloseIV.setOnClickListener(this::wordChange);
        meaningTvW.setOnClickListener(this::meaningChange);
        meaningCloseIV.setOnClickListener(this::meaningChange);

        assert getLessonType != null;
        if (getLessonType.equals("verb")){
            wordTvW.setText("Verb 1");
            meaningTvW.setText("Meaning");
            synonymsTvW.setText("Verb 2");
            antonymsTvW.setText("Verb 3");
        }else {
            wordTvW.setText("Word");
            meaningTvW.setText("Meaning");
            synonymsTvW.setText("Synonyms");
            antonymsTvW.setText("Antonyms");
        }

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
            interstitialAdId = "" + R.string.INTERSTITIAL_UNIT_ID;
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
        return super.onOptionsItemSelected(item);
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
                        // Logic for visibility of synonymsTvW and antonymsTvW
                        boolean isAnySynonymNotNull = wordList.stream()
                                .anyMatch(word -> word.getSynonyms() != null && !"null".equals(word.getSynonyms()));

                        boolean isAnyAntonymNotNull = wordList.stream()
                                .anyMatch(word -> word.getAntonyms() != null && !"null".equals(word.getAntonyms()));

                        if (isAnySynonymNotNull) {
                            synonymsTvW.setVisibility(View.VISIBLE);
                        } else {
                            synonymsTvW.setVisibility(View.GONE);
                        }

                        if (isAnyAntonymNotNull) {
                            antonymsTvW.setVisibility(View.VISIBLE);
                        } else {
                            antonymsTvW.setVisibility(View.GONE);
                        }
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

    @Override
    public void onDestroy() {
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
}