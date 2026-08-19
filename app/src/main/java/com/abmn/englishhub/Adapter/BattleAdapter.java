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

import org.json.JSONArray;
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

        // Two response shapes exist server-side (BattleController::formatBattle()
        // picks one per-battle): legacy 1v1 battles carry a top-level "other"
        // object + "my_score"/"their_score"; customize-flow battles (the only
        // kind creatable from the UI since the "one entry point" simplification)
        // carry a "participants" array instead and never send those legacy
        // fields at all - reading only the legacy fields is why this list used
        // to show "?" for every current battle's opponent/score.
        String other;
        int myScore, theirScore;
        JSONArray participants = b.optJSONArray("participants");
        if (participants != null) {
            JSONObject me = null, opponent = null;
            int otherCount = 0;
            for (int i = 0; i < participants.length(); i++) {
                JSONObject p = participants.optJSONObject(i);
                if (p == null) continue;
                if (p.optBoolean("is_me", false)) {
                    me = p;
                } else {
                    otherCount++;
                    if (opponent == null) opponent = p; // first other participant (1v1 case)
                }
            }
            String opponentName = opponent != null ? opponent.optString("name", "?") : "?";
            // Customize battles can have 3+ participants (multi-invite) - this
            // row only has room for one name, so name the first opponent and
            // flag how many more instead of silently hiding them.
            other = otherCount > 1 ? opponentName + " +" + (otherCount - 1) : opponentName;
            myScore    = me != null ? me.optInt("score", -1) : -1;
            theirScore = opponent != null ? opponent.optInt("score", -1) : -1;
        } else {
            other = b.optJSONObject("other") != null
                    ? b.optJSONObject("other").optString("name", "?")
                    : "?";
            myScore    = b.optInt("my_score", -1);
            theirScore = b.optInt("their_score", -1);
        }
        h.opponentTV.setText(other);

        String status = b.optString("status", "pending");
        String result = b.optString("result", "");

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
