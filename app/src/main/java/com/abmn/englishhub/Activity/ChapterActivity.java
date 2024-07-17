package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.abmn.englishhub.Adapter.ChapterAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.ChapterModel;
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

public class ChapterActivity extends AppCompatActivity {

    private Activity activity;
    private List<ChapterModel> chapterList;
    private UConfig uConfig;
    private RecyclerView chapterRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chapter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        define();
    }

    private void define() {

        activity = this;
        uConfig = new UConfig(activity);
        chapterRV = findViewById(R.id.chapterRV);
        chapterList = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        chapterRV.setLayoutManager(linearLayout);

        String slug = getIntent().getStringExtra(Constant.FROM);
        getData(slug);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData(String data) {
        JSONArray chapterArray = uConfig.getJSONArray(data);
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

                ApiConfig.RequestToVolley((result, response, error) -> {
                    try {
                        JSONObject itemsObject = new JSONObject(response);
                        JSONObject dataObject = itemsObject.getJSONObject("items");
                        JSONArray itemsArray = dataObject.getJSONArray(Constant.DATA);
                        uConfig.setJSONArray(slug, itemsArray);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }, Request.Method.GET, activity, Constant.ITEM_API + "?chapter_slug=" + slug, new HashMap<>(), true);

                ChapterModel model = new ChapterModel(id, book_id, title, slug, status, pageview, book_title);
                chapterList.add(model);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        ChapterAdapter adapter = new ChapterAdapter(chapterList, activity);
        chapterRV.setAdapter(adapter);
        Objects.requireNonNull(chapterRV.getAdapter()).notifyDataSetChanged();
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