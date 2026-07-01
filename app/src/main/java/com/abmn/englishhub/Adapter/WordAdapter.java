package com.abmn.englishhub.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.R;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.WordModel;
import com.abmn.texttospeech.TextToSpeechHelper;
import com.abmn.utility.UConfig;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private final List<WordModel> wordList;
    private final Activity activity;
    private boolean isBWord = false, isBMeaning = false;

    public WordAdapter(List<WordModel> wordList, Activity activity) {
        this.wordList = wordList;
        this.activity = activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void blurWordText() {
        isBWord = true;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void removeBlurWordText() {
        isBWord = false;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void blurMeaningText() {
        isBMeaning = true;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void removeBlurMeaningText() {
        isBMeaning = false;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.lyt_item_word, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordModel word = wordList.get(position);
        holder.bind(word, wordList, position, isBWord, isBMeaning);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView counterTV, wordTV, meaningTV, synonymsTV, antonymsTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            counterTV = itemView.findViewById(R.id.counterTV);
            wordTV = itemView.findViewById(R.id.wordTV);
            meaningTV = itemView.findViewById(R.id.meaningTV);
            synonymsTV = itemView.findViewById(R.id.synonymsTV);
            antonymsTV = itemView.findViewById(R.id.antonymsTV);
        }

        @SuppressLint("SetTextI18n")
        public void bind(WordModel model, List<WordModel> wordList, int position, boolean isBWord, boolean isBMeaning) {
            wordTV.setText(model.getWord());
            meaningTV.setText(model.getMeaning());

            antonymsTV.setText(model.getAntonyms());
            counterTV.setText("" + (position + 1));

            boolean isAnySynonymNotNull = wordList.stream()
                    .anyMatch(word -> word.getSynonyms() != null && !"null".equals(word.getSynonyms()));

            boolean isAnyAntonymsNotNull = wordList.stream()
                    .anyMatch(word -> word.getAntonyms() != null && !"null".equals(word.getAntonyms()));

            if (isAnySynonymNotNull) {
                synonymsTV.setVisibility(View.VISIBLE);
                if (model.getSynonyms().equals("null"))
                    synonymsTV.setText("");
                else
                    synonymsTV.setText(model.getSynonyms());
            } else {
                synonymsTV.setVisibility(View.GONE);
            }

            if (isAnyAntonymsNotNull) {
                antonymsTV.setVisibility(View.VISIBLE);
                if (model.getAntonyms().equals("null"))
                    antonymsTV.setText("");
                else
                    antonymsTV.setText(model.getAntonyms());
            } else {
                antonymsTV.setVisibility(View.GONE);
            }

            if (isBWord) {
                wordTV.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                wordTV.getPaint().setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
            } else {
                wordTV.getPaint().setMaskFilter(null);
            }
            wordTV.invalidate();

            if (isBMeaning) {
                meaningTV.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                meaningTV.getPaint().setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
            } else {
                meaningTV.getPaint().setMaskFilter(null);
            }
            meaningTV.invalidate();

            wordTV.setOnClickListener(v -> {
                play(wordTV.getContext(), model.getWord(), wordTV);
            });
            synonymsTV.setOnClickListener(v -> play(synonymsTV.getContext(), model.getSynonyms(), synonymsTV));
            antonymsTV.setOnClickListener(v -> play(antonymsTV.getContext(), model.getAntonyms(), antonymsTV));
        }

        private void play(Context context, String word, TextView textView) {
            UConfig uConfig = new UConfig(context);
            TextToSpeechHelper ttsHelper = new TextToSpeechHelper(context, uConfig.getData(Constant.VOICE_SPEED));
            int defaultColor = textView.getCurrentTextColor();
            textView.setTextColor(Color.parseColor("#5A69FF"));
            int estimatedDuration = word.split(" ").length * 500;
            ttsHelper.speak(word);
            new Handler(Looper.getMainLooper()).postDelayed(() -> textView.setTextColor(defaultColor), estimatedDuration);
        }
    }
}