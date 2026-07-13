package com.abmn.englishhub.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    // Podium views
    private TextView rank1NameTV, rank1XpTV;
    private TextView rank2NameTV, rank2XpTV;
    private TextView rank3NameTV, rank3XpTV;

    // My rank strip
    private CardView myRankCard;
    private TextView myNameTV, myXpTV, myRankTV;

    // List
    private RecyclerView leaderboardRV;
    private ProgressBar loadingBar;
    private GlobalLeaderboardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        bindViews();
        setupList();
        fetchLeaderboard();
    }

    private void bindViews() {
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        rank1NameTV = findViewById(R.id.rank1NameTV);
        rank1XpTV   = findViewById(R.id.rank1XpTV);
        rank2NameTV = findViewById(R.id.rank2NameTV);
        rank2XpTV   = findViewById(R.id.rank2XpTV);
        rank3NameTV = findViewById(R.id.rank3NameTV);
        rank3XpTV   = findViewById(R.id.rank3XpTV);

        myRankCard = findViewById(R.id.myRankCard);
        myNameTV   = findViewById(R.id.myNameTV);
        myXpTV     = findViewById(R.id.myXpTV);
        myRankTV   = findViewById(R.id.myRankTV);

        leaderboardRV = findViewById(R.id.leaderboardRV);
        loadingBar    = findViewById(R.id.loadingBar);
    }

    private void setupList() {
        adapter = new GlobalLeaderboardAdapter(this);
        leaderboardRV.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRV.setAdapter(adapter);
    }

    private void fetchLeaderboard() {
        ApiConfig.getRequest(this, Constant.GAME_LEADERBOARD, response -> {
            try {
                JSONObject json = new JSONObject(response);
                if (!json.optString(Constant.STATUS, "").equals(Constant.SUCCESS)) return;

                JSONArray list = json.optJSONArray(Constant.DATA);
                if (list == null) return;

                // Find current user by ID (name match is unreliable)
                UConfig uConfig = new UConfig(this);
                String myUserId = uConfig.getData(Constant.TOKEN) != null
                        ? uConfig.getData("user_id") : null;
                String myName = uConfig.getData("name");

                List<JSONObject> rows = new ArrayList<>();
                JSONObject myEntry = null;

                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    rows.add(entry);
                    boolean matchById   = myUserId != null
                            && String.valueOf(entry.optInt("id")).equals(myUserId);
                    boolean matchByName = myName != null
                            && myName.equalsIgnoreCase(entry.optString("name"));
                    if (matchById || matchByName) {
                        myEntry = entry;
                    }
                }

                final JSONObject finalMyEntry = myEntry;
                final List<JSONObject> finalRows = rows;

                runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);

                    // Podium top 3
                    fillPodium(finalRows);

                    // My rank strip
                    if (finalMyEntry != null) {
                        myNameTV.setText(finalMyEntry.optString("name", "You"));
                        myXpTV.setText(finalMyEntry.optInt("points", 0) + " XP");
                        myRankTV.setText("#" + finalMyEntry.optInt("rank", 0));
                        myRankCard.setVisibility(View.VISIBLE);
                    }

                    // List starts from rank 4
                    List<JSONObject> tail = finalRows.size() > 3
                            ? finalRows.subList(3, finalRows.size())
                            : new ArrayList<>();
                    adapter.setData(tail);
                });

            } catch (Exception e) {
                runOnUiThread(() -> loadingBar.setVisibility(View.GONE));
            }
        }, error -> runOnUiThread(() -> loadingBar.setVisibility(View.GONE)));
    }

    private void fillPodium(List<JSONObject> rows) {
        if (rows.size() >= 1) fillPodiumSlot(rows.get(0), rank1NameTV, rank1XpTV);
        if (rows.size() >= 2) fillPodiumSlot(rows.get(1), rank2NameTV, rank2XpTV);
        if (rows.size() >= 3) fillPodiumSlot(rows.get(2), rank3NameTV, rank3XpTV);
    }

    private void fillPodiumSlot(JSONObject entry, TextView nameTV, TextView xpTV) {
        try {
            String name = entry.optString("name", "—");
            // Shorten to first name
            if (name.contains(" ")) name = name.substring(0, name.indexOf(" "));
            nameTV.setText(name);
            xpTV.setText(entry.optInt("points", 0) + " XP");
        } catch (Exception ignored) {}
    }

    // ── Adapter ──────────────────────────────────────────────────
    // Named distinctly from Adapter/LeaderboardAdapter.java (used by
    // GroupLeaderboardActivity, reads "xp") since this one is the global
    // leaderboard and reads a different field ("points") - same simple name
    // on both was causing confusion during review.
    static class GlobalLeaderboardAdapter extends RecyclerView.Adapter<GlobalLeaderboardAdapter.VH> {

        private final Context context;
        private final List<JSONObject> data = new ArrayList<>();

        GlobalLeaderboardAdapter(Context context) { this.context = context; }

        void setData(List<JSONObject> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_leaderboard_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            try {
                JSONObject item = data.get(pos);
                int rank = item.optInt("rank", pos + 4);
                h.rankTV.setText(String.valueOf(rank));
                h.nameTV.setText(item.optString("name", "—"));
                h.xpTV.setText(item.optInt("points", 0) + " XP");

                // Highlight top 10 ranks with slightly brighter rank text
                if (rank <= 10) {
                    h.rankTV.setTextColor(ContextCompat.getColor(context, R.color.indigo));
                } else {
                    h.rankTV.setTextColor(ContextCompat.getColor(context, R.color.text_inactive));
                }
            } catch (Exception ignored) {}
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView rankTV, nameTV, xpTV;
            VH(View v) {
                super(v);
                rankTV = v.findViewById(R.id.rowRankTV);
                nameTV = v.findViewById(R.id.rowNameTV);
                xpTV   = v.findViewById(R.id.rowXpTV);
            }
        }
    }
}
