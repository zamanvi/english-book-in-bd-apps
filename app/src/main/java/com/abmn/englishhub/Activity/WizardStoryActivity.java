package com.abmn.englishhub.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.abmn.texttospeech.TextToSpeechHelper;
import com.abmn.utility.UConfig;
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
    private TextToSpeechHelper ttsHelper;
    private List<String[]> vocabularyData = new ArrayList<>();

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
        UConfig uConfig = new UConfig(this);
        ttsHelper = new TextToSpeechHelper(this, uConfig.getData(Constant.VOICE_SPEED));

        Toolbar toolbar = findViewById(R.id.toolbarId);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        applyTheme();
        fetchStory();

        findViewById(R.id.playAllBtn).setOnClickListener(v -> playAllVocabulary());
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
        applyVocabularyTheme();
    }

    private int vocabAccentColor() {
        return isStorybook() ? android.graphics.Color.parseColor("#0F6E56")
                : getResources().getColor(R.color.teal);
    }

    // Shared with the "meaning" line in the vocabulary card, so a word looks the
    // same whether you spot it in the passage or in the list below it.
    private int vocabHighlightColor() {
        return isStorybook() ? android.graphics.Color.parseColor("#9B2C5E")
                : getResources().getColor(R.color.pink_accent);
    }

    private CharSequence highlightVocabWords(String text, List<String[]> vocabulary, int highlightColor) {
        SpannableString spannable = new SpannableString(text);
        for (String[] entry : vocabulary) {
            String word = entry[0];
            if (word == null || word.isEmpty()) continue;
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("\\b" + java.util.regex.Pattern.quote(word) + "\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
                    .matcher(text);
            while (matcher.find()) {
                spannable.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new ForegroundColorSpan(highlightColor), matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }

    // Bangla has no single "word" field to match against - only the short
    // "meaning" gloss, which is often a different inflection than what actually
    // appears in the flowing translation (e.g. meaning "গ্রাস করেছিল" vs passage
    // "গ্রাস করে নেয়"). So this only lights up meanings that happen to appear
    // verbatim as a substring - a quieter, honest subset rather than forcing
    // matches that aren't really there.
    //
    // Color only, no StyleSpan(BOLD): the app's theme font (Poppins) has no
    // Bengali glyphs, so Bangla already renders through a system fallback font.
    // Forcing bold on top of that fallback makes Android synthesize a fake bold
    // weight per-glyph, which is what actually breaks conjunct clusters
    // (যুক্তাক্ষর) into disconnected pieces - a real font, not just an
    // isolated one-off.
    private CharSequence highlightBanglaMeanings(String text, List<String[]> vocabulary, int highlightColor) {
        SpannableString spannable = new SpannableString(text);
        for (String[] entry : vocabulary) {
            String meaning = entry[2];
            if (meaning == null || meaning.isEmpty()) continue;
            for (String term : meaning.split(",")) {
                term = term.trim();
                if (term.isEmpty()) continue;
                int searchFrom = 0;
                int idx;
                while ((idx = text.indexOf(term, searchFrom)) >= 0) {
                    spannable.setSpan(new ForegroundColorSpan(highlightColor), idx, idx + term.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    searchFrom = idx + term.length();
                }
            }
        }
        return spannable;
    }

    private void applyVocabularyTheme() {
        boolean storybook = isStorybook();
        int accent = vocabAccentColor();

        androidx.cardview.widget.CardView vocabularyCV = findViewById(R.id.vocabularyCV);
        vocabularyCV.setCardBackgroundColor(storybook
                ? android.graphics.Color.parseColor("#EFE6CC")
                : getResources().getColor(R.color.bg_card));

        ((TextView) findViewById(R.id.vocabEyebrowTV)).setTextColor(accent);

        LinearLayout playAllBtn = findViewById(R.id.playAllBtn);
        playAllBtn.setBackground(dimShape(accent, false));
        ((ImageView) findViewById(R.id.playAllIcon)).setColorFilter(accent);
        ((TextView) playAllBtn.getChildAt(1)).setTextColor(accent);

        LinearLayout vocabularyContainer = findViewById(R.id.vocabularyContainer);
        vocabularyContainer.removeAllViews();
        addVocabulary(vocabularyContainer, vocabularyData);
        vocabularyCV.setVisibility(vocabularyData.isEmpty() ? View.GONE : View.VISIBLE);
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
                    vocabularyData = toVocabList(story.optJSONArray("vocabulary"));

                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(hookTitle);
                    }
                    ((TextView) findViewById(R.id.metaTV)).setText(meta);
                    ((TextView) findViewById(R.id.hookTitleTV)).setText(hookTitle);
                    ((TextView) findViewById(R.id.banglaTitleTV)).setText(banglaTitle);

                    addParagraphs(findViewById(R.id.englishContainer), englishParagraphs,
                            paragraphColor(true), 15f, 1.35f, vocabularyData, false);
                    addParagraphs(findViewById(R.id.banglaContainer), banglaParagraphs,
                            paragraphColor(false), 15f, 1.5f, vocabularyData, true);
                    addNotes(findViewById(R.id.notesContainer), grammarNotes);
                    applyVocabularyTheme();
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
                                int color, float sizeSp, float lineSpacingMultiplier,
                                List<String[]> highlightVocabulary, boolean banglaMode) {
        int highlightColor = vocabHighlightColor();
        for (int i = 0; i < paragraphs.size(); i++) {
            TextView tv = new TextView(activity);
            if (highlightVocabulary != null && !highlightVocabulary.isEmpty()) {
                CharSequence highlighted = banglaMode
                        ? highlightBanglaMeanings(paragraphs.get(i), highlightVocabulary, highlightColor)
                        : highlightVocabWords(paragraphs.get(i), highlightVocabulary, highlightColor);
                tv.setText(highlighted);
            } else {
                tv.setText(paragraphs.get(i));
            }
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

    private List<String[]> toVocabList(JSONArray array) throws Exception {
        List<String[]> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.length(); i++) {
            JSONObject v = array.getJSONObject(i);
            list.add(new String[]{
                    v.optString("word", ""), v.optString("phonetic", ""),
                    v.optString("meaning", ""), v.optString("pos", ""),
            });
        }
        return list;
    }

    private void addVocabulary(LinearLayout container, List<String[]> vocabulary) {
        boolean storybook = isStorybook();
        int accent = vocabAccentColor();
        int rowBg = storybook ? android.graphics.Color.parseColor("#E5D9B0")
                : getResources().getColor(R.color.bg_elevated);
        int wordColor = paragraphColor(true);
        int phoneticColor = storybook ? android.graphics.Color.parseColor("#6B5A3A")
                : getResources().getColor(R.color.text_muted);
        int meaningColor = vocabHighlightColor();

        for (String[] entry : vocabulary) {
            String word = entry[0];
            String phonetic = entry[1];
            String meaning = entry[2];
            String pos = entry[3];

            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(roundedShape(rowBg, dp(12)));
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(8);
            row.setLayoutParams(rowLp);

            TextView badge = new TextView(activity);
            badge.setText(pos);
            badge.setTextColor(accent);
            badge.setTextSize(10f);
            badge.setTypeface(null, Typeface.BOLD);
            badge.setGravity(Gravity.CENTER);
            badge.setBackground(dimShape(accent, dp(6)));
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(22), dp(22));
            badgeLp.setMarginEnd(dp(10));
            badge.setLayoutParams(badgeLp);
            row.addView(badge);

            LinearLayout textCol = new LinearLayout(activity);
            textCol.setOrientation(LinearLayout.VERTICAL);
            textCol.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            LinearLayout wordRow = new LinearLayout(activity);
            wordRow.setOrientation(LinearLayout.HORIZONTAL);
            wordRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView wordTV = new TextView(activity);
            wordTV.setText(word);
            wordTV.setTextColor(wordColor);
            wordTV.setTextSize(14f);
            wordTV.setTypeface(null, Typeface.BOLD);
            wordRow.addView(wordTV);

            if (phonetic != null && !phonetic.isEmpty()) {
                TextView phoneticTV = new TextView(activity);
                phoneticTV.setText(" (" + phonetic + ")");
                phoneticTV.setTextColor(phoneticColor);
                phoneticTV.setTextSize(12f);
                wordRow.addView(phoneticTV);
            }
            textCol.addView(wordRow);

            TextView meaningTV = new TextView(activity);
            meaningTV.setText(meaning);
            meaningTV.setTextColor(meaningColor);
            meaningTV.setTextSize(12f);
            LinearLayout.LayoutParams meaningLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            meaningLp.topMargin = dp(2);
            meaningTV.setLayoutParams(meaningLp);
            textCol.addView(meaningTV);

            row.addView(textCol);

            ImageView playBtn = new ImageView(activity);
            playBtn.setImageResource(R.drawable.ic_volume_up);
            playBtn.setColorFilter(accent);
            playBtn.setBackground(dimShape(accent, true));
            playBtn.setPadding(dp(7), dp(7), dp(7), dp(7));
            playBtn.setContentDescription(word + " উচ্চারণ শোনো");
            LinearLayout.LayoutParams playLp = new LinearLayout.LayoutParams(dp(32), dp(32));
            playLp.setMarginStart(dp(8));
            playBtn.setLayoutParams(playLp);
            playBtn.setClickable(true);
            playBtn.setFocusable(true);
            playBtn.setOnClickListener(v -> speakWord(word));
            row.addView(playBtn);

            container.addView(row);
        }
    }

    private void speakWord(String word) {
        if (ttsHelper == null || word == null || word.isEmpty()) return;
        ttsHelper.speak(word);
    }

    private void playAllVocabulary() {
        if (ttsHelper == null || vocabularyData.isEmpty()) return;
        Handler handler = new Handler(Looper.getMainLooper());
        long delay = 0;
        for (String[] entry : vocabularyData) {
            String word = entry[0];
            handler.postDelayed(() -> speakWord(word), delay);
            int wordCount = Math.max(1, word.split(" ").length);
            delay += wordCount * 600L + 400L;
        }
    }

    private android.graphics.drawable.GradientDrawable roundedShape(int color, int radiusPx) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radiusPx);
        return shape;
    }

    private android.graphics.drawable.GradientDrawable dimShape(int accentColor, boolean oval) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor((0x33 << 24) | (accentColor & 0x00FFFFFF));
        if (oval) {
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        } else {
            shape.setCornerRadius(dp(20));
        }
        return shape;
    }

    // Rounded-square variant (small corner radius) for the POS badge, which needs
    // to fit both single-letter (N/V) and 3-letter (ADJ) labels without clipping
    // the way a full oval would.
    private android.graphics.drawable.GradientDrawable dimShape(int accentColor, int cornerRadiusPx) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor((0x33 << 24) | (accentColor & 0x00FFFFFF));
        shape.setCornerRadius(cornerRadiusPx);
        return shape;
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
