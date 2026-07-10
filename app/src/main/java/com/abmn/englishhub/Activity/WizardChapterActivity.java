package com.abmn.englishhub.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.R;

// Chapter list for the "Wizard" section. Currently holds a single chapter
// ("ইতিহাসের অদ্ভুত পাতা"); built as a list screen (not a direct jump to
// lessons) so more chapters can be added later without restructuring.
public class WizardChapterActivity extends AppCompatActivity {

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

        Activity activity = this;
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("উইজার্ড");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        CardView chapterCard = findViewById(R.id.chapterCard);
        ((TextView) chapterCard.findViewById(R.id.titleTV)).setText("ইতিহাসের অদ্ভুত পাতা");
        chapterCard.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, WizardLessonActivity.class)));
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
