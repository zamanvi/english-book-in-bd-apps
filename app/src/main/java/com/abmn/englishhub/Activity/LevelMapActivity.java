package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

// Entry point for the round-based Quick Quiz (Round 1 MCQ → Round 2 Reading →
// Round 3 Listening → Round 4 Writing). Shows lock/unlock/passed state and
// star rating per round, fetched from GameController::levelMap. Rounds 1-3
// launch QuizActivity in round mode; Round 4 launches a dedicated writing
// screen (not yet built - shows a "coming soon" toast until then).
public class LevelMapActivity extends AppCompatActivity {

    private static final String[] ROUND_ICONS  = {"🎯", "📖", "🎧", "✍️"};
    private static final String[] ROUND_TITLES = {
            "Round 1 · MCQ", "Round 2 · Reading", "Round 3 · Listening", "Round 4 · Writing"
    };

    private int lessonId;
    private View loadingBar;
    private final View[] roundRoots = new View[4];
    private final String[] roundStatus = {"locked", "locked", "locked", "locked"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_level_map);

        lessonId = getIntent().getIntExtra("lesson_id", 1);
        loadingBar = findViewById(R.id.levelMapLoadingBar);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        roundRoots[0] = findViewById(R.id.round1Include);
        roundRoots[1] = findViewById(R.id.round2Include);
        roundRoots[2] = findViewById(R.id.round3Include);
        roundRoots[3] = findViewById(R.id.round4Include);

        for (int i = 0; i < 4; i++) {
            int round = i + 1;
            TextView iconTV = roundRoots[i].findViewById(R.id.roundIconTV);
            TextView titleTV = roundRoots[i].findViewById(R.id.roundTitleTV);
            iconTV.setText(ROUND_ICONS[i]);
            titleTV.setText(ROUND_TITLES[i]);
            roundRoots[i].setOnClickListener(v -> onRoundTapped(round));
        }

        fetchLevelMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A round may have just been passed (returning from ResultActivity),
        // so always re-fetch instead of relying on onCreate's one-time load.
        if (loadingBar != null) fetchLevelMap();
    }

    private void fetchLevelMap() {
        loadingBar.setVisibility(View.VISIBLE);
        String token = new UConfig(this).getData(Constant.TOKEN);
        String url = Constant.GAME_LEVEL_MAP + lessonId;

        ApiConfig.getRequest(this, url, token, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;
                JSONArray rounds = json.optJSONArray(Constant.DATA);
                if (rounds == null) return;

                for (int i = 0; i < rounds.length() && i < 4; i++) {
                    JSONObject r = rounds.getJSONObject(i);
                    roundStatus[i] = r.optString("status", "locked");
                    int stars = r.optInt("stars", 0);
                    applyRoundState(i, roundStatus[i], stars);
                }
            } catch (Exception ignored) {
            } finally {
                runOnUiThread(() -> loadingBar.setVisibility(View.GONE));
            }
        }, error -> runOnUiThread(() -> loadingBar.setVisibility(View.GONE)));
    }

    private void applyRoundState(int index, String status, int stars) {
        runOnUiThread(() -> {
            TextView statusTV = roundRoots[index].findViewById(R.id.roundStatusTV);
            TextView starsTV  = roundRoots[index].findViewById(R.id.roundStarsTV);

            switch (status) {
                case "passed":
                    statusTV.setText("✅ ক্লিয়ার করেছো");
                    statusTV.setTextColor(ContextCompat.getColor(this, R.color.teal));
                    starsTV.setText(repeatStar(stars));
                    roundRoots[index].setAlpha(1f);
                    break;
                case "unlocked":
                    statusTV.setText("আনলক করা আছে — খেলো!");
                    statusTV.setTextColor(ContextCompat.getColor(this, R.color.gold));
                    starsTV.setText("");
                    roundRoots[index].setAlpha(1f);
                    break;
                default: // locked
                    statusTV.setText("🔒 আগের রাউন্ড ক্লিয়ার করো আগে");
                    statusTV.setTextColor(ContextCompat.getColor(this, R.color.text_muted));
                    starsTV.setText("");
                    roundRoots[index].setAlpha(0.55f);
            }
        });
    }

    private String repeatStar(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("⭐");
        return sb.toString();
    }

    private void onRoundTapped(int round) {
        String status = roundStatus[round - 1];
        if ("locked".equals(status)) {
            Toast.makeText(this, "🔒 আগের রাউন্ড ক্লিয়ার করো আগে!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (round == 4) {
            startActivity(new Intent(this, WritingActivity.class)
                    .putExtra("lesson_id", lessonId));
            return;
        }

        startActivity(new Intent(this, QuizActivity.class)
                .putExtra("lesson_id", lessonId)
                .putExtra("round", round));
    }
}
