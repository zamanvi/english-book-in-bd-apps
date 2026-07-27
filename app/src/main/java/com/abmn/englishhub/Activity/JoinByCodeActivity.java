package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

public class JoinByCodeActivity extends AppCompatActivity {

    private TextInputEditText codeET;
    private MaterialButton resolveBtn, joinBtn;
    private android.widget.TextView resolveErrorTV, joinErrorTV, foundBattleTV, foundBattleDetailTV;
    private CardView foundBattleCV;

    private int foundBattleId = 0;
    private int foundLessonId = 0;
    private int foundQuestionCount = 10;
    private int foundLives = 0;
    private String resolvedCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_join_by_code);

        codeET              = findViewById(R.id.codeET);
        resolveBtn          = findViewById(R.id.resolveBtn);
        joinBtn             = findViewById(R.id.joinBtn);
        resolveErrorTV      = findViewById(R.id.resolveErrorTV);
        joinErrorTV         = findViewById(R.id.joinErrorTV);
        foundBattleTV       = findViewById(R.id.foundBattleTV);
        foundBattleDetailTV = findViewById(R.id.foundBattleDetailTV);
        foundBattleCV       = findViewById(R.id.foundBattleCV);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        resolveBtn.setOnClickListener(v -> resolveCode());
        joinBtn.setOnClickListener(v -> joinBattle());
    }

    private void resolveCode() {
        String code = codeET.getText() != null ? codeET.getText().toString().trim().toUpperCase() : "";
        if (code.isEmpty()) { showResolveError("কোড লিখো"); return; }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showResolveError("লগইন করো আগে"); return; }

        if (!new UConfig(this).isConnected()) {
            new UConfig(this).isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
            return;
        }

        foundBattleCV.setVisibility(View.GONE);
        showResolveError("");

        String url = Constant.BATTLE_RESOLVE_CODE + "?code=" + code;
        ApiConfig.getRequest(this, url, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    showResolveError(r.optString("message", "খুঁজে পাওয়া যায়নি"));
                    return;
                }

                foundBattleId      = r.optInt("battle_id", 0);
                foundLessonId      = r.optInt("lesson_id", 0);
                foundQuestionCount = r.optInt("question_count", 10);
                foundLives         = r.optInt("lives", 0);
                resolvedCode       = code;

                String creatorName   = r.optString("creator_name", "একজন বন্ধু");
                String lessonTitle   = r.optString("lesson_title", "");
                int participantCount = r.optInt("participant_count", 1);

                runOnUiThread(() -> {
                    foundBattleTV.setText("🎮 " + creatorName + " এর চ্যালেঞ্জ");
                    String detail = lessonTitle + " • " + foundQuestionCount + " টি প্রশ্ন"
                            + (foundLives > 0 ? " • ❤️ " + foundLives + " lives" : "")
                            + " • 👥 " + participantCount + " জন যোগ দিয়েছে";
                    foundBattleDetailTV.setText(detail);
                    foundBattleCV.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                showResolveError("সমস্যা হয়েছে");
            }
        }, error -> showResolveError("কোড খুঁজে পাওয়া যায়নি"));
    }

    private void joinBattle() {
        if (foundBattleId == 0) { showJoinError("আগে একটা কোড খুঁজে বের করো"); return; }

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showJoinError("লগইন করো আগে"); return; }

        if (!new UConfig(this).isConnected()) {
            new UConfig(this).isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
            return;
        }

        joinBtn.setEnabled(false);
        joinBtn.setText("⏳ যোগ দেওয়া হচ্ছে...");

        JSONObject body = new JSONObject();
        try { body.put("code", resolvedCode); } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.BATTLE_JOIN, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    runOnUiThread(this::resetJoinBtn);
                    showJoinError(r.optString("message", "সমস্যা হয়েছে"));
                    return;
                }

                JSONObject battle = r.optJSONObject("battle");
                String wordIds = "";
                if (battle != null) {
                    JSONArray wordIdsArr = battle.optJSONArray("question_word_ids");
                    if (wordIdsArr != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < wordIdsArr.length(); i++) {
                            if (sb.length() > 0) sb.append(",");
                            sb.append(wordIdsArr.optInt(i));
                        }
                        wordIds = sb.toString();
                    }
                }
                final String finalWordIds = wordIds;

                runOnUiThread(() -> {
                    Intent intent = new Intent(this, QuizActivity.class);
                    intent.putExtra("lesson_id", foundLessonId);
                    intent.putExtra("battle_id", foundBattleId);
                    intent.putExtra("question_count", foundQuestionCount);
                    intent.putExtra("lives", foundLives);
                    if (!finalWordIds.isEmpty()) intent.putExtra("word_ids", finalWordIds);
                    startActivity(intent);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(this::resetJoinBtn);
                showJoinError("সমস্যা হয়েছে");
            }
        }, error -> {
            runOnUiThread(this::resetJoinBtn);
            String message = error.getMessage();
            showJoinError(message != null ? message : "নেটওয়ার্ক সমস্যা");
        });
    }

    private void resetJoinBtn() {
        joinBtn.setEnabled(true);
        joinBtn.setText("⚔️ যোগ দাও এবং খেলো");
    }

    private void showResolveError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                resolveErrorTV.setVisibility(View.GONE);
            } else {
                resolveErrorTV.setText(msg);
                resolveErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showJoinError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                joinErrorTV.setVisibility(View.GONE);
            } else {
                joinErrorTV.setText(msg);
                joinErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }
}
