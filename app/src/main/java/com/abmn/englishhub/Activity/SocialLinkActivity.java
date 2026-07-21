package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.R;

public class SocialLinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_social_link);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        define();
    }

    private void define() {
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView facebookIV = findViewById(R.id.facebookIV);
        ImageView youtubeIV = findViewById(R.id.youtubeIV);

        facebookIV.setOnClickListener(v -> openInChrome("https://www.facebook.com/bdlearnify/"));
        youtubeIV.setOnClickListener(v -> openInChrome("https://youtu.be/DquLb3cQwCQ?si=oDNVMjMfbXpV0xSB"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
}