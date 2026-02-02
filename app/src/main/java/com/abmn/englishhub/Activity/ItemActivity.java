package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;

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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void callAds() {
        UConfig uConfig = new UConfig(activity);
        String interstitialAdId;
        if (uConfig.getBoolean(Constant.IS_TEST_ADS)){
            interstitialAdId = this.getString(R.string.INTERSTITIAL_UNIT_ID_LOCAL);
        }else {
            interstitialAdId = "" + R.string.INTERSTITIAL_UNIT_ID;
        }
        interstitialAdManager = new InterstitialAdManager(activity, interstitialAdId);
        interstitialAdManager.loadInterstitialAd();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData(String data) {

        ApiConfig.RequestToVolley((result, response, error) -> {
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
                        throw new RuntimeException(e);
                    }
                }
                ItemAdapter adapter = new ItemAdapter(itemList, activity);
                itemRV.setAdapter(adapter);
                Objects.requireNonNull(itemRV.getAdapter()).notifyDataSetChanged();

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, Request.Method.GET, activity, Constant.ITEM_API + "?chapter_slug=" + data, new HashMap<>(), false);
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