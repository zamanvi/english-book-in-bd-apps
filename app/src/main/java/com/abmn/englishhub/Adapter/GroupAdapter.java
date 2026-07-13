package com.abmn.englishhub.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Activity.GroupLeaderboardActivity;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.R;

import org.json.JSONObject;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private final List<JSONObject> groups;
    private final Activity activity;

    public GroupAdapter(List<JSONObject> groups, Activity activity) {
        this.groups   = groups;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        try {
            JSONObject g = groups.get(position);
            h.nameTV.setText(g.optString("name", "—"));
            h.codeTV.setText(g.optString("code", ""));
            h.memberTV.setText(g.optInt("member_count", 0) + " সদস্য");
            String code = g.optString("code", "");
            h.card.setOnClickListener(v -> {
                Intent i = new Intent(activity, GroupLeaderboardActivity.class);
                i.putExtra(Constant.CODE, code);
                i.putExtra(Constant.GROUP_NAME, g.optString("name", ""));
                activity.startActivity(i);
            });
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() { return groups.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView nameTV, codeTV, memberTV;
        ViewHolder(@NonNull View v) {
            super(v);
            card     = v.findViewById(R.id.groupItemCard);
            nameTV   = v.findViewById(R.id.groupItemNameTV);
            codeTV   = v.findViewById(R.id.groupItemCodeTV);
            memberTV = v.findViewById(R.id.groupItemMemberTV);
        }
    }
}
