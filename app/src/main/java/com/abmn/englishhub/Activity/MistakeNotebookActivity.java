package com.abmn.englishhub.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Helper.MistakeNotebook;
import com.abmn.englishhub.Helper.AppTextToSpeechHelper;
import com.abmn.englishhub.R;
import com.abmn.utility.UConfig;

import java.util.ArrayList;
import java.util.List;

// Review screen for words missed across any Quick Quiz round (client-local
// store, see Helper/MistakeNotebook). Reached from LevelMapActivity's 📓
// button — a lightweight spaced-repetition aid, not a graded quiz.
public class MistakeNotebookActivity extends AppCompatActivity {

    private RecyclerView mistakeRV;
    private View emptyLL;
    private MistakeAdapter adapter;
    private AppTextToSpeechHelper ttsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mistake_notebook);

        UConfig uConfig = new UConfig(this);
        ttsHelper = new AppTextToSpeechHelper(this, uConfig.getData(Constant.VOICE_SPEED));

        mistakeRV = findViewById(R.id.mistakeRV);
        emptyLL   = findViewById(R.id.mistakeEmptyLL);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.clearBtn).setOnClickListener(v -> {
            MistakeNotebook.clear(this);
            render();
        });

        adapter = new MistakeAdapter(word -> ttsHelper.speak(word));
        mistakeRV.setLayoutManager(new LinearLayoutManager(this));
        mistakeRV.setAdapter(adapter);

        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A quiz round may have added new mistakes since this screen was
        // last shown (user navigated back and forth without recreating it).
        render();
    }

    private void render() {
        List<String[]> entries = MistakeNotebook.getAll(this);
        adapter.setData(entries);
        emptyLL.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        mistakeRV.setVisibility(entries.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper != null) ttsHelper.shutdown();
    }

    interface OnSpeak {
        void speak(String word);
    }

    static class MistakeAdapter extends RecyclerView.Adapter<MistakeAdapter.VH> {
        private final List<String[]> data = new ArrayList<>();
        private final OnSpeak onSpeak;

        MistakeAdapter(OnSpeak onSpeak) { this.onSpeak = onSpeak; }

        void setData(List<String[]> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mistake_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            String[] entry = data.get(pos);
            String word = entry[0];
            String meaning = entry[1];
            h.wordTV.setText(word);
            h.meaningTV.setText(meaning);
            h.speakBtn.setOnClickListener(v -> onSpeak.speak(word));
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView wordTV, meaningTV;
            CardView speakBtn;
            VH(View v) {
                super(v);
                wordTV = v.findViewById(R.id.rowWordTV);
                meaningTV = v.findViewById(R.id.rowMeaningTV);
                speakBtn = v.findViewById(R.id.rowSpeakBtn);
            }
        }
    }
}
