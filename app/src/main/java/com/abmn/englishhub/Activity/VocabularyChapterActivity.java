package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.WordChapterAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.InterstitialAdManager;
import com.abmn.englishhub.Model.WordChapterModel;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class VocabularyChapterActivity extends AppCompatActivity {
    private Activity activity;
    private UConfig uConfig;
    private RecyclerView chapterRV;
    private List<WordChapterModel> chapterList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;
    private String categoryType;
    private InterstitialAdManager interstitialAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vocabulary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        define();
        loadInitialData();
    }

    private void define() {
        activity = this;
        uConfig = new UConfig(activity);
        categoryType = getIntent().getStringExtra(Constant.FROM_TYPE);
        String categoryTitle = getIntent().getStringExtra(Constant.FROM_TITLE);

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (categoryTitle != null) {
                getSupportActionBar().setTitle(categoryTitle);
            }
        }
        chapterRV = findViewById(R.id.chapterRV);

        if (!uConfig.isConnected()){
            uConfig.isConnectedAlert("", "");
        }

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        chapterRV.setLayoutManager(linearLayout);
        chapterRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadInitialData() {
        chapterList = new ArrayList<>();
        WordChapterAdapter adapter = new WordChapterAdapter(chapterList, activity);
        chapterRV.setAdapter(adapter);
        fetchData(currentPage);
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
        String url = Constant.ROOT_API2 + Constant.CHAPTERS + "?page=" + page;
        if (categoryType != null && !categoryType.isEmpty()) {
            url += "&type=" + categoryType;
        }
        String tag = "VocabularyChapterActivity";
        ApiConfig.RequestToVolley((result, response, error) -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (result) {
                    JSONObject chaptersObject = jsonObject.getJSONObject(Constant.CHAPTERS);
                    JSONArray dataArray = chaptersObject.getJSONArray(Constant.DATA);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject chapter = dataArray.getJSONObject(i);
                        int id = chapter.getInt("id");
                        String title = chapter.getString("title");
                        String type = chapter.getString("type");
                        String image_path = chapter.getString("image_path");
                        boolean status = chapter.getBoolean("status");
                        String created_at = chapter.getString("created_at");
                        String updated_at = chapter.getString("updated_at");

                        WordChapterModel model = new WordChapterModel(id, title, type, image_path, status, created_at, updated_at);
                        chapterList.add(model);
                    }
                    if (currentPage == 1) {
                        lastPage = chaptersObject.getInt("last_page");
                    }
                    Objects.requireNonNull(chapterRV.getAdapter()).notifyDataSetChanged();
                }
            } catch (Exception e) {
                // ignored
            } finally {
                isLoading = false;
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true);
    }

    @Override
    public void onDestroy() {
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
}
