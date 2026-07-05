package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private TextInputEditText opponentIdET, lessonIdET;
    private MaterialButton challengeBtn;
    private TextView challengeErrorTV;
    private RecyclerView pendingRV, historyRV;

    private final List<JSONObject> pendingBattles = new ArrayList<>();
    private final List<JSONObject> historyBattles = new ArrayList<>();
    private BattleAdapter pendingAdapter, historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        opponentIdET      = findViewById(R.id.opponentIdET);
        lessonIdET        = findViewById(R.id.lessonIdET);
        challengeBtn      = findViewById(R.id.challengeBtn);
        challengeErrorTV  = findViewById(R.id.challengeErrorTV);
        pendingRV         = findViewById(R.id.pendingRV);
        historyRV         = findViewById(R.id.historyRV);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        pendingAdapter = new BattleAdapter(pendingBattles, this::openBattle);
        pendingRV.setLayoutManager(new LinearLayoutManager(this));
        pendingRV.setAdapter(pendingAdapter);

        historyAdapter = new BattleAdapter(historyBattles, this::openBattle);
        historyRV.setLayoutManager(new LinearLayoutManager(this));
        historyRV.setAdapter(historyAdapter);

        challengeBtn.setOnClickListener(v -> sendChallenge());

        loadPending();
        loadHistory();
    }

    private void sendChallenge() {
        String oidStr = opponentIdET.getText() != null ? opponentIdET.getText().toString().trim() : "";
        String lidStr = lessonIdET.getText() != null ? lessonIdET.getText().toString().trim() : "";

        if (oidStr.isEmpty() || lidStr.isEmpty()) {
            showError("সব তথ্য দাও");
            return;
        }

        String token = UConfig.getString(this, "auth_token", "");
        if (token.isEmpty()) { showError("লগইন করো আগে"); return; }

        JSONObject body = new JSONObject();
        try {
            body.put("opponent_id", Integer.parseInt(oidStr));
            body.put("lesson_id", Integer.parseInt(lidStr));
        } catch (Exception e) { showError("সঠিক নম্বর দাও"); return; }

        ApiConfig.postRequest(this, Constant.BATTLE_CHALLENGE, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if ("success".equals(r.optString("status"))) {
                    showError("");
                    runOnUiThread(() -> {
                        opponentIdET.setText("");
                        lessonIdET.setText("");
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
        String token = UConfig.getString(this, "auth_token", "");
        if (token.isEmpty()) return;
        ApiConfig.getRequest(this, Constant.BATTLE_PENDING, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                JSONArray arr = r.optJSONArray("battles");
                if (arr == null) return;
                pendingBattles.clear();
                for (int i = 0; i < arr.length(); i++) pendingBattles.add(arr.getJSONObject(i));
                runOnUiThread(() -> pendingAdapter.notifyDataSetChanged());
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void loadHistory() {
        String token = UConfig.getString(this, "auth_token", "");
        if (token.isEmpty()) return;
        ApiConfig.getRequest(this, Constant.BATTLE_HISTORY, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                JSONArray arr = r.optJSONArray("battles");
                if (arr == null) return;
                historyBattles.clear();
                for (int i = 0; i < arr.length(); i++) historyBattles.add(arr.getJSONObject(i));
                runOnUiThread(() -> historyAdapter.notifyDataSetChanged());
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
