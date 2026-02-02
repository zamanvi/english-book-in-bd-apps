package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.abmn.englishhub.Helper.BannerAdManager;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

public class BookActivity extends AppCompatActivity {

    private BannerAdManager bannerAdManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        define();

    }

    private void define() {

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Book");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        UConfig uConfig = new UConfig(this);

        String bannerAdId;
        if (uConfig.getBoolean(Constant.IS_TEST_ADS)){
            bannerAdId = this.getString(R.string.BANNER_UNIT_ID_LOCAL);
        }else {
            bannerAdId = "" + R.string.BANNER_UNIT_ID;
        }
        FrameLayout adContainer = findViewById(R.id.banner_ad_container);
        BannerAdManager.loadBannerAd(this, adContainer, bannerAdId);

        ImageView callIB = findViewById(R.id.callIB);
        ImageView whatsappIB = findViewById(R.id.whatsappIB);
        ImageView facebookIB = findViewById(R.id.facebookIB);
        ImageView youtubeIB = findViewById(R.id.youtubeIB);

        callIB.setOnClickListener(view -> makePhoneCall());
        whatsappIB.setOnClickListener(view -> openWhatsApp());

        facebookIB.setOnClickListener(view -> openInChrome("https://www.facebook.com/share/1HscyY4viN/?mibextid=wwXIfr"));
        youtubeIB.setOnClickListener(view -> openInChrome("https://youtu.be/a2qazWNSiW0?si=Wc_O7rI6rZ2-ckyA"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + "+8801898884425"));
        startActivity(intent);
    }

    private void openWhatsApp() {
        try {
            String url = "https://wa.me/" + "+8801826192179".replace("+", "") + "?text=" + Uri.encode("আমি বইটি নিতে চাচ্ছি।");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (Exception e) {
            // If WhatsApp is not installed, open via browser as fallback
            String url = "https://wa.me/" + "+8801826192179".replace("+", "") + "?text=" + Uri.encode("আমি বইটি নিতে চাচ্ছি।");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }

    private void openInChrome(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            startActivity(intent);
        } catch (Exception e) {
            // If Chrome isn’t installed, open with any available browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
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
}