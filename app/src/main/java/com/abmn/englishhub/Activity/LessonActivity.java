package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import com.abmn.englishhub.Adapter.LessonAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.InterstitialAdManager;
import com.abmn.englishhub.Model.LessonModel;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LessonActivity extends AppCompatActivity {
    private Activity activity;
    private UConfig uConfig;
    private RecyclerView lessonRV;
    private List<LessonModel> lessonList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;
    private String getChapterId;
    private InterstitialAdManager interstitialAdManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lesson);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        define();
        loadInitialData();
    }

    @Override
    public void onDestroy() {
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
    private void define() {

        activity = this;
        uConfig = new UConfig(activity);
        getChapterId = getIntent().getStringExtra(Constant.FROM);
        String getChapterTitle = getIntent().getStringExtra(Constant.FROM_TITLE);
        setToolbar("Vocabulary: " + getChapterTitle);

        lessonRV = findViewById(R.id.lessonRV);

        if (!uConfig.isConnected()){
            uConfig.isConnectedAlert("", "");
        }

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        lessonRV.setLayoutManager(linearLayout);
        lessonRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void loadInitialData() {
        lessonList = new ArrayList<>();
        LessonAdapter adapter = new LessonAdapter(lessonList, activity);
        lessonRV.setAdapter(adapter);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchData(int page) {
        String url = Constant.ROOT_API2 + Constant.LESSONS + "/" + getChapterId + "?page=" + page;
        String tag = "LessonActivity";

        ApiConfig.RequestToVolley((result, response, error) -> {
            try {
                if (result) {
                    JSONObject chaptersObject = new JSONObject(response).getJSONObject(Constant.LESSONS);
                    JSONArray dataArray = chaptersObject.getJSONArray(Constant.DATA);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject lesson = dataArray.getJSONObject(i);
                        int id = lesson.getInt("id");
                        String title = lesson.getString("title");
                        String chapter_id = lesson.getString("chapter_id");
                        boolean status = lesson.getBoolean("status");
                        String created_at = lesson.getString("created_at");
                        String updated_at = lesson.getString("updated_at");

                        LessonModel model = new LessonModel(id, title, "", "", "", chapter_id, status, created_at, updated_at);
                        lessonList.add(model);
                    }
                    if (currentPage == 1) {
                        lastPage = chaptersObject.getInt("last_page");
                    }
                    Objects.requireNonNull(lessonRV.getAdapter()).notifyDataSetChanged();
                }
            } catch (Exception e) {
                // ignored
            } finally {
                isLoading = false;
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true);
    }
}