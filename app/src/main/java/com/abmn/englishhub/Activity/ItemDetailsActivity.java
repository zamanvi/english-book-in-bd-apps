package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.InterstitialAdManager;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.android.volley.Request;

import org.json.JSONObject;

import java.util.HashMap;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView titleTV;
    private WebView detailsWV;
    private Activity activity;
    private InterstitialAdManager interstitialAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);
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

        titleTV = findViewById(R.id.titleTV);
        detailsWV = findViewById(R.id.detailsWV);

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

    @SuppressLint("SetJavaScriptEnabled")
    private void getData(String slug) {

        WebSettings webSettings = detailsWV.getSettings();

        ApiConfig.RequestToVolley((result, response, error) -> {
            if (result){
                try {
                    JSONObject item = new JSONObject(response).getJSONObject("item");
                    String title = item.getString("title");
                    String details = item.getString("details");
                    titleTV.setText(title);
                    detailsWV.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    detailsWV.setBackgroundColor(android.graphics.Color.parseColor("#07081A"));

                    String darkCss = "<style>" +
                        "body{background:#07081A;color:#F0EEFF;font-family:'Segoe UI',sans-serif;font-size:15px;line-height:1.7;padding:12px 16px;}" +
                        "h1,h2,h3,h4{color:#A78BFA;margin-top:18px;}" +
                        "b,strong{color:#FBBF24;}" +
                        "i,em{color:#34D399;}" +
                        "a{color:#60A5FA;}" +
                        "p{margin:8px 0;}" +
                        "table{width:100%;border-collapse:collapse;margin:12px 0;}" +
                        "th{background:#191B40;color:#A78BFA;padding:8px;border:1px solid #2E3060;}" +
                        "td{background:#0C0E26;color:#F0EEFF;padding:8px;border:1px solid #2E3060;}" +
                        "ul,ol{padding-left:20px;color:#F0EEFF;}" +
                        "li{margin:4px 0;}" +
                        "code,pre{background:#191B40;color:#34D399;padding:2px 6px;border-radius:4px;}" +
                        "</style>";

                    String htmlData = "<!DOCTYPE html><html><head>" + darkCss + "</head><body>" + details + "</body></html>";
                    webSettings.setJavaScriptEnabled(true);
                    detailsWV.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
                } catch (Exception e) {
                    android.util.Log.e("ItemDetailsActivity", "parse error", e);
                }
            }else {
                onSupportNavigateUp();
            }
        }, Request.Method.GET, activity, Constant.ITEM_SHOW_API + slug, new HashMap<>(), true);
    }
}

