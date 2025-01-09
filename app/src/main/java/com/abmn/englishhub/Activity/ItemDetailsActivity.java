package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.android.volley.Request;

import org.json.JSONObject;

import java.util.HashMap;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView titleTV;
    private WebView detailsWV;
    private Activity activity;

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

    private void define() {

        activity = this;

        titleTV = findViewById(R.id.titleTV);
        detailsWV = findViewById(R.id.detailsWV);

        String slug = getIntent().getStringExtra(Constant.FROM);
        getData(slug);

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void getData(String slug) {

        WebSettings webSettings = detailsWV.getSettings();

        ApiConfig.RequestToVolley((result, response, error) -> {
            try {
                JSONObject item = new JSONObject(response).getJSONObject("item");
                String title = item.getString("title");
                String details = item.getString("details");
                titleTV.setText(title);
                detailsWV.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

                String htmlData = "<!DOCTYPE html><html><head></head><body>" + details + "</body></html>";
                webSettings.setJavaScriptEnabled(true);
                detailsWV.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Request.Method.GET, activity, Constant.ITEM_SHOW_API + slug, new HashMap<>(), true);
    }
}

