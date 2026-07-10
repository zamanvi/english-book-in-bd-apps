package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Shows a single "ইতিহাসের অদ্ভুত পাতা" story: the English tale, its Bangla
// translation, and grammar notes. No chapter/section header here - the
// toolbar title (the story's own hook title) and the back arrow are enough,
// same pattern as the rest of the app's content/details screens.
public class WizardStoryActivity extends AppCompatActivity {

    private Activity activity;
    private int storyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wizard_story);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        activity = this;
        storyId = getIntent().getIntExtra(Constant.FROM_ID, 0);

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fetchStory();
    }

    @SuppressLint("SetTextI18n")
    private void fetchStory() {
        String url = Constant.WIZARD_STORY_SHOW + storyId;
        ApiConfig.RequestToVolley((result, response, error) -> {
            try {
                if (result) {
                    JSONObject story = new JSONObject(response).getJSONObject("story");

                    String hookTitle = story.getString("hook_title");
                    String meta = story.optString("meta", "");
                    String banglaTitle = story.getString("bangla_title");
                    List<String> englishParagraphs = toStringList(story.getJSONArray("english_paragraphs"));
                    List<String> banglaParagraphs = toStringList(story.getJSONArray("bangla_paragraphs"));
                    List<String[]> grammarNotes = toNoteList(story.optJSONArray("grammar_notes"));

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(hookTitle);
                    }
                    ((TextView) findViewById(R.id.metaTV)).setText(meta);
                    ((TextView) findViewById(R.id.hookTitleTV)).setText(hookTitle);
                    ((TextView) findViewById(R.id.banglaTitleTV)).setText(banglaTitle);

                    addParagraphs(findViewById(R.id.englishContainer), englishParagraphs,
                            R.color.text_primary, 14.5f, 1.35f);
                    addParagraphs(findViewById(R.id.banglaContainer), banglaParagraphs,
                            R.color.text_secondary, 14f, 1.5f);
                    addNotes(findViewById(R.id.notesContainer), grammarNotes);
                }
            } catch (Exception e) {
                // ignored
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true);
    }

    private List<String> toStringList(JSONArray array) throws Exception {
        List<String> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    private List<String[]> toNoteList(JSONArray array) throws Exception {
        List<String[]> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            JSONObject note = array.getJSONObject(i);
            list.add(new String[]{note.optString("label", ""), note.optString("text", "")});
        }
        return list;
    }

    private void addParagraphs(LinearLayout container, List<String> paragraphs,
                                int colorRes, float sizeSp, float lineSpacingMultiplier) {
        for (int i = 0; i < paragraphs.size(); i++) {
            TextView tv = new TextView(activity);
            tv.setText(paragraphs.get(i));
            tv.setTextColor(getResources().getColor(colorRes));
            tv.setTextSize(sizeSp);
            tv.setLineSpacing(0f, lineSpacingMultiplier);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < paragraphs.size() - 1) lp.bottomMargin = dp(10);
            tv.setLayoutParams(lp);
            container.addView(tv);
        }
    }

    private void addNotes(LinearLayout container, List<String[]> notes) {
        for (String[] note : notes) {
            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.bg_wizard_tip);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.bottomMargin = dp(10);
            card.setLayoutParams(cardLp);

            TextView label = new TextView(activity);
            label.setText(note[0]);
            label.setTextColor(getResources().getColor(R.color.indigo));
            label.setTextSize(10f);
            label.setTypeface(null, Typeface.BOLD);
            label.setLetterSpacing(0.08f);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.bottomMargin = dp(6);
            label.setLayoutParams(labelLp);
            card.addView(label);

            TextView body = new TextView(activity);
            body.setText(note[1]);
            body.setTextColor(getResources().getColor(R.color.text_secondary));
            body.setTextSize(12.5f);
            body.setLineSpacing(0f, 1.4f);
            card.addView(body);

            container.addView(card);
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
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
