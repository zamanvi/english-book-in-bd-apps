package com.abmn.englishhub.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.abmn.englishhub.Activity.ItemDetailsActivity;
import com.abmn.englishhub.Adapter.NoticeAdapter;
import com.abmn.englishhub.Helper.ApiConfig;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.NoticeModel;
import com.abmn.englishhub.R;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NoticeFragment extends Fragment {

    private Activity activity;
    private RecyclerView noticeRV;
    private List<NoticeModel> noticeList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private int lastPage = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notice, container, false);

        define(view);

        return  view;
    }

    private void define(View view) {

        activity = getActivity();

        noticeRV = view.findViewById(R.id.noticeRV);

        LinearLayoutManager linearLayout = new LinearLayoutManager(activity);
        linearLayout.setReverseLayout(false);
        linearLayout.setOrientation(RecyclerView.VERTICAL);
        noticeRV.setLayoutManager(linearLayout);
        noticeRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    assert layoutManager != null;
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreData();
                    }
                }
            }
        });

        loadInitialData();
    }


    private void loadInitialData() {
        noticeList = new ArrayList<>();
        NoticeAdapter adapter = new NoticeAdapter(noticeList, this::onClick);
        noticeRV.setAdapter(adapter);
        fetchData(currentPage);
    }
    private void onClick(NoticeModel noticeModel) {

        if (noticeModel.getSlug().equals("null")) {
            Intent intent = new Intent(activity, ItemDetailsActivity.class);
            intent.putExtra(Constant.FROM, noticeModel.getSlug());
            startActivity(intent);
        }else {
            Toast.makeText(activity, "This Notification has not any action", Toast.LENGTH_SHORT).show();
        }
    }
    private void loadMoreData() {
        if (currentPage < lastPage) {
            isLoading = true;
            currentPage++;
            fetchData(currentPage);
        } else {
            isLoading = false;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchData(int page) {
        String url = Constant.ROOT_API + "notices/grammar?page=" + page;
        String tag = "NoticeModel";

        Log.d("url", url);

        ApiConfig.RequestToVolley((result, response, error) -> {
            try {
                Log.d("Notice", response);
                if (result) {
                    JSONObject chaptersObject = new JSONObject(response).getJSONObject("notices");
                    JSONArray dataArray = chaptersObject.getJSONArray(Constant.DATA);

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject notice = dataArray.getJSONObject(i);
                        int id = notice.getInt("id");
                        String title = notice.getString("title");
                        String app = notice.getString("app");
                        String body = notice.getString("body");
                        String slug = notice.getString("slug");

                        NoticeModel model = new NoticeModel(id, title, app, body, slug);
                        noticeList.add(model);
                    }
                    if (currentPage == 1) {
                        lastPage = chaptersObject.getInt("last_page");
                    }
                    Objects.requireNonNull(noticeRV.getAdapter()).notifyDataSetChanged();
                }
            } catch (Exception e) {
                Log.d(tag + "_exception", Objects.requireNonNull(e.getMessage()));
            } finally {
                isLoading = false;
            }
        }, Request.Method.GET, activity, url, new HashMap<>(), true);
    }
}