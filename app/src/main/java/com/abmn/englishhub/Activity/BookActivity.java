package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.abmn.englishhub.R;

public class BookActivity extends AppCompatActivity {

    private CardView bookCoverCV01, bookCoverCV02;
    private ImageView bookCover01, bookCover02;
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
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        }

        ImageView callIB = findViewById(R.id.callIB);
        ImageView whatsappIB = findViewById(R.id.whatsappIB);
        ImageView facebookIB = findViewById(R.id.facebookIB);
        ImageView youtubeIB = findViewById(R.id.youtubeIB);
        bookCover01 = findViewById(R.id.bookCover01);
        bookCover02 = findViewById(R.id.bookCover02);

        bookCoverCV01 = findViewById(R.id.bookCoverCV01);
        bookCoverCV02 = findViewById(R.id.bookCoverCV02);

        // 🔹 Default selection
        setSelected(bookCoverCV01, bookCover01, true);
        setSelected(bookCoverCV02, bookCover02, false);

        bookCoverCV01.setOnClickListener(v -> selectBookCover(true));
        bookCoverCV02.setOnClickListener(v -> selectBookCover(false));

        callIB.setOnClickListener(view -> makePhoneCall());
        whatsappIB.setOnClickListener(view -> openWhatsApp());

        facebookIB.setOnClickListener(view -> openInChrome("https://www.facebook.com/share/1HscyY4viN/?mibextid=wwXIfr"));
        youtubeIB.setOnClickListener(view -> openInChrome("https://youtu.be/a2qazWNSiW0?si=Wc_O7rI6rZ2-ckyA"));
    }

    private void selectBookCover(boolean firstSelected) {
        if (firstSelected) {
            setSelected(bookCoverCV01, bookCover01, true);
            setSelected(bookCoverCV02, bookCover02, false);
        } else {
            setSelected(bookCoverCV01, bookCover01, false);
            setSelected(bookCoverCV02, bookCover02, true);
        }
    }

    private void setSelected(CardView cardView, ImageView imageView, boolean isSelected) {
        if (isSelected) {
            imageView.setVisibility(View.VISIBLE);
            cardView.setCardBackgroundColor(getColor(R.color.selected_bg));
            cardView.setClickable(false);
        } else {
            imageView.setVisibility(View.GONE);
            cardView.setCardBackgroundColor(getColor(R.color.unselected_bg));
            cardView.setClickable(true);
        }
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