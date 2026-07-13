package com.abmn.englishhub.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.R;

import org.json.JSONObject;

import java.util.List;

public class LiptoTransactionAdapter extends RecyclerView.Adapter<LiptoTransactionAdapter.ViewHolder> {

    private final List<JSONObject> transactions;

    public LiptoTransactionAdapter(List<JSONObject> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lipto_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        JSONObject t = transactions.get(position);
        int amount = t.optInt("amount", 0);
        String type = t.optString("type", "");
        boolean positive = amount >= 0;
        int color = h.itemView.getResources().getColor(positive ? R.color.teal : R.color.red_wrong);

        h.iconTV.setText(positive ? "⬆" : "⬇");
        h.iconTV.setTextColor(color);

        String desc = t.optString("description", "");
        if (desc.isEmpty() || "null".equals(desc)) {
            desc = defaultDescription(type);
        }
        h.descTV.setText(desc);
        h.dateTV.setText(formatDate(t.optString("created_at", "")));

        h.amountTV.setText((positive ? "+" : "") + amount);
        h.amountTV.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private String defaultDescription(String type) {
        switch (type) {
            case "earn": return "Lipto অর্জন";
            case "spend": return "Lipto খরচ";
            case "transfer_in": return "বন্ধুর কাছ থেকে Lipto";
            case "transfer_out": return "বন্ধুকে Lipto পাঠানো";
            default: return "লেনদেন";
        }
    }

    private static final String[] MONTHS = {
            "", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // "2026-07-13T08:46:12.000000Z" -> "13 Jul, 08:46"
    private String formatDate(String raw) {
        if (raw == null || raw.length() < 16) return "";
        try {
            String[] ymd = raw.substring(0, 10).split("-");
            String time = raw.substring(11, 16);
            int month = Integer.parseInt(ymd[1]);
            return ymd[2] + " " + MONTHS[month] + ", " + time;
        } catch (Exception e) {
            return "";
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView iconTV, descTV, dateTV, amountTV;

        public ViewHolder(@NonNull View v) {
            super(v);
            iconTV = v.findViewById(R.id.txnIconTV);
            descTV = v.findViewById(R.id.txnDescTV);
            dateTV = v.findViewById(R.id.txnDateTV);
            amountTV = v.findViewById(R.id.txnAmountTV);
        }
    }
}
