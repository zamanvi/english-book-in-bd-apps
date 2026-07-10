package com.abmn.englishhub.Activity;

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
import com.abmn.englishhub.Helper.WizardStoryData;
import com.abmn.englishhub.R;

// Shows the 10 story lessons inside the "ইতিহাসের অদ্ভুত পাতা" chapter.
public class WizardLessonActivity extends AppCompatActivity {

    private Activity activity;

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
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ইতিহাসের অদ্ভুত পাতা");
            getSupportActionBar().setSubtitle("বিশ্বাস না হওয়া সত্যি");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView lessonRV = findViewById(R.id.lessonRV);
        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        lessonRV.setLayoutManager(linearLayout);
        lessonRV.setAdapter(new WizardLessonAdapter(WizardStoryData.getAll(), activity));
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
