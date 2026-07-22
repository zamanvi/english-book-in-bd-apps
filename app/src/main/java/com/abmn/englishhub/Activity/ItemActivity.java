package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.ItemAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.InterstitialAdManager;
import com.abmn.englishhub.Model.ItemModel;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ItemActivity extends AppCompatActivity {

    private Activity activity;
    private List<ItemModel> itemList;
    private RecyclerView itemRV;
    private ProgressBar loadingPB;
    private View emptyLL;
    private TextView emptyTV;
    private InterstitialAdManager interstitialAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        define();
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
        itemRV = findViewById(R.id.itemRV);
        loadingPB = findViewById(R.id.loadingPB);
        emptyLL = findViewById(R.id.emptyLL);
        emptyTV = findViewById(R.id.emptyTV);
        itemList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        itemRV.setLayoutManager(linearLayout);

        String slug = getIntent().getStringExtra(Constant.FROM);
        String title = getIntent().getStringExtra(Constant.FROM_TITLE);
        rememberAsLastOpened(slug, title);
        loadingPB.setVisibility(View.VISIBLE);
        getData(slug);

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

    // Lets Home's "Continue Learning" card point at the chapter the user
    // actually opened most recently, instead of always the first chapter.
    private void rememberAsLastOpened(String slug, String title) {
        if (slug == null || slug.isEmpty() || title == null || title.isEmpty()) return;
        activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(Constant.LAST_CHAPTER_SLUG, slug)
                .putString(Constant.LAST_CHAPTER_TITLE, title)
                .apply();
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
        UConfig uConfig = new UConfig(activity);
        String interstitialAdId;
        if (uConfig.getBoolean(Constant.IS_TEST_ADS)){
            interstitialAdId = this.getString(R.string.INTERSTITIAL_UNIT_ID_LOCAL);
        }else {
            interstitialAdId = getString(R.string.INTERSTITIAL_UNIT_ID);
        }
        interstitialAdManager = new InterstitialAdManager(activity, interstitialAdId);
        interstitialAdManager.loadInterstitialAd();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData(String data) {

        String cacheKey = "items_" + data;
        ApiConfig.RequestToVolley((result, response, error) -> {
            if (!result || response == null || response.isEmpty()) {
                showEmpty(true);
                return;
            }
            try {
                JSONObject itemsObject = new JSONObject(response);
                JSONObject dataObject = itemsObject.getJSONObject("items");
                JSONArray itemsArray = dataObject.getJSONArray(Constant.DATA);

                for (int i = 0; i < itemsArray.length(); i++) {
                    try {
                        JSONObject chapter = itemsArray.getJSONObject(i);
                        int id = chapter.getInt("id");
                        String slug = chapter.getString("slug");
                        String chapter_id = chapter.getString("chapter_id");
                        String title = chapter.getString("title");
                        String pageview = chapter.getString("pageview");
                        String book_title = chapter.getString("book_title");

                        ItemModel model = new ItemModel(id, slug, chapter_id, title, pageview, book_title);
                        itemList.add(model);
                    } catch (JSONException e) {
                        android.util.Log.e("ItemActivity", "item parse error", e);
                    }
                }
                ItemAdapter adapter = new ItemAdapter(itemList, activity);
                itemRV.setAdapter(adapter);
                Objects.requireNonNull(itemRV.getAdapter()).notifyDataSetChanged();

                loadingPB.setVisibility(View.GONE);
                if (itemList.isEmpty()) {
                    showEmpty(false);
                }
            } catch (JSONException e) {
                android.util.Log.e("ItemActivity", "getData parse error", e);
                showEmpty(true);
            }
        }, Request.Method.GET, activity, Constant.ITEM_API + "?chapter_slug=" + data, new HashMap<>(), false, cacheKey);
    }

    private void showEmpty(boolean failed) {
        loadingPB.setVisibility(View.GONE);
        emptyTV.setText(failed ? "লোড করা যায়নি" : "কোনো item নেই");
        emptyLL.setVisibility(View.VISIBLE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.homeNav) {
            startActivity(new Intent(activity, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}