package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class VocabularyActivity extends AppCompatActivity {
    private Activity activity;
    private UConfig uConfig;
    private RecyclerView chapterRV;
    private List<WordChapterModel> chapterList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;
    private InterstitialAdManager interstitialAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);

        define();
        loadInitialData();
    }

    private void define() {
        activity = this;
        uConfig = new UConfig(activity);
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Vocabulary");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        finish();
        return true;
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
        String tag = "VocabularyActivity";
        ApiConfig.RequestToVolley((result, response, error) -> {
            Log.d("response", response);
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
                Log.d(tag + "_exception", Objects.requireNonNull(e.getMessage()));
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