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
                        "html,body{background:#07081A !important;color:#E8E4FF !important;font-family:'Roboto','Noto Sans Bengali','Noto Sans',sans-serif;font-size:15px;line-height:1.15;padding:10px 14px;margin:0;}" +
                        "*{color:#E8E4FF !important;background-color:transparent !important;box-sizing:border-box;}" +
                        "h1,h2,h3,h4,h5,h6{color:#A78BFA !important;margin-top:14px;margin-bottom:4px;line-height:1.3;}" +
                        "b,strong{color:#FBBF24 !important;}" +
                        "i,em{color:#34D399 !important;}" +
                        "a{color:#60A5FA !important;}" +
                        "p{margin:4px 0;}" +
                        "p:empty{display:none;}" +
                        "br{line-height:1.2;}" +
                        "table{width:100% !important;border-collapse:collapse !important;margin:10px 0;font-size:14px;}" +
                        "th{background:#191B40 !important;color:#A78BFA !important;padding:7px;border:1px solid #2E3060 !important;}" +
                        "td{background:#0C0E26 !important;color:#E8E4FF !important;padding:7px;border:1px solid #2E3060 !important;}" +
                        "ul,ol{padding-left:18px;margin:4px 0;}" +
                        "li{margin:3px 0;}" +
                        "code,pre{background:#191B40 !important;color:#34D399 !important;padding:2px 5px;border-radius:4px;font-size:13px;}" +
                        "span,div,font,label{color:inherit !important;background:transparent !important;}" +
                        "img{max-width:100% !important;height:auto !important;}" +
                        "</style>";

                    String metaViewport = "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>";
                    String htmlData = "<!DOCTYPE html><html><head>" + metaViewport + darkCss + "</head><body>" + details + "</body></html>";
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

