package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private ProgressBar loadingPB;
    private View emptyLL;
    private TextView emptyTV;
    private List<LessonModel> lessonList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;
    private String getChapterId;
    private String getChapterType;
    private boolean quizPickerMode;
    private InterstitialAdManager interstitialAdManager;
    // Safety net: on a slow/congested backend response, don't leave the
    // spinner spinning forever - show the retry state after a bounded wait.
    private final Handler loadTimeoutHandler = new Handler(Looper.getMainLooper());
    private static final long LOAD_TIMEOUT_MS = 12000;
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
        loadTimeoutHandler.removeCallbacksAndMessages(null);
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
    private void define() {

        activity = this;
        uConfig = new UConfig(activity);
        getChapterId = getIntent().getStringExtra(Constant.FROM);
        getChapterType = getIntent().getStringExtra(Constant.FROM_TYPE);
        if (getChapterType == null) getChapterType = "vocabulary";
        quizPickerMode = getIntent().getBooleanExtra(Constant.QUIZ_PICKER_MODE, false);
        String getChapterTitle = getIntent().getStringExtra(Constant.FROM_TITLE);
        setToolbar(getChapterTitle);

        lessonRV = findViewById(R.id.lessonRV);
        loadingPB = findViewById(R.id.loadingPB);
        emptyLL = findViewById(R.id.emptyLL);
        emptyTV = findViewById(R.id.emptyTV);

        // No unconditional "no internet" check here - fetchData() below goes
        // through ApiConfig, which serves cached content silently when
        // offline and only alerts if there's truly nothing cached to show.

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
            interstitialAdId = getString(R.string.INTERSTITIAL_UNIT_ID);
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

    private void loadInitialData() {
        lessonList = new ArrayList<>();
        LessonAdapter adapter = new LessonAdapter(lessonList, activity, getChapterType, quizPickerMode);
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
        String cacheKey = "vocab_lessons_" + getChapterId + "_p" + page;

        if (page == 1) {
            loadingPB.setVisibility(View.VISIBLE);
            emptyLL.setVisibility(View.GONE);
            loadTimeoutHandler.removeCallbacksAndMessages(null);
            loadTimeoutHandler.postDelayed(() -> {
                if (loadingPB.getVisibility() == View.VISIBLE) {
                    loadingPB.setVisibility(View.GONE);
                    emptyTV.setText("লোড করতে বেশি সময় লাগছে, আবার চেষ্টা করুন");
                    emptyLL.setVisibility(View.VISIBLE);
                }
            }, LOAD_TIMEOUT_MS);
        }

        ApiConfig.RequestToVolley((result, response, error) -> {
            loadTimeoutHandler.removeCallbacksAndMessages(null);
            boolean failed = false;
            try {
                if (result) {
                    JSONObject chaptersObject = new JSONObject(response).getJSONObject(Constant.LESSONS);
                    JSONArray dataArray = chaptersObject.getJSONArray(Constant.DATA);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject lesson = dataArray.getJSONObject(i);
                        int id = lesson.getInt("id");
                        String title = lesson.getString("title");
                        String type = lesson.optString("type", "vocabulary");
                        String chapter_id = lesson.getString("chapter_id");
                        boolean status = lesson.getBoolean("status");
                        String created_at = lesson.getString("created_at");
                        String updated_at = lesson.getString("updated_at");
                        boolean isPremium = lesson.optBoolean("is_premium", false);

                        LessonModel model = new LessonModel(id, title, "", "", type, chapter_id, status, created_at, updated_at, isPremium);
                        lessonList.add(model);
                    }
                    if (currentPage == 1) {
                        lastPage = chaptersObject.getInt("last_page");
                    }
                    Objects.requireNonNull(lessonRV.getAdapter()).notifyDataSetChanged();
                } else {
                    failed = true;
                }
            } catch (Exception e) {
                failed = true;
            } finally {
                isLoading = false;
            }

            if (page == 1) {
                loadingPB.setVisibility(View.GONE);
                if (lessonList.isEmpty()) {
                    emptyTV.setText(failed ? "লেসন লোড করা যায়নি" : "কোনো লেসন নেই");
                    emptyLL.setVisibility(View.VISIBLE);
                }
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true, cacheKey);
    }
}