package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
    private ProgressBar loadingPB;
    private View emptyLL;
    private TextView emptyTV;
    private List<WordChapterModel> chapterList;
    // Everything fetched so far; chapterList (what the adapter shows) is the
    // slice of it that matches the selected exam filter.
    private final List<WordChapterModel> allChapters = new ArrayList<>();
    private LinearLayout goalChipsLL;
    private View goalChipsScroll;
    private String selectedGoal = "";
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;
    private String categoryType;
    private boolean quizPickerMode;
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
        quizPickerMode = getIntent().getBooleanExtra(Constant.QUIZ_PICKER_MODE, false);

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (categoryTitle != null) {
                getSupportActionBar().setTitle(categoryTitle);
            }
        }
        chapterRV = findViewById(R.id.chapterRV);
        loadingPB = findViewById(R.id.loadingPB);
        emptyLL = findViewById(R.id.emptyLL);
        emptyTV = findViewById(R.id.emptyTV);
        goalChipsLL = findViewById(R.id.goalChipsLL);
        goalChipsScroll = findViewById(R.id.goalChipsScroll);
        selectedGoal = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString(Constant.EXAM_GOAL, "");

        // No unconditional "no internet" check here - fetchData() below goes
        // through ApiConfig, which serves cached content silently when
        // offline and only alerts if there's truly nothing cached to show.

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
        WordChapterAdapter adapter = new WordChapterAdapter(chapterList, activity, quizPickerMode);
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
        String cacheKey = "vocab_chapters_" + (categoryType != null && !categoryType.isEmpty() ? categoryType : "all") + "_p" + page;

        if (page == 1) {
            loadingPB.setVisibility(View.VISIBLE);
            emptyLL.setVisibility(View.GONE);
        }

        ApiConfig.RequestToVolley((result, response, error) -> {
            boolean failed = false;
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
                        // optString would return the text "null" for a JSON null
                        model.setExamTags(chapter.isNull("exam_tags") ? "" : chapter.optString("exam_tags", ""));
                        allChapters.add(model);
                    }
                    if (currentPage == 1) {
                        lastPage = chaptersObject.getInt("last_page");
                    }
                    applyGoalFilter();
                } else {
                    failed = true;
                }
            } catch (Exception e) {
                failed = true;
            } finally {
                isLoading = false;
            }

            // A category with exactly one chapter (today: Verb) made the player
            // tap through a one-row list just to reach the lessons. Skip it
            // - data driven, so the list comes back by itself the moment a
            // second chapter is added.
            if (page == 1 && !failed && allChapters.size() == 1 && lastPage <= 1
                    && !isFinishing()) {
                WordChapterModel only = allChapters.get(0);
                startActivity(new Intent(activity, LessonActivity.class)
                        .putExtra(Constant.FROM, "" + only.getId())
                        .putExtra(Constant.FROM_TITLE, only.getTitle())
                        .putExtra(Constant.FROM_TYPE, only.getType())
                        .putExtra(Constant.QUIZ_PICKER_MODE, quizPickerMode));
                finish();
                return;
            }

            if (page == 1) {
                loadingPB.setVisibility(View.GONE);
                if (allChapters.isEmpty()) {
                    emptyTV.setText(failed ? "চ্যাপ্টার লোড করা যায়নি" : "কোনো চ্যাপ্টার নেই");
                    emptyLL.setVisibility(View.VISIBLE);
                }
            }

            // The exam chips need every chapter's tags, and a filtered list can
            // be too short to ever trigger the scroll-to-load-more listener, so
            // pull the remaining pages straight away (2 requests today).
            if (!failed && page == currentPage && currentPage < lastPage && !isFinishing()) {
                loadMoreData();
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true, cacheKey);
    }

    // ── "My exam" filter ─────────────────────────────────────────────

    private static final String[] GOAL_ORDER = {"hsc", "ssc", "jsc", "bcs", "bank", "medical", "admission_job"};

    private static String goalLabel(String tag) {
        switch (tag) {
            case "hsc": return "HSC";
            case "ssc": return "SSC";
            case "jsc": return "JSC";
            case "bcs": return "BCS";
            case "bank": return "Bank";
            case "medical": return "Medical";
            case "admission_job": return "Admission & Job";
            default: return tag.toUpperCase(java.util.Locale.ROOT);
        }
    }

    private static boolean matchesGoal(WordChapterModel c, String goal) {
        String tags = c.getExamTags();
        if (goal.isEmpty() || tags.isEmpty()) return true; // untagged = for everybody
        for (String t : tags.split(",")) {
            if (t.trim().equals(goal)) return true;
        }
        return false;
    }

    // Rebuilds what the list shows from allChapters + the selected exam, and
    // refreshes the chip row. Never leaves the list empty because of a filter.
    @SuppressLint("NotifyDataSetChanged")
    private void applyGoalFilter() {
        java.util.TreeSet<String> present = new java.util.TreeSet<>();
        for (WordChapterModel c : allChapters) {
            if (c.getExamTags().isEmpty()) continue;
            for (String t : c.getExamTags().split(",")) {
                if (!t.trim().isEmpty()) present.add(t.trim());
            }
        }
        // A saved goal that no chapter carries any more must not hide things.
        if (!selectedGoal.isEmpty() && !present.contains(selectedGoal) && currentPage >= lastPage) {
            selectedGoal = "";
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit().remove(Constant.EXAM_GOAL).apply();
        }

        chapterList.clear();
        for (WordChapterModel c : allChapters) {
            if (matchesGoal(c, selectedGoal)) chapterList.add(c);
        }
        if (chapterList.isEmpty() && !allChapters.isEmpty()) {
            chapterList.addAll(allChapters);
        }
        if (chapterRV.getAdapter() != null) chapterRV.getAdapter().notifyDataSetChanged();
        buildGoalChips(present);
    }

    private void buildGoalChips(java.util.Set<String> present) {
        goalChipsLL.removeAllViews();
        // Only worth showing when at least one chapter is tagged.
        if (present.isEmpty()) {
            goalChipsScroll.setVisibility(View.GONE);
            return;
        }
        goalChipsScroll.setVisibility(View.VISIBLE);

        java.util.List<String> ordered = new ArrayList<>();
        for (String t : GOAL_ORDER) if (present.contains(t)) ordered.add(t);
        for (String t : present) if (!ordered.contains(t)) ordered.add(t);

        addGoalChip("", "\u09b8\u09ac");
        for (String t : ordered) addGoalChip(t, goalLabel(t));
    }

    private void addGoalChip(String tag, String label) {
        TextView chip = new TextView(this);
        boolean selected = tag.equals(selectedGoal);
        chip.setText(label);
        chip.setTextSize(13);
        chip.setTextColor(androidx.core.content.ContextCompat.getColor(this,
                selected ? R.color.text_primary : R.color.text_secondary));
        chip.setBackgroundResource(selected ? R.drawable.bg_indigo_pill : R.drawable.bg_pill_inactive);
        float d = getResources().getDisplayMetrics().density;
        chip.setPadding((int) (14 * d), (int) (7 * d), (int) (14 * d), (int) (7 * d));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd((int) (8 * d));
        chip.setLayoutParams(lp);
        chip.setOnClickListener(v -> {
            if (tag.equals(selectedGoal)) return;
            selectedGoal = tag;
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                    .putString(Constant.EXAM_GOAL, tag).apply();
            applyGoalFilter();
            chapterRV.scrollToPosition(0);
        });
        goalChipsLL.addView(chip);
    }

    @Override
    public void onDestroy() {
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }
}
