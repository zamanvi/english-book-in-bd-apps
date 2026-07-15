package com.abmn.englishhub.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.R;

import org.json.JSONObject;

import java.util.List;

public class BattleAdapter extends RecyclerView.Adapter<BattleAdapter.ViewHolder> {

    private final List<JSONObject> battles;
    private final OnBattleClickListener listener;

    public interface OnBattleClickListener {
        void onBattleClick(JSONObject battle);
    }

    public BattleAdapter(List<JSONObject> battles, OnBattleClickListener listener) {
        this.battles  = battles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_battle, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        JSONObject b = battles.get(position);

        // "challenger"/"opponent" are raw DB roles - whichever one isn't me
        // depends on whether I sent or received this challenge, so always
        // read the server's already-perspective-adjusted "other" field
        // instead of guessing from those two.
        String other = b.optJSONObject("other") != null
                ? b.optJSONObject("other").optString("name", "?")
                : "?";
        h.opponentTV.setText(other);

        String status = b.optString("status", "pending");
        String result = b.optString("result", "");
        int myScore    = b.optInt("my_score", -1);
        int theirScore = b.optInt("their_score", -1);

        if ("completed".equals(status)) {
            String scoreText = (myScore >= 0 ? myScore : "?") + " - " + (theirScore >= 0 ? theirScore : "?");
            h.scoreTV.setText(scoreText);
            switch (result) {
                case "win":
                    h.badgeTV.setText("জয়");
                    ((CardView) h.badgeCV).setCardBackgroundColor(Color.parseColor("#1a4a1a"));
                    h.badgeTV.setTextColor(Color.parseColor("#4ade80"));
                    break;
                case "loss":
                    h.badgeTV.setText("হার");
                    ((CardView) h.badgeCV).setCardBackgroundColor(Color.parseColor("#4a1a1a"));
                    h.badgeTV.setTextColor(Color.parseColor("#f87171"));
                    break;
                default:
                    h.badgeTV.setText("ড্র");
                    h.badgeTV.setTextColor(Color.parseColor("#facc15"));
            }
            h.statusTV.setText("শেষ");
        } else {
            h.badgeTV.setText("⚔");
            h.scoreTV.setText("Lesson " + b.optInt("lesson_id", 0));
            h.statusTV.setText("অপেক্ষায়");
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onBattleClick(b); });
    }

    @Override
    public int getItemCount() { return battles.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View badgeCV;
        TextView badgeTV, opponentTV, scoreTV, statusTV;
        ViewHolder(@NonNull View v) {
            super(v);
            badgeCV    = v.findViewById(R.id.resultBadgeCV);
            badgeTV    = v.findViewById(R.id.resultBadgeTV);
            opponentTV = v.findViewById(R.id.battleOpponentTV);
            scoreTV    = v.findViewById(R.id.battleScoreTV);
            statusTV   = v.findViewById(R.id.battleStatusTV);
        }
    }
}
