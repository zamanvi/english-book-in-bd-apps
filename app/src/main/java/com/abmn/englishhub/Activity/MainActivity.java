package com.abmn.englishhub.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.abmn.englishhub.databinding.ActivityMainBinding;
import com.abmn.utility.UConfig;
import com.android.volley.Request;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Activity activity;
    private UConfig uConfig;
    private JSONArray bookArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        define();
    }

    private void define() {
        activity = this;
        uConfig = new UConfig(activity);

        CardView cardView = findViewById(R.id.bookCV);
        cardView.setOnClickListener(view -> work());

        getBookData();
        work();
    }

    private void work() {
        if (bookArray != null && bookArray.length() > 0) {
            try {
                JSONObject firstBook = bookArray.getJSONObject(0);
                String slug = firstBook.getString("slug");
                startActivity(new Intent(activity, ChapterActivity.class).putExtra(Constant.FROM, slug));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getBookData() {

        ApiConfig.RequestToVolley((result, response, error) -> {
            Log.d("response", response);
            try {
                JSONObject bookObject = new JSONObject(response);
                JSONObject books = bookObject.getJSONObject("books");
                bookArray = books.getJSONArray(Constant.DATA);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Request.Method.GET, activity, Constant.BOOK_API, new HashMap<>(), false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.chapterNav) {
            work();
        }
        return super.onOptionsItemSelected(item);
    }
}