package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.MatchBoard;
import com.abmn.englishhub.Helper.QuizExitGuard;
import com.abmn.englishhub.Helper.QuizRounds;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Quick Quiz Round 3 - "জোড়া মেলাও" (match pairs).
 *
 * Ten English words and their Bangla meanings, five per board: tap a word,
 * then the meaning you think belongs to it. A right pair lights up and
 * stays; a wrong one costs a heart (three hearts = round failed, the same
 * rule every other round uses). Results go through ResultActivity with
 * round = 3, so XP, stars, Lipto, streak and unlocking all use the existing
 * submit path.
 */
public class MatchPairsActivity extends AppCompatActivity {

    private static final int ROUND = 3;
    private static final int MAX_LIVES = 3;
    private static final long FLASH_MS = 450;
    private static final long NEXT_BOARD_MS = 650;

    private LinearLayout leftCol, rightCol, heartsRowLL;
    private TextView boardCounterTV;
    private ProgressBar progressBar, loadingBar;
    private QuizExitGuard exitGuard;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private ToneGenerator toneGenerator;

    private int lessonId = 1;
    private final List<MatchBoard.Pair> allPairs = new ArrayList<>();
    private List<List<MatchBoard.Pair>> boards = new ArrayList<>();
    private int boardIndex = 0;
    private MatchBoard board;

    private final List<CardView> wordCards = new ArrayList<>();
    private final List<CardView> meaningCards = new ArrayList<>();
    private final List<Integer> wordCardPair = new ArrayList<>();
    private final List<Integer> meaningCardPair = new ArrayList<>();
    private int selectedWord = -1;     // card position in the left column
    private int selectedMeaning = -1;  // card position in the right column
    private boolean busy = false;      // true while a wrong pair is flashing

    private int livesRemaining = MAX_LIVES;
    private int matchedTotal = 0;
    private long startMs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_pairs);
        lessonId = getIntent().getIntExtra("lesson_id", 1);

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }

        leftCol = findViewById(R.id.leftColLL);
        rightCol = findViewById(R.id.rightColLL);
        heartsRowLL = findViewById(R.id.heartsRowLL);
        boardCounterTV = findViewById(R.id.boardCounterTV);
        progressBar = findViewById(R.id.matchProgressBar);
        loadingBar = findViewById(R.id.matchLoadingBar);

        exitGuard = QuizExitGuard.attach(this, () -> matchedTotal > 0 || livesRemaining < MAX_LIVES);
        findViewById(R.id.closeBtn).setOnClickListener(v -> exitGuard.requestExit());

        setupHearts();
        fetchPairs();
    }

    // ── API ──────────────────────────────────────────────────────

    private void fetchPairs() {
        loadingBar.setVisibility(View.VISIBLE);
        String token = new UConfig(this).getData(Constant.TOKEN);
        String url = QuizRounds.withVersion(Constant.GAME_ROUND_QUIZ + lessonId + "/" + ROUND);

        ApiConfig.getRequest(this, url, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!Constant.SUCCESS.equals(json.optString(Constant.STATUS, ""))) {
                    failAndClose(null);
                    return;
                }
                JSONObject payload = json.optJSONObject(Constant.DATA);
                JSONArray data = payload != null ? payload.optJSONArray("questions") : null;
                if (data == null) {
                    failAndClose(null);
                    return;
                }
                List<MatchBoard.Pair> pairs = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject q = data.getJSONObject(i);
                    String word = q.optString("word", "").trim();
                    String meaning = q.optString("meaning", "").trim();
                    if (!word.isEmpty() && !meaning.isEmpty()) {
                        pairs.add(new MatchBoard.Pair(q.optInt("word_id", i), word, meaning));
                    }
                }
                runOnUiThread(() -> startRound(pairs));
            } catch (Exception e) {
                failAndClose(null);
            }
        }, error -> failAndClose(error.getMessage()));
    }

    private void failAndClose(String serverMessage) {
        runOnUiThread(() -> {
            loadingBar.setVisibility(View.GONE);
            Toast.makeText(this,
                    serverMessage != null ? serverMessage : "নেটওয়ার্ক সমস্যা — আবার চেষ্টা করো",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void startRound(List<MatchBoard.Pair> pairs) {
        loadingBar.setVisibility(View.GONE);
        if (pairs.size() < MatchBoard.BOARD_SIZE) {
            Toast.makeText(this, "এই লেসনে জোড়া মেলানোর মতো যথেষ্ট শব্দ নেই", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        allPairs.clear();
        allPairs.addAll(pairs);
        boards = MatchBoard.split(allPairs);
        startMs = System.currentTimeMillis();
        showBoard(0);
    }

    // ── Board ────────────────────────────────────────────────────

    private void showBoard(int index) {
        boardIndex = index;
        board = new MatchBoard(boards.get(index), random);
        boardCounterTV.setText("বোর্ড " + (index + 1) + "/" + boards.size());
        selectedWord = -1;
        selectedMeaning = -1;
        busy = false;

        leftCol.removeAllViews();
        rightCol.removeAllViews();
        wordCards.clear();
        meaningCards.clear();
        wordCardPair.clear();
        meaningCardPair.clear();

        int n = board.pairs().size();
        for (int pos = 0; pos < n; pos++) {
            int wordPair = board.wordOrder().get(pos);
            int meaningPair = board.meaningOrder().get(pos);
            final int p = pos;

            CardView wc = makeCard(board.pairs().get(wordPair).word, 17, true);
            wc.setOnClickListener(v -> onWordTapped(p));
            leftCol.addView(wc);
            wordCards.add(wc);
            wordCardPair.add(wordPair);

            CardView mc = makeCard(board.pairs().get(meaningPair).meaning, 14, false);
            mc.setOnClickListener(v -> onMeaningTapped(p));
            rightCol.addView(mc);
            meaningCards.add(mc);
            meaningCardPair.add(meaningPair);
        }
        updateProgress();
    }

    private CardView makeCard(String text, int textSp, boolean bold) {
        float d = getResources().getDisplayMetrics().density;
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lp.setMargins(0, (int) (5 * d), 0, (int) (5 * d));
        card.setLayoutParams(lp);
        card.setRadius(14 * d);
        card.setCardElevation(0);
        card.setClickable(true);
        card.setFocusable(true);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(textSp);
        tv.setGravity(Gravity.CENTER);
        tv.setMaxLines(4);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tv.setPadding((int) (8 * d), (int) (6 * d), (int) (8 * d), (int) (6 * d));
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        card.addView(tv, new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.MATCH_PARENT));
        paint(card, State.NORMAL);
        return card;
    }

    private enum State { NORMAL, SELECTED, CORRECT, WRONG }

    private void paint(CardView card, State state) {
        int bg, fg;
        switch (state) {
            case SELECTED:
                bg = ContextCompat.getColor(this, R.color.indigo_dim);
                fg = ContextCompat.getColor(this, R.color.indigo);
                break;
            case CORRECT:
                bg = Color.parseColor("#2200E8B8");
                fg = ContextCompat.getColor(this, R.color.teal);
                break;
            case WRONG:
                bg = Color.parseColor("#33FF3F6C");
                fg = ContextCompat.getColor(this, R.color.red_wrong);
                break;
            default:
                bg = ContextCompat.getColor(this, R.color.bg_card);
                fg = ContextCompat.getColor(this, R.color.text_primary);
        }
        card.setCardBackgroundColor(bg);
        ((TextView) card.getChildAt(0)).setTextColor(fg);
        card.setAlpha(state == State.CORRECT ? 0.55f : 1f);
    }

    // ── Taps ─────────────────────────────────────────────────────

    private void onWordTapped(int pos) {
        if (busy || board.isMatched(wordCardPair.get(pos))) return;
        if (selectedWord >= 0) paint(wordCards.get(selectedWord), State.NORMAL);
        selectedWord = pos;
        paint(wordCards.get(pos), State.SELECTED);
        evaluate();
    }

    private void onMeaningTapped(int pos) {
        if (busy || board.isMatched(meaningCardPair.get(pos))) return;
        if (selectedMeaning >= 0) paint(meaningCards.get(selectedMeaning), State.NORMAL);
        selectedMeaning = pos;
        paint(meaningCards.get(pos), State.SELECTED);
        evaluate();
    }

    private void evaluate() {
        if (selectedWord < 0 || selectedMeaning < 0) return;
        final int w = selectedWord;
        final int m = selectedMeaning;
        selectedWord = -1;
        selectedMeaning = -1;

        if (board.tryMatch(wordCardPair.get(w), meaningCardPair.get(m))) {
            matchedTotal++;
            paint(wordCards.get(w), State.CORRECT);
            paint(meaningCards.get(m), State.CORRECT);
            wordCards.get(w).setClickable(false);
            meaningCards.get(m).setClickable(false);
            playTone(ToneGenerator.TONE_PROP_ACK);
            updateProgress();
            if (board.isComplete()) {
                busy = true;
                handler.postDelayed(this::nextBoardOrFinish, NEXT_BOARD_MS);
            }
        } else {
            busy = true;
            paint(wordCards.get(w), State.WRONG);
            paint(meaningCards.get(m), State.WRONG);
            playTone(ToneGenerator.TONE_PROP_NACK);
            loseLife();
            handler.postDelayed(() -> {
                paint(wordCards.get(w), State.NORMAL);
                paint(meaningCards.get(m), State.NORMAL);
                busy = false;
                if (livesRemaining <= 0) goToResult();
            }, FLASH_MS);
        }
    }

    private void nextBoardOrFinish() {
        if (boardIndex + 1 < boards.size()) {
            showBoard(boardIndex + 1);
        } else {
            goToResult();
        }
    }

    private void updateProgress() {
        progressBar.setProgress(allPairs.isEmpty() ? 0 : matchedTotal * 100 / allPairs.size());
    }

    // ── Hearts (same look as the other rounds) ───────────────────

    private void setupHearts() {
        heartsRowLL.removeAllViews();
        int marginPx = (int) (3 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < MAX_LIVES; i++) {
            TextView heart = new TextView(this);
            heart.setText("❤️");
            heart.setTextSize(18);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(marginPx, 0, marginPx, 0);
            heart.setLayoutParams(lp);
            heartsRowLL.addView(heart);
        }
    }

    private void loseLife() {
        if (livesRemaining <= 0) return;
        livesRemaining--;
        View heart = heartsRowLL.getChildAt(livesRemaining);
        if (heart instanceof TextView) {
            ((TextView) heart).setText("🖤");
            heart.setAlpha(0.5f);
        }
    }

    // ── Sound / finish ───────────────────────────────────────────

    private boolean soundEnabled() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean(Constant.SOUND_ENABLED, true);
    }

    private void playTone(int tone) {
        if (!soundEnabled() || toneGenerator == null) return;
        try { toneGenerator.startTone(tone, 150); } catch (Exception ignored) {}
    }

    private void goToResult() {
        if (isFinishing() || isDestroyed()) return;
        handler.removeCallbacksAndMessages(null);
        int total = allPairs.size();
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("correct", matchedTotal);
        intent.putExtra("total", total);
        intent.putExtra("xp", total > 0 ? Math.round(matchedTotal * 10f / total) : 0);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("time_sec", startMs == 0 ? 0 : (int) ((System.currentTimeMillis() - startMs) / 1000));
        intent.putExtra("round", ROUND);
        intent.putExtra("hearts_lost", MAX_LIVES - livesRemaining);
        intent.putExtra("max_lives", MAX_LIVES);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (toneGenerator != null) { toneGenerator.release(); toneGenerator = null; }
        if (exitGuard != null) exitGuard.dismiss();
    }
}
