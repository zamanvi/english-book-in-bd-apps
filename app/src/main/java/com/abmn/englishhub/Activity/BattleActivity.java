package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.abmn.englishhub.Adapter.BattleAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BattleActivity extends AppCompatActivity {

    private TextInputEditText opponentIdET;
    private AutoCompleteTextView lessonDropdown;
    private MaterialButton challengeBtn;
    private TextView challengeErrorTV, pendingEmptyTV, historyEmptyTV;
    private RecyclerView pendingRV, historyRV;
    private SwipeRefreshLayout swipeRefresh;

    private final List<JSONObject> pendingBattles = new ArrayList<>();
    private final List<JSONObject> historyBattles = new ArrayList<>();
    private BattleAdapter pendingAdapter, historyAdapter;

    // lesson list for dropdown
    private final List<String> lessonNames = new ArrayList<>();
    private final List<Integer> lessonIds   = new ArrayList<>();
    private int selectedLessonId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_battle);

        opponentIdET     = findViewById(R.id.opponentIdET);
        lessonDropdown   = findViewById(R.id.lessonDropdown);
        challengeBtn     = findViewById(R.id.challengeBtn);
        challengeErrorTV = findViewById(R.id.challengeErrorTV);
        pendingRV        = findViewById(R.id.pendingRV);
        historyRV        = findViewById(R.id.historyRV);
        pendingEmptyTV   = findViewById(R.id.pendingEmptyTV);
        historyEmptyTV   = findViewById(R.id.historyEmptyTV);
        swipeRefresh     = findViewById(R.id.battleSwipeRefresh);

        swipeRefresh.setColorSchemeColors(0xFF2DD4BF);
        swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF0C0E26);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        pendingAdapter = new BattleAdapter(pendingBattles, this::openBattle);
        pendingRV.setLayoutManager(new LinearLayoutManager(this));
        pendingRV.setAdapter(pendingAdapter);

        historyAdapter = new BattleAdapter(historyBattles, this::openBattle);
        historyRV.setLayoutManager(new LinearLayoutManager(this));
        historyRV.setAdapter(historyAdapter);

        challengeBtn.setOnClickListener(v -> sendChallenge());

        swipeRefresh.setOnRefreshListener(() -> {
            loadPending();
            loadHistory();
        });

        loadLessons();
        loadPending();
        loadHistory();
    }

    private void loadLessons() {
        String url = Constant.ROOT_API2 + "lessons";
        ApiConfig.RequestToVolley((result, response, error) -> {
            if (!result) return;
            try {
                JSONArray arr = new JSONObject(response).optJSONArray("lessons");
                if (arr == null) return;
                lessonNames.clear(); lessonIds.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject l = arr.getJSONObject(i);
                    lessonNames.add(l.optString("title", "Lesson " + l.optInt("id")));
                    lessonIds.add(l.optInt("id"));
                }
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_dropdown_item_1line, lessonNames);
                    lessonDropdown.setAdapter(adapter);
                    lessonDropdown.setOnItemClickListener((parent, view, position, id) ->
                            selectedLessonId = lessonIds.get(position));
                    if (!lessonNames.isEmpty()) lessonDropdown.setText("", false);
                });
            } catch (Exception ignored) {}
        }, com.android.volley.Request.Method.GET, this, url, new java.util.HashMap<>(), false);
    }

    private void sendChallenge() {
        String oidStr = opponentIdET.getText() != null ? opponentIdET.getText().toString().trim() : "";

        if (oidStr.isEmpty()) { showError("বন্ধুর User ID দাও"); return; }
        if (selectedLessonId == 0) { showError("একটা Lesson বেছে নাও"); return; }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showError("লগইন করো আগে"); return; }

        JSONObject body = new JSONObject();
        try {
            body.put("opponent_id", Integer.parseInt(oidStr));
            body.put("lesson_id", selectedLessonId);
        } catch (Exception e) { showError("সঠিক User ID দাও"); return; }

        ApiConfig.postRequest(this, Constant.BATTLE_CHALLENGE, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if ("success".equals(r.optString("status"))) {
                    showError("");
                    runOnUiThread(() -> {
                        opponentIdET.setText("");
                        lessonDropdown.setText("", false);
                        selectedLessonId = 0;
                        // Go play the quiz
                        int battleId = r.optInt("battle_id", 0);
                        int lessonId = r.optInt("lesson_id", 0);
                        // Open QuizActivity with lesson, pass battleId for later submit
                        Intent intent = new Intent(this, QuizActivity.class);
                        intent.putExtra("lesson_id", lessonId);
                        intent.putExtra("battle_id", battleId);
                        startActivity(intent);
                    });
                } else {
                    showError(r.optString("message", "সমস্যা হয়েছে"));
                }
            } catch (Exception e) { showError("সমস্যা হয়েছে"); }
        }, error -> showError("নেটওয়ার্ক সমস্যা"));
    }

    private void loadPending() {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;
        ApiConfig.getRequest(this, Constant.BATTLE_PENDING, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                JSONArray arr = r.optJSONArray("battles");
                if (arr == null) return;
                pendingBattles.clear();
                for (int i = 0; i < arr.length(); i++) pendingBattles.add(arr.getJSONObject(i));
                runOnUiThread(() -> {
                    pendingAdapter.notifyDataSetChanged();
                    pendingEmptyTV.setVisibility(pendingBattles.isEmpty() ? View.VISIBLE : View.GONE);
                    swipeRefresh.setRefreshing(false);
                });
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void loadHistory() {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;
        ApiConfig.getRequest(this, Constant.BATTLE_HISTORY, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                JSONArray arr = r.optJSONArray("battles");
                if (arr == null) return;
                historyBattles.clear();
                for (int i = 0; i < arr.length(); i++) historyBattles.add(arr.getJSONObject(i));
                runOnUiThread(() -> {
                    historyAdapter.notifyDataSetChanged();
                    historyEmptyTV.setVisibility(historyBattles.isEmpty() ? View.VISIBLE : View.GONE);
                    swipeRefresh.setRefreshing(false);
                });
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void openBattle(JSONObject battle) {
        int battleId  = battle.optInt("id", 0);
        int lessonId  = battle.optInt("lesson_id", 0);
        String status = battle.optString("status", "");

        // If pending for me (I'm opponent), go play
        if ("challenger_done".equals(status)) {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("lesson_id", lessonId);
            intent.putExtra("battle_id", battleId);
            startActivity(intent);
        }
        // else already completed - could show result detail screen (future enhancement)
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                challengeErrorTV.setVisibility(View.GONE);
            } else {
                challengeErrorTV.setText(msg);
                challengeErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }
}
