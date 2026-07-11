package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.WizardChapterAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.WizardChapterModel;
import com.abmn.englishhub.R;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// Chapter list for the "Wizard" section, fetched from the backend so an
// admin can add more chapters later without an app update.
public class WizardChapterActivity extends AppCompatActivity {

    private Activity activity;
    private RecyclerView chapterRV;
    private ProgressBar loadingPB;
    private View emptyLL;
    private TextView emptyTV;
    private List<WizardChapterModel> chapterList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wizard_chapter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activity = this;
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("উইজার্ড");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chapterRV = findViewById(R.id.chapterRV);
        loadingPB = findViewById(R.id.loadingPB);
        emptyLL = findViewById(R.id.emptyLL);
        emptyTV = findViewById(R.id.emptyTV);
        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        chapterRV.setLayoutManager(linearLayout);

        chapterList = new ArrayList<>();
        chapterRV.setAdapter(new WizardChapterAdapter(chapterList, activity));
        loadingPB.setVisibility(View.VISIBLE);
        fetchChapters();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchChapters() {
        ApiConfig.RequestToVolley((result, response, error) -> {
            boolean failed = false;
            try {
                if (result) {
                    JSONObject chaptersObject = new JSONObject(response).getJSONObject("chapters");
                    JSONArray dataArray = chaptersObject.getJSONArray(Constant.DATA);
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject chapter = dataArray.getJSONObject(i);
                        chapterList.add(new WizardChapterModel(
                                chapter.getInt("id"),
                                chapter.getString("title"),
                                chapter.optString("subtitle", "")));
                    }
                    Objects.requireNonNull(chapterRV.getAdapter()).notifyDataSetChanged();
                } else {
                    failed = true;
                }
            } catch (Exception e) {
                failed = true;
            }

            loadingPB.setVisibility(View.GONE);
            if (chapterList.isEmpty()) {
                emptyTV.setText(failed ? "লোড করা যায়নি" : "কোনো গল্প নেই");
                emptyLL.setVisibility(View.VISIBLE);
            }
        }, Request.Method.GET, activity, Constant.WIZARD_CHAPTERS, new HashMap<>(), true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
