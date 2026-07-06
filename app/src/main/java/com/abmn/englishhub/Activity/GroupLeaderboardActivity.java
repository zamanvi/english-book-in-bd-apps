package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.LeaderboardAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GroupLeaderboardActivity extends AppCompatActivity {

    private TextView groupNameTV, groupCodeTV;
    private RecyclerView leaderboardRV;
    private final List<JSONObject> members = new ArrayList<>();
    private LeaderboardAdapter adapter;
    private String groupCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_leaderboard);

        groupCode = getIntent().getStringExtra(Constant.CODE);
        String groupName = getIntent().getStringExtra("group_name");

        groupNameTV = findViewById(R.id.groupNameTV);
        groupCodeTV = findViewById(R.id.groupCodeTV);
        leaderboardRV = findViewById(R.id.leaderboardRV);

        if (groupName != null) groupNameTV.setText(groupName);
        if (groupCode != null) groupCodeTV.setText("কোড: " + groupCode);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        findViewById(R.id.shareCodeBtn).setOnClickListener(v -> shareCode());

        adapter = new LeaderboardAdapter(members);
        leaderboardRV.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRV.setAdapter(adapter);

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty() || groupCode == null) return;

        String url = Constant.GROUP_LEADERBOARD + groupCode + "/leaderboard";
        ApiConfig.getRequest(this, url, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                JSONArray arr = r.optJSONArray("members");
                if (arr == null) return;
                members.clear();
                for (int i = 0; i < arr.length(); i++) {
                    members.add(arr.getJSONObject(i));
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void shareCode() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String groupName = groupNameTV.getText().toString();
        intent.putExtra(Intent.EXTRA_TEXT,
                "আমার সাথে Learnify-তে \"" + groupName + "\" গ্রুপে যোগ দাও!\n" +
                "কোড: " + groupCode + "\n" +
                "ডাউনলোড করো: https://play.google.com/store/apps/details?id=com.abmn.englishhub");
        startActivity(Intent.createChooser(intent, "শেয়ার করো"));
    }

}
