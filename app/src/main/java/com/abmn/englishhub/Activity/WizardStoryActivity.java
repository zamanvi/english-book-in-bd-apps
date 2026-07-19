package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

        applyTheme();
        fetchStory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wizard_story, menu);
        return true;
    }

    private String currentTheme() {
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getString(Constant.CONTENT_THEME, Constant.CONTENT_THEME_STORYBOOK);
    }

    private void toggleTheme() {
        String next = currentTheme().equals(Constant.CONTENT_THEME_STORYBOOK)
                ? Constant.CONTENT_THEME_APP : Constant.CONTENT_THEME_STORYBOOK;
        activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit().putString(Constant.CONTENT_THEME, next).apply();
        applyTheme();
        Toast.makeText(activity,
                next.equals(Constant.CONTENT_THEME_STORYBOOK) ? "Storybook theme" : "App theme",
                Toast.LENGTH_SHORT).show();
    }

    private boolean isStorybook() {
        return currentTheme().equals(Constant.CONTENT_THEME_STORYBOOK);
    }

    private int paragraphColor(boolean isEnglish) {
        boolean storybook = isStorybook();
        if (isEnglish) {
            return storybook ? android.graphics.Color.parseColor("#241C10")
                    : getResources().getColor(R.color.text_primary);
        }
        return storybook ? android.graphics.Color.parseColor("#3A2E1A")
                : getResources().getColor(R.color.text_secondary);
    }

    private void applyTheme() {
        LinearLayout taleCardBg = findViewById(R.id.taleCardBg);
        TextView metaTV = findViewById(R.id.metaTV);
        TextView hookTitleTV = findViewById(R.id.hookTitleTV);
        androidx.cardview.widget.CardView translationCV = findViewById(R.id.translationCV);
        TextView translationLabelTV = findViewById(R.id.translationLabelTV);
        TextView banglaTitleTV = findViewById(R.id.banglaTitleTV);
        boolean storybook = isStorybook();

        if (storybook) {
            android.graphics.drawable.GradientDrawable taleShape = new android.graphics.drawable.GradientDrawable();
            taleShape.setColor(android.graphics.Color.parseColor("#EFE6CC"));
            taleShape.setCornerRadius(dp(16));
            taleCardBg.setBackground(taleShape);
            translationCV.setCardBackgroundColor(android.graphics.Color.parseColor("#EFE6CC"));
            int accent = android.graphics.Color.parseColor("#7A4A00");
            metaTV.setTextColor(accent);
            hookTitleTV.setTextColor(accent);
            int purpleAccent = android.graphics.Color.parseColor("#6D3FC0");
            translationLabelTV.setTextColor(purpleAccent);
            banglaTitleTV.setTextColor(android.graphics.Color.parseColor("#241C10"));
        } else {
            taleCardBg.setBackgroundResource(R.drawable.grad_word_of_day);
            translationCV.setCardBackgroundColor(getResources().getColor(R.color.bg_elevated));
            int accent = getResources().getColor(R.color.indigo);
            metaTV.setTextColor(accent);
            hookTitleTV.setTextColor(accent);
            translationLabelTV.setTextColor(accent);
            banglaTitleTV.setTextColor(getResources().getColor(R.color.text_primary));
        }

        findViewById(R.id.main).setBackgroundColor(android.graphics.Color.parseColor(
                storybook ? "#F7F1E1" : "#07081A"));

        retintExistingContent();
    }

    private void retintExistingContent() {
        boolean storybook = isStorybook();

        LinearLayout englishContainer = findViewById(R.id.englishContainer);
        for (int i = 0; i < englishContainer.getChildCount(); i++) {
            ((TextView) englishContainer.getChildAt(i)).setTextColor(paragraphColor(true));
        }

        LinearLayout banglaContainer = findViewById(R.id.banglaContainer);
        for (int i = 0; i < banglaContainer.getChildCount(); i++) {
            ((TextView) banglaContainer.getChildAt(i)).setTextColor(paragraphColor(false));
        }

        LinearLayout notesContainer = findViewById(R.id.notesContainer);
        int noteBg = storybook ? android.graphics.Color.parseColor("#EFE6CC")
                : getResources().getColor(R.color.indigo_dim);
        int labelColor = storybook ? android.graphics.Color.parseColor("#6D3FC0")
                : getResources().getColor(R.color.indigo);
        int bodyColor = paragraphColor(false);
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            LinearLayout card = (LinearLayout) notesContainer.getChildAt(i);
            card.setBackground(noteTipShape(noteBg));
            ((TextView) card.getChildAt(0)).setTextColor(labelColor);
            ((TextView) card.getChildAt(1)).setTextColor(bodyColor);
        }
    }

    private android.graphics.drawable.GradientDrawable noteTipShape(int color) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(color);
        float r = dp(10);
        shape.setCornerRadii(new float[]{0, 0, r, r, r, r, 0, 0});
        return shape;
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
                            paragraphColor(true), 15f, 1.35f);
                    addParagraphs(findViewById(R.id.banglaContainer), banglaParagraphs,
                            paragraphColor(false), 15f, 1.5f);
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
                                int color, float sizeSp, float lineSpacingMultiplier) {
        for (int i = 0; i < paragraphs.size(); i++) {
            TextView tv = new TextView(activity);
            tv.setText(paragraphs.get(i));
            tv.setTextColor(color);
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
        boolean storybook = isStorybook();
        int cardBg = storybook ? android.graphics.Color.parseColor("#EFE6CC")
                : getResources().getColor(R.color.indigo_dim);
        int labelColor = storybook ? android.graphics.Color.parseColor("#6D3FC0")
                : getResources().getColor(R.color.indigo);
        int bodyColor = paragraphColor(false);

        for (String[] note : notes) {
            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(noteTipShape(cardBg));
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.bottomMargin = dp(10);
            card.setLayoutParams(cardLp);

            TextView label = new TextView(activity);
            label.setText(note[0]);
            label.setTextColor(labelColor);
            label.setTextSize(11f);
            label.setTypeface(null, Typeface.BOLD);
            label.setLetterSpacing(0.08f);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            labelLp.bottomMargin = dp(6);
            label.setLayoutParams(labelLp);
            card.addView(label);

            TextView body = new TextView(activity);
            body.setText(note[1]);
            body.setTextColor(bodyColor);
            body.setTextSize(14f);
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
        if (item.getItemId() == R.id.themeToggleId) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
