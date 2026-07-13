package com.abmn.englishhub.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Adapter.LiptoTransactionAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LiptoPassbookActivity extends AppCompatActivity {

    private TextView currentBalanceTV;
    private RecyclerView txnRV;
    private ProgressBar loadingPB;
    private LinearLayout emptyLL;
    private final List<JSONObject> transactions = new ArrayList<>();
    private LiptoTransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lipto_passbook);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        currentBalanceTV = findViewById(R.id.currentBalanceTV);
        txnRV = findViewById(R.id.txnRV);
        loadingPB = findViewById(R.id.loadingPB);
        emptyLL = findViewById(R.id.emptyLL);

        adapter = new LiptoTransactionAdapter(transactions);
        txnRV.setLayoutManager(new LinearLayoutManager(this));
        txnRV.setAdapter(adapter);

        loadPassbook();
    }

    private void loadPassbook() {
        String token = new UConfig(this).getData(Constant.TOKEN);
        if (token == null || token.isEmpty()) {
            loadingPB.setVisibility(View.GONE);
            emptyLL.setVisibility(View.VISIBLE);
            return;
        }

        ApiConfig.getRequest(this, Constant.GAME_LIPTO_BALANCE, token, response -> {
            boolean failed = false;
            int balance = 0;
            try {
                JSONObject r = new JSONObject(response);
                if (!Constant.SUCCESS.equals(r.optString(Constant.STATUS))) {
                    failed = true;
                } else {
                    balance = r.optInt("balance", 0);
                    JSONArray arr = r.optJSONArray("recent");
                    if (arr != null) {
                        transactions.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            transactions.add(arr.getJSONObject(i));
                        }
                    }
                }
            } catch (Exception e) {
                failed = true;
            }

            boolean finalFailed = failed;
            int finalBalance = balance;
            runOnUiThread(() -> {
                loadingPB.setVisibility(View.GONE);
                currentBalanceTV.setText("💎 " + finalBalance);
                adapter.notifyDataSetChanged();
                emptyLL.setVisibility(!finalFailed && !transactions.isEmpty() ? View.GONE : View.VISIBLE);
            });
        }, error -> runOnUiThread(() -> {
            loadingPB.setVisibility(View.GONE);
            emptyLL.setVisibility(View.VISIBLE);
        }));
    }
}
