package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.GroupAdapter;
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

public class GroupActivity extends AppCompatActivity {

    private CardView tabCreateCV, tabJoinCV;
    private LinearLayout createPanel, joinPanel;
    private TextInputEditText groupNameET, groupCodeET;
    private MaterialButton createGroupBtn, joinGroupBtn;
    private TextView groupErrorTV;
    private RecyclerView groupsRV;

    private final List<JSONObject> groupList = new ArrayList<>();
    private GroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_group);

        bindViews();
        setupTabs();
        setupButtons();

        adapter = new GroupAdapter(groupList, this);
        groupsRV.setLayoutManager(new LinearLayoutManager(this));
        groupsRV.setAdapter(adapter);

        loadMyGroups();
    }

    private void bindViews() {
        tabCreateCV   = findViewById(R.id.tabCreateCV);
        tabJoinCV     = findViewById(R.id.tabJoinCV);
        createPanel   = findViewById(R.id.createPanel);
        joinPanel     = findViewById(R.id.joinPanel);
        groupNameET   = findViewById(R.id.groupNameET);
        groupCodeET   = findViewById(R.id.groupCodeET);
        createGroupBtn= findViewById(R.id.createGroupBtn);
        joinGroupBtn  = findViewById(R.id.joinGroupBtn);
        groupErrorTV  = findViewById(R.id.groupErrorTV);
        groupsRV      = findViewById(R.id.groupsRV);
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabCreateCV.setOnClickListener(v -> {
            tabCreateCV.setCardBackgroundColor(getResources().getColor(R.color.indigo));
            tabJoinCV.setCardBackgroundColor(getResources().getColor(R.color.bg_card));
            createPanel.setVisibility(View.VISIBLE);
            joinPanel.setVisibility(View.GONE);
        });

        tabJoinCV.setOnClickListener(v -> {
            tabJoinCV.setCardBackgroundColor(getResources().getColor(R.color.indigo));
            tabCreateCV.setCardBackgroundColor(getResources().getColor(R.color.bg_card));
            joinPanel.setVisibility(View.VISIBLE);
            createPanel.setVisibility(View.GONE);
        });
    }

    private void setupButtons() {
        createGroupBtn.setOnClickListener(v -> {
            String name = groupNameET.getText() != null ? groupNameET.getText().toString().trim() : "";
            if (name.isEmpty()) { showError("গ্রুপের নাম দাও"); return; }
            createGroup(name);
        });

        joinGroupBtn.setOnClickListener(v -> {
            String code = groupCodeET.getText() != null ? groupCodeET.getText().toString().trim() : "";
            if (code.length() != 8) { showError("৮ অক্ষরের কোড দাও"); return; }
            joinGroup(code);
        });
    }

    private void createGroup(String name) {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showError("লগইন করো আগে"); return; }

        JSONObject body = new JSONObject();
        try { body.put("name", name); } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GROUP_CREATE, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if ("success".equals(r.optString("status"))) {
                    groupNameET.setText("");
                    groupList.add(0, r.getJSONObject("group"));
                    adapter.notifyItemInserted(0);
                    showError("");
                } else {
                    showError(r.optString("message", "সমস্যা হয়েছে"));
                }
            } catch (Exception e) { showError("সমস্যা হয়েছে"); }
        }, error -> showError("নেটওয়ার্ক সমস্যা"));
    }

    private void joinGroup(String code) {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) { showError("লগইন করো আগে"); return; }

        JSONObject body = new JSONObject();
        try { body.put("code", code.toUpperCase()); } catch (Exception ignored) {}

        ApiConfig.postRequest(this, Constant.GROUP_JOIN, body, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                if ("success".equals(r.optString("status"))) {
                    groupCodeET.setText("");
                    JSONObject g = r.getJSONObject("group");
                    boolean found = false;
                    for (JSONObject existing : groupList) {
                        if (existing.optString("code","").equals(g.optString("code",""))) {
                            found = true; break;
                        }
                    }
                    if (!found) {
                        groupList.add(0, g);
                        adapter.notifyItemInserted(0);
                    }
                    showError("");
                } else {
                    showError(r.optString("message", "ভুল কোড"));
                }
            } catch (Exception e) { showError("সমস্যা হয়েছে"); }
        }, error -> showError("নেটওয়ার্ক সমস্যা"));
    }

    private void loadMyGroups() {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) return;

        ApiConfig.getRequest(this, Constant.GROUP_MY, token, response -> {
            try {
                JSONObject r = new JSONObject(response);
                JSONArray arr = r.optJSONArray("groups");
                if (arr == null) return;
                groupList.clear();
                for (int i = 0; i < arr.length(); i++) {
                    groupList.add(arr.getJSONObject(i));
                }
                adapter.notifyDataSetChanged();
            } catch (Exception ignored) {}
        }, error -> {});
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            if (msg.isEmpty()) {
                groupErrorTV.setVisibility(View.GONE);
            } else {
                groupErrorTV.setText(msg);
                groupErrorTV.setVisibility(View.VISIBLE);
            }
        });
    }
}
