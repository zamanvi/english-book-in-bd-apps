package com.abmn.englishhub.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
    private boolean storybook;
    private TextToSpeechHelper ttsHelper;

    public WordAdapter(List<WordModel> wordList, Activity activity) {
        this.wordList = wordList;
        this.activity = activity;
        UConfig uConfig = new UConfig(activity);
        ttsHelper = new TextToSpeechHelper(activity, uConfig.getData(Constant.VOICE_SPEED));
    }

    public void refreshTtsSpeed() {
        UConfig uConfig = new UConfig(activity);
        ttsHelper = new TextToSpeechHelper(activity, uConfig.getData(Constant.VOICE_SPEED));
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

    @SuppressLint("NotifyDataSetChanged")
    public void setStorybook(boolean storybook) {
        this.storybook = storybook;
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
        holder.bind(word, wordList, position, isBWord, isBMeaning, storybook, ttsHelper);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView counterTV, wordTV, meaningTV, synonymsTV, antonymsTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            counterTV = itemView.findViewById(R.id.counterTV);
            wordTV = itemView.findViewById(R.id.wordTV);
            meaningTV = itemView.findViewById(R.id.meaningTV);
            synonymsTV = itemView.findViewById(R.id.synonymsTV);
            antonymsTV = itemView.findViewById(R.id.antonymsTV);
        }

        @SuppressLint("SetTextI18n")
        public void bind(WordModel model, List<WordModel> wordList, int position, boolean isBWord, boolean isBMeaning, boolean storybook, TextToSpeechHelper tts) {
            wordTV.setText(model.getWord());
            meaningTV.setText(model.getMeaning());

            rootCV.setCardBackgroundColor(itemView.getResources().getColor(
                    storybook ? R.color.storybook_card_bg : R.color.bg_elevated));
            wordTV.setTextColor(itemView.getResources().getColor(
                    storybook ? R.color.gold : R.color.text_primary));

            counterTV.setText("" + (position + 1));

            String syn = model.getSynonyms();
            String ant = model.getAntonyms();

            synonymsTV.setVisibility(View.VISIBLE);
            synonymsTV.setText((syn == null || "null".equals(syn)) ? "—" : syn);

            antonymsTV.setVisibility(View.VISIBLE);
            antonymsTV.setText((ant == null || "null".equals(ant)) ? "—" : ant);

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

            wordTV.setOnClickListener(v -> play(wordTV.getContext(), model.getWord(), wordTV, tts));
            synonymsTV.setOnClickListener(v -> play(synonymsTV.getContext(), model.getSynonyms(), synonymsTV, tts));
            antonymsTV.setOnClickListener(v -> play(antonymsTV.getContext(), model.getAntonyms(), antonymsTV, tts));
        }

        private void play(Context context, String word, TextView textView, TextToSpeechHelper tts) {
            if (word == null || word.isEmpty() || "null".equals(word) || "—".equals(word)) return;
            int defaultColor = textView.getCurrentTextColor();
            textView.setTextColor(context.getResources().getColor(R.color.indigo, null));
            int estimatedDuration = word.split(" ").length * 500;
            tts.speak(word);
            new Handler(Looper.getMainLooper()).postDelayed(() -> textView.setTextColor(defaultColor), estimatedDuration);
        }
    }
}