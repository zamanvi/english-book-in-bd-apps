package com.abmn.englishhub.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Model.NoticeModel;
import com.abmn.englishhub.R;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.BlogViewHolder> {

    private final List<NoticeModel> arrayList;
    private final OnNoticeClickListener listener;

    public NoticeAdapter(List<NoticeModel> arrayList, OnNoticeClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
    }
    public interface OnNoticeClickListener {
        void onLinkClick(NoticeModel noticeModel);
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_notices, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        NoticeModel model = arrayList.get(position);
        holder.bind(model);
        holder.itemView.setOnClickListener(v -> listener.onLinkClick(model));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
    static class BlogViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTV;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.titleTV);
        }

        public void bind(NoticeModel model) {
            titleTV.setText(model.getTitle());
        }
    }
}