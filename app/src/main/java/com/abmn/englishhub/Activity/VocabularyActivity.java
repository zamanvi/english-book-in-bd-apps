package com.abmn.englishhub.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;

public class VocabularyActivity extends AppCompatActivity {
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vocabulary_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        define();
    }

    private void define() {
        activity = this;
        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.vocabularyCategoryCV).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyChapterActivity.class)
                        .putExtra(Constant.FROM_TYPE, "vocabulary")
                        .putExtra(Constant.FROM_TITLE, "Vocabulary")));

        findViewById(R.id.verbCategoryCV).setOnClickListener(v ->
                startActivity(new Intent(activity, VocabularyChapterActivity.class)
                        .putExtra(Constant.FROM_TYPE, "verb")
                        .putExtra(Constant.FROM_TITLE, "Verb")));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
