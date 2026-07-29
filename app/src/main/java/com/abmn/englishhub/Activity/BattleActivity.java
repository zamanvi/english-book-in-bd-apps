package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

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

    private TextInputEditText friendCodeET;
    private AutoCompleteTextView lessonDropdown;
    private MaterialButton challengeBtn;
    private TextView challengeErrorTV, pendingEmptyTV, historyEmptyTV;
    private RecyclerView pendingRV, historyRV;
    private SwipeRefreshLayout swipeRefresh;

    private ToneGenerator toneGenerator;

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

        // This whole section is online-only - warn once up front instead of
        // letting every list silently fail one by one.
        if (!new UConfig(this).isConnected()) {
            new UConfig(this).isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
        }

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (RuntimeException e) {
            toneGenerator = null;
        }

        setContentView(R.layout.activity_battle);

        friendCodeET     = findViewById(R.id.friendCodeET);
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

        View customizeBtn = findViewById(R.id.customizeChallengeBtn);
        if (customizeBtn != null) {
            customizeBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, CreateChallengeActivity.class)));
        }
        View joinCodeBtn = findViewById(R.id.joinByCodeBtn);
        if (joinCodeBtn != null) {
            joinCodeBtn.setOnClickListener(v ->
                    startActivity(new Intent(this, JoinByCodeActivity.class)));
        }

        pendingAdapter = new BattleAdapter(pendingBattles, this::openBattle);
        pendingRV.setLayoutManager(new LinearLayoutManager(this));
        pendingRV.setAdapter(pendingAdapter);

        historyAdapter = new BattleAdapter(historyBattles, this::openBattle);
        historyRV.setLayoutManager(new LinearLayoutManager(this));
        historyRV.setAdapter(historyAdapter);

        challengeBtn.setOnClickListener(v -> sendChallenge());
        lessonDropdown.setOnClickListener(v -> {
            if (lessonNames.isEmpty()) loadLessons();
        });

        swipeRefresh.setOnRefreshListener(() -> {
            loadPending();
            loadHistory();
        });

        loadLessons();
        loadPending();
        loadHistory();

        handleDeepLinkBattle(getIntent());
    }

    // Fixes the notification-tap bug: FcmService/SplashActivity both launch
    // this Activity with a "battle_id" extra when a challenge push is tapped,
    // but that extra was never read - the user had to manually hunt through
    // pendingRV. Now it's read and routed straight to the right screen/state.
    private void handleDeepLinkBattle(Intent intent) {
        int battleId = intent != null ? intent.getIntExtra("battle_id", 0) : 0;
        if (battleId <= 0) return;

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        if (!new UConfig(this).isConnected()) {
            new UConfig(this).isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
            return;
        }

        ApiConfig.getRequest(this, Constant.BATTLE_BASE + battleId, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    runOnUiThread(() -> Toast.makeText(this, "😕 এই battle খুঁজে পাওয়া যায়নি", Toast.LENGTH_SHORT).show());
                    return;
                }
                JSONObject battle = r.optJSONObject("battle");
                if (battle == null) return;

                String status   = battle.optString("status", "");
                int lessonId    = battle.optInt("lesson_id", 0);
                int questionCnt = battle.optInt("question_count", 10);
                int lives       = battle.optInt("lives", 0);
                JSONArray wordIdsArr = battle.optJSONArray("question_word_ids");

                runOnUiThread(() -> {
                    if ("challenger_done".equals(status) || isMyTurnInMultiBattle(battle)) {
                        Intent qi = new Intent(this, QuizActivity.class);
                        qi.putExtra("lesson_id", lessonId);
                        qi.putExtra("battle_id", battleId);
                        qi.putExtra("question_count", questionCnt);
                        qi.putExtra("lives", lives);
                        if (wordIdsArr != null && wordIdsArr.length() > 0) {
                            qi.putExtra("word_ids", joinIntArray(wordIdsArr));
                        }
                        startActivity(qi);
                    } else if ("completed".equals(status)) {
                        Toast.makeText(this, "🏁 এই battle শেষ হয়ে গেছে — নিচে ফলাফল দেখো", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "⏳ এখনো তোমার পালা আসেনি, একটু অপেক্ষা করো", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception ignored) {}
        }, error -> runOnUiThread(() ->
                Toast.makeText(this, "নেটওয়ার্ক সমস্যা, আবার চেষ্টা করো", Toast.LENGTH_SHORT).show()));
    }

    // "participants" only exists on new-shape (customize) battles; legacy
    // 2-player battles are handled by the "challenger_done" check above.
    private boolean isMyTurnInMultiBattle(JSONObject battle) {
        JSONArray participants = battle.optJSONArray("participants");
        if (participants == null) return false;
        for (int i = 0; i < participants.length(); i++) {
            JSONObject p = participants.optJSONObject(i);
            if (p != null && p.optBoolean("is_me", false)) {
                return !"submitted".equals(p.optString("status", ""));
            }
        }
        return false;
    }

    private String joinIntArray(JSONArray arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(arr.optInt(i));
        }
        return sb.toString();
    }

    private void loadLessons() {
        String url = Constant.ROOT_API2 + "lessons";
        ApiConfig.RequestToVolley((result, response, error) -> {
            if (!result) {
                runOnUiThread(() -> {
                    lessonDropdown.setText("লোড ব্যর্থ — আবার চেষ্টা করতে ট্যাপ করো", false);
                    android.widget.Toast.makeText(this, "লেসন লোড করা যায়নি, ইন্টারনেট চেক করো", android.widget.Toast.LENGTH_SHORT).show();
                });
                return;
            }
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
                            R.layout.item_dropdown_text, lessonNames);
                    lessonDropdown.setAdapter(adapter);
                    lessonDropdown.setOnItemClickListener((parent, view, position, id) ->
                            selectedLessonId = lessonIds.get(position));
                    if (!lessonNames.isEmpty()) lessonDropdown.setText("", false);
                });
            } catch (Exception ignored) {}
        }, com.android.volley.Request.Method.GET, this, url, new java.util.HashMap<>(), false);
    }

    private void sendChallenge() {
        String code = friendCodeET.getText() != null ? friendCodeET.getText().toString().trim() : "";

        if (code.isEmpty()) { showError("বন্ধুর Friend Code দাও"); return; }
        if (selectedLessonId == 0) { showError("একটা Lesson বেছে নাও"); return; }

        UConfig uConfig = new UConfig(this);
        String token = uConfig.getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showError("লগইন করো আগে"); return; }

        if (!uConfig.isConnected()) {
            uConfig.isConnectedAlert("ইন্টারনেট সংযোগ নেই", "এই ফিচারটি ব্যবহার করতে ইন্টারনেট সংযোগ প্রয়োজন");
            return;
        }

        showError("");
        challengeBtn.setEnabled(false);

        // Resolve the friend code to a user id first, same lookup used by
        // LiptoTransferActivity/CreateChallengeActivity, then send the challenge.
        String url = Constant.GAME_LIPTO_FIND_FRIEND + "?code=" + code;
        ApiConfig.getRequest(this, url, token, findResponse -> {
            try {
                JSONObject r = new JSONObject(findResponse);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    runOnUiThread(() -> challengeBtn.setEnabled(true));
                    showError(r.optString("message", "খুঁজে পাওয়া যায়নি"));
                    return;
                }
                JSONObject user = r.optJSONObject("user");
                if (user == null) {
                    runOnUiThread(() -> challengeBtn.setEnabled(true));
                    showError("খুঁজে পাওয়া যায়নি");
                    return;
                }
                if (user.optBoolean("is_self", false)) {
                    runOnUiThread(() -> challengeBtn.setEnabled(true));
                    showError("নিজেকে চ্যালেঞ্জ করা যাবে না");
                    return;
                }
                submitChallenge(user.optInt("id", 0), token);
            } catch (Exception e) {
                runOnUiThread(() -> challengeBtn.setEnabled(true));
                showError("সমস্যা হয়েছে");
            }
        }, error -> {
            runOnUiThread(() -> challengeBtn.setEnabled(true));
            showError("খুঁজে পাওয়া যায়নি");
        });
    }

    private void submitChallenge(int opponentId, String token) {
        JSONObject body = new JSONObject();
        try {
            body.put("opponent_id", opponentId);
            body.put("lesson_id", selectedLessonId);
        } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.BATTLE_CHALLENGE, body, token, response -> {
            runOnUiThread(() -> challengeBtn.setEnabled(true));
            try {
                JSONObject r = new JSONObject(response);
                if (Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    showError("");
                    playTone(ToneGenerator.TONE_PROP_ACK);
                    runOnUiThread(() -> {
                        friendCodeET.setText("");
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
        }, error -> {
            runOnUiThread(() -> challengeBtn.setEnabled(true));
            String message = error.getMessage();
            showError(message != null ? message : "নেটওয়ার্ক সমস্যা");
        });
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

    // Same mute toggle QuizActivity's sound effects respect.
    private void playTone(int tone) {
        boolean soundEnabled = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean(Constant.SOUND_ENABLED, true);
        if (!soundEnabled || toneGenerator == null) return;
        try {
            toneGenerator.startTone(tone, 200);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}
