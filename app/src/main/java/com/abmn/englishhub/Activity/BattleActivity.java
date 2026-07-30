package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BattleActivity extends AppCompatActivity {

    private TextView pendingEmptyTV, historyEmptyTV;
    private RecyclerView pendingRV, historyRV;
    private SwipeRefreshLayout swipeRefresh;

    private final List<JSONObject> pendingBattles = new ArrayList<>();
    private final List<JSONObject> historyBattles = new ArrayList<>();
    private BattleAdapter pendingAdapter, historyAdapter;

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

        setContentView(R.layout.activity_battle);

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

        swipeRefresh.setOnRefreshListener(() -> {
            loadPending();
            loadHistory();
        });

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
}
