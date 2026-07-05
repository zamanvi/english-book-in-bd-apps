package com.abmn.englishhub.Adapter;

import android.content.Context;
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

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<JSONObject> members;

    public LeaderboardAdapter(List<JSONObject> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        JSONObject m = members.get(position);
        int rank = m.optInt("rank", position + 1);
        h.rankTV.setText("#" + rank);
        h.nameTV.setText(m.optString("name", "—"));
        h.xpTV.setText(m.optInt("xp", 0) + " XP");

        boolean isMe = m.optBoolean("is_me", false);
        Context ctx = h.itemView.getContext();
        if (isMe) {
            ((CardView) h.itemView).setCardBackgroundColor(
                    ctx.getResources().getColor(R.color.bg_elevated));
            h.nameTV.setTextColor(ctx.getResources().getColor(R.color.indigo));
        } else {
            ((CardView) h.itemView).setCardBackgroundColor(
                    ctx.getResources().getColor(R.color.bg_card));
            h.nameTV.setTextColor(ctx.getResources().getColor(R.color.text_primary));
        }

        if (rank == 1) h.rankTV.setTextColor(Color.parseColor("#FFD700"));
        else if (rank == 2) h.rankTV.setTextColor(Color.parseColor("#C0C0C0"));
        else if (rank == 3) h.rankTV.setTextColor(Color.parseColor("#CD7F32"));
        else h.rankTV.setTextColor(ctx.getResources().getColor(R.color.text_inactive));
    }

    @Override
    public int getItemCount() { return members.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankTV, nameTV, xpTV;
        ViewHolder(@NonNull View v) {
            super(v);
            rankTV = v.findViewById(R.id.rankTV);
            nameTV = v.findViewById(R.id.memberNameTV);
            xpTV   = v.findViewById(R.id.memberXpTV);
        }
    }
}
