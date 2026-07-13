package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
    private CardView titleCV;
    private WebView detailsWV;
    private ProgressBar loadingPB;
    private Activity activity;
    private InterstitialAdManager interstitialAdManager;
    private String currentDetailsHtml;

    // ── Reading-to-Lipto reward ────────────────────────────────────
    // Active-reading time only accumulates while the user has interacted
    // (touched/scrolled) within the last IDLE_PAUSE_MS - otherwise it pauses,
    // so a screen left open in the background doesn't quietly rack up Lipto.
    private final Handler readingTimerHandler = new Handler(Looper.getMainLooper());
    private long lastInteractionMs = System.currentTimeMillis();
    private long activeReadingMs = 0;
    private static final long IDLE_PAUSE_MS = 3 * 60 * 1000;
    private static final long MIN_ACTIVE_MS_TO_EARN = 30 * 1000;
    private static final long MS_PER_LIPTO = 60 * 1000;

    private final Runnable readingTicker = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() - lastInteractionMs <= IDLE_PAUSE_MS) {
                activeReadingMs += 1000;
            }
            readingTimerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        lastInteractionMs = System.currentTimeMillis();
        return super.dispatchTouchEvent(ev);
    }

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
    protected void onResume() {
        super.onResume();
        lastInteractionMs = System.currentTimeMillis();
        readingTimerHandler.removeCallbacks(readingTicker);
        readingTimerHandler.postDelayed(readingTicker, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        readingTimerHandler.removeCallbacks(readingTicker);
        claimReadingReward();
    }

    @Override
    public void onDestroy() {
        readingTimerHandler.removeCallbacksAndMessages(null);
        if (interstitialAdManager != null) {
            interstitialAdManager = null;
        }
        super.onDestroy();
    }

    // Claims whatever active-reading time has accumulated since the last
    // claim, then resets the counter - safe to call multiple times across
    // app-switches within one continuous reading session (e.g. onPause fires
    // on a brief backgrounding, then onResume keeps accumulating more).
    private void claimReadingReward() {
        if (activeReadingMs < MIN_ACTIVE_MS_TO_EARN) return;
        int lipto = (int) (activeReadingMs / MS_PER_LIPTO);
        if (lipto <= 0) return;

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        activeReadingMs = 0;

        JSONObject body = new JSONObject();
        try {
            body.put("amount", lipto);
            body.put("source", "reading");
            body.put("description", "পড়ে Lipto অর্জন");
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GAME_LIPTO_EARN, body, token,
                response -> { /* silent - low-key reward, no interruption while reading */ },
                error -> { /* silent - small session, not worth a retry mechanism */ });
    }

    private void define() {

        activity = this;

        titleTV = findViewById(R.id.titleTV);
        titleCV = findViewById(R.id.titleCV);
        detailsWV = findViewById(R.id.detailsWV);
        loadingPB = findViewById(R.id.loadingPB);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.themeToggleId) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isStorybookTheme() {
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return Constant.CONTENT_THEME_STORYBOOK.equals(
                prefs.getString(Constant.CONTENT_THEME, Constant.CONTENT_THEME_STORYBOOK));
    }

    private void toggleTheme() {
        boolean next = !isStorybookTheme();
        activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString(Constant.CONTENT_THEME, next ? Constant.CONTENT_THEME_STORYBOOK : Constant.CONTENT_THEME_APP)
                .apply();
        if (currentDetailsHtml != null) {
            renderContent(currentDetailsHtml);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void getData(String slug) {

        ApiConfig.RequestToVolley((result, response, error) -> {
            loadingPB.setVisibility(View.GONE);
            if (result){
                try {
                    JSONObject item = new JSONObject(response).getJSONObject("item");
                    String title = item.getString("title");
                    String details = item.getString("details");
                    titleTV.setText(title);
                    currentDetailsHtml = details;
                    renderContent(details);
                } catch (Exception e) {
                    android.util.Log.e("ItemDetailsActivity", "parse error", e);
                }
            }else {
                onSupportNavigateUp();
            }
        }, Request.Method.GET, activity, Constant.ITEM_SHOW_API + slug, new HashMap<>(), true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void renderContent(String details) {
        WebSettings webSettings = detailsWV.getSettings();
        boolean storybook = isStorybookTheme();

        detailsWV.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        detailsWV.setBackgroundColor(android.graphics.Color.parseColor(storybook ? "#F7F1E1" : "#07081A"));

        titleCV.setCardBackgroundColor(storybook
                ? android.graphics.Color.parseColor("#EFE6CC")
                : androidx.core.content.ContextCompat.getColor(this, R.color.indigo_dim));
        titleTV.setTextColor(storybook
                ? android.graphics.Color.parseColor("#7A4A00")
                : androidx.core.content.ContextCompat.getColor(this, R.color.gold));

        String css = storybook ? storybookCss() : darkCss();
        String metaViewport = "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0'>";
        String htmlData = "<!DOCTYPE html><html><head>" + metaViewport + css + "</head><body>" + details + "</body></html>";
        webSettings.setJavaScriptEnabled(true);
        detailsWV.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
    }

    private String storybookCss() {
        return "<style>" +
                "html,body{" +
                "  background:#F7F1E1 !important;" +
                "  color:#241C10 !important;" +
                "  font-family:Georgia,'Iowan Old Style','Noto Sans Bengali','Noto Sans',serif;" +
                "  font-size:15.5px;" +
                "  line-height:1.7;" +
                "  padding:14px 16px 32px;" +
                "  margin:0;" +
                "  word-break:break-word;" +
                "}" +
                "*{box-sizing:border-box;}" +
                "font{font-size:15.5px !important;font-family:inherit !important;color:inherit !important;}" +
                "p,li,td,th,span,div,label,code,pre,blockquote{color:#241C10 !important;background:transparent !important;}" +
                "a{color:#6D3FC0 !important;}" +
                "h1{color:#7A4A00 !important;background:transparent !important;font-size:18px !important;font-weight:700;font-style:italic;margin:20px 0 6px;line-height:1.35;padding:0;}" +
                "h2{color:#7A4A00 !important;background:transparent !important;font-size:16px !important;font-weight:700;margin:18px 0 5px;line-height:1.35;padding:0 0 4px;border-bottom:1px solid #E1D6B8;}" +
                "h3{color:#7A4A00 !important;background:transparent !important;font-size:15px !important;font-weight:700;margin:16px 0 4px;line-height:1.35;padding:0 0 3px;border-bottom:1px solid #E9E0C8;}" +
                "h4,h5,h6{color:#8B5E00 !important;background:transparent !important;font-size:15.5px !important;font-weight:700;margin:14px 0 4px;line-height:1.35;padding:0;}" +
                "b,strong{color:#8B5E00 !important;}" +
                "i,em{color:#5B3D8F !important;font-style:italic;}" +
                "p{margin:0 0 8px;padding:0;}" +
                "p:empty{display:none;}" +
                ":first-child{margin-top:0;}" +
                "br{content:'';display:block;margin:1px 0;}" +
                "ul,ol{padding-left:16px;margin:4px 0 8px;}" +
                "li{margin:4px 0;line-height:1.6;color:#241C10 !important;}" +
                "blockquote{border-left:3px solid #6D3FC0;background:#EFE6CC !important;margin:10px 0;padding:8px 12px;border-radius:0 6px 6px 0;color:#3A2E1A !important;}" +
                "table{width:100% !important;border-collapse:collapse !important;margin:10px 0;font-size:14px;}" +
                "th{background:#EFE6CC !important;color:#7A4A00 !important;padding:8px;border:1px solid #E1D6B8 !important;text-align:left;}" +
                "td{background:#F7F1E1 !important;color:#241C10 !important;padding:8px;border:1px solid #E1D6B8 !important;}" +
                "code,pre{background:#EFE6CC !important;color:#6D3FC0 !important;padding:2px 6px;border-radius:4px;font-size:13px;}" +
                "img{max-width:100% !important;height:auto !important;border-radius:6px;margin:6px 0;display:block;}" +
                "</style>";
    }

    private String darkCss() {
        return "<style>" +
                        /* ── Base ─────────────────────────────────────────── */
                        "html,body{" +
                        "  background:#07081A !important;" +
                        "  color:#E0DCFF !important;" +
                        "  font-family:'Roboto','Noto Sans Bengali','Noto Sans',sans-serif;" +
                        "  font-size:15.5px;" +
                        "  line-height:1.65;" +
                        "  padding:14px 16px 32px;" +
                        "  margin:0;" +
                        "  word-break:break-word;" +
                        "}" +
                        "*{box-sizing:border-box;}" +
                        /* ── Reset inline editor noise ────────────────────── */
                        "font{" +
                        "  font-size:15.5px !important;" +
                        "  font-family:inherit !important;" +
                        "  color:inherit !important;" +
                        "}" +
                        /* ── Default element colors ───────────────────────── */
                        "p,li,td,th,span,div,label,code,pre,blockquote{" +
                        "  color:#E0DCFF !important;" +
                        "  background:transparent !important;" +
                        "}" +
                        "a{color:#60A5FA !important;}" +
                        /* ── Headings — clear visual hierarchy ────────────── */
                        "h1{" +
                        "  color:#A78BFA !important;" +
                        "  background:transparent !important;" +
                        "  font-size:18px !important;" +
                        "  font-weight:700;" +
                        "  margin:20px 0 6px;" +
                        "  line-height:1.35;" +
                        "  padding:0;" +
                        "}" +
                        "h2{" +
                        "  color:#A78BFA !important;" +
                        "  background:transparent !important;" +
                        "  font-size:16px !important;" +
                        "  font-weight:700;" +
                        "  margin:18px 0 5px;" +
                        "  line-height:1.35;" +
                        "  padding:0 0 4px;" +
                        "  border-bottom:1px solid #1E1F4A;" +
                        "}" +
                        "h3{" +
                        "  color:#A78BFA !important;" +
                        "  background:transparent !important;" +
                        "  font-size:15px !important;" +
                        "  font-weight:700;" +
                        "  margin:16px 0 4px;" +
                        "  line-height:1.35;" +
                        "  padding:0 0 3px;" +
                        "  border-bottom:1px solid #1A1C40;" +
                        "}" +
                        "h4,h5,h6{" +
                        "  color:#FBBF24 !important;" +
                        "  background:transparent !important;" +
                        "  font-size:15.5px !important;" +
                        "  font-weight:700;" +
                        "  margin:14px 0 4px;" +
                        "  line-height:1.35;" +
                        "  padding:0;" +
                        "}" +
                        /* ── Highlights ───────────────────────────────────── */
                        "b,strong{color:#FBBF24 !important;}" +
                        "i,em{color:#8BE8C0 !important;font-style:italic;}" +
                        /* ── Paragraphs & whitespace control ─────────────── */
                        "p{margin:0 0 8px;padding:0;}" +
                        "p:empty{display:none;}" +
                        ":first-child{margin-top:0;}" +
                        "br{content:'';display:block;margin:1px 0;}" +
                        /* ── Lists ────────────────────────────────────────── */
                        "ul,ol{padding-left:16px;margin:4px 0 8px;}" +
                        "li{margin:4px 0;line-height:1.6;color:#E0DCFF !important;}" +
                        /* ── Example / blockquote blocks ─────────────────── */
                        "blockquote{" +
                        "  border-left:3px solid #A78BFA;" +
                        "  background:#0E1030 !important;" +
                        "  margin:10px 0;" +
                        "  padding:8px 12px;" +
                        "  border-radius:0 6px 6px 0;" +
                        "  color:#C4BFEF !important;" +
                        "}" +
                        /* ── Tables ───────────────────────────────────────── */
                        "table{width:100% !important;border-collapse:collapse !important;margin:10px 0;font-size:14px;}" +
                        "th{background:#191B40 !important;color:#A78BFA !important;padding:8px;border:1px solid #2E3060 !important;text-align:left;}" +
                        "td{background:#0C0E26 !important;color:#E0DCFF !important;padding:8px;border:1px solid #2E3060 !important;}" +
                        /* ── Code ─────────────────────────────────────────── */
                        "code,pre{background:#12143A !important;color:#34D399 !important;padding:2px 6px;border-radius:4px;font-size:13px;}" +
                        /* ── Images ───────────────────────────────────────── */
                        "img{max-width:100% !important;height:auto !important;border-radius:6px;margin:6px 0;display:block;}" +
                        "</style>";
    }
}

