package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.ChapterAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.ChapterModel;
import com.abmn.englishhub.R;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChapterActivity extends AppCompatActivity {

    private Activity activity;
    private List<ChapterModel> chapterList;
    private RecyclerView chapterRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        define();
    }

    private void define() {
        activity = this;

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        chapterRV = findViewById(R.id.chapterRV);
        chapterList = new ArrayList<>();


        getBookData();

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        chapterRV.setLayoutManager(linearLayout);
    }


    private void getBookData() {

        ApiConfig.RequestToVolley((result, response, error) -> {
            Log.d("response", response);
            try {
                JSONObject bookObject = new JSONObject(response);
                JSONObject books = bookObject.getJSONObject("books");
                JSONArray bookArray = books.getJSONArray(Constant.DATA);
                JSONObject firstBook = bookArray.getJSONObject(0);
                String slug = firstBook.getString("slug");
                getData(slug);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Request.Method.GET, activity, Constant.BOOK_API, new HashMap<>(), false);

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData(String data) {
        ApiConfig.RequestToVolley((chapterResult, chapterResponse, chapterError) -> {
            Log.d("chapterResponse", chapterResponse);
            try {
                JSONObject jsonObject = new JSONObject(chapterResponse);
                JSONObject chapters = jsonObject.getJSONObject("chapters");
                JSONArray chapterArray = chapters.getJSONArray(Constant.DATA);

                for (int i = 0; i < chapterArray.length(); i++) {
                    try {
                        JSONObject chapter = chapterArray.getJSONObject(i);
                        int id = chapter.getInt("id");
                        String book_id = chapter.getString("book_id");
                        String title = chapter.getString("title");
                        String slug = chapter.getString("slug");
                        String status = chapter.getString("status");
                        String pageview = chapter.getString("pageview");
                        String book_title = chapter.getString("book_title");

                        ChapterModel model = new ChapterModel(id, book_id, title, slug, status, pageview, book_title);
                        chapterList.add(model);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                ChapterAdapter adapter = new ChapterAdapter(chapterList, activity);
                chapterRV.setAdapter(adapter);
                Objects.requireNonNull(chapterRV.getAdapter()).notifyDataSetChanged();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Request.Method.GET, activity, Constant.CHAPTER_API2 + "?book_slug=" + data , new HashMap<>(), false);
    }

}