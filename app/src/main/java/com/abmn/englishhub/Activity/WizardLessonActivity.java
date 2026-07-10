package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.WizardLessonAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.WizardStoryListItemModel;
import com.abmn.englishhub.R;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// Shows the story lessons inside a Wizard chapter, fetched from the backend.
public class WizardLessonActivity extends AppCompatActivity {

    private Activity activity;
    private RecyclerView lessonRV;
    private List<WizardStoryListItemModel> storyList;
    private int chapterId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wizard_lesson);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activity = this;
        chapterId = getIntent().getIntExtra(Constant.FROM_ID, 0);
        String title = getIntent().getStringExtra(Constant.FROM_TITLE);
        String subtitle = getIntent().getStringExtra(Constant.FROM_SUBTITLE);

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            if (subtitle != null && !subtitle.isEmpty()) {
                getSupportActionBar().setSubtitle(subtitle);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        lessonRV = findViewById(R.id.lessonRV);
        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        lessonRV.setLayoutManager(linearLayout);

        storyList = new ArrayList<>();
        lessonRV.setAdapter(new WizardLessonAdapter(storyList, activity));
        fetchStories();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchStories() {
        String url = Constant.WIZARD_STORIES + "?chapter_id=" + chapterId;
        ApiConfig.RequestToVolley((result, response, error) -> {
            try {
                if (result) {
                    JSONObject storiesObject = new JSONObject(response).getJSONObject("stories");
                    JSONArray dataArray = storiesObject.getJSONArray(Constant.DATA);
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject story = dataArray.getJSONObject(i);
                        storyList.add(new WizardStoryListItemModel(
                                story.getInt("id"),
                                story.getString("hook_title")));
                    }
                    Objects.requireNonNull(lessonRV.getAdapter()).notifyDataSetChanged();
                }
            } catch (Exception e) {
                // ignored
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true);
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
