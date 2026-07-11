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

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.abmn.englishhub.R;
import com.abmn.englishhub.Helper.Constant;
import com.abmn.englishhub.Model.WordModel;
import com.abmn.texttospeech.TextToSpeechHelper;
import com.abmn.utility.UConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {
    private static final String NOTE_MARKER = "দ্রষ্টব্য";

    private final List<WordModel> wordList;
    private final Activity activity;
    private boolean isBWord = false, isBMeaning = false;
    private boolean storybook;
    private final Set<Integer> expandedNotePositions = new HashSet<>();
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
        boolean isNote = word.getWord() != null && word.getWord().contains(NOTE_MARKER);
        boolean expanded = expandedNotePositions.contains(position);
        holder.bind(word, position, isBWord, isBMeaning, storybook, isNote, expanded, ttsHelper, () -> {
            if (expandedNotePositions.contains(position)) {
                expandedNotePositions.remove(position);
            } else {
                expandedNotePositions.add(position);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public interface OnToggleListener {
        void onToggle();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CardView rootCV;
        private final TextView counterTV, wordTV, meaningTV, synonymsTV, antonymsTV;
        private final ImageView expandIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootCV = itemView.findViewById(R.id.rootCV);
            counterTV = itemView.findViewById(R.id.counterTV);
            wordTV = itemView.findViewById(R.id.wordTV);
            meaningTV = itemView.findViewById(R.id.meaningTV);
            synonymsTV = itemView.findViewById(R.id.synonymsTV);
            antonymsTV = itemView.findViewById(R.id.antonymsTV);
            expandIV = itemView.findViewById(R.id.expandIV);
        }

        @SuppressLint("SetTextI18n")
        public void bind(WordModel model, int position, boolean isBWord, boolean isBMeaning, boolean storybook,
                          boolean isNote, boolean expanded, TextToSpeechHelper tts, OnToggleListener onToggle) {
            wordTV.setText(model.getWord());
            meaningTV.setText(model.getMeaning());

            if (storybook) {
                rootCV.setCardBackgroundColor(android.graphics.Color.parseColor(isNote ? "#EFE6CC" : "#F7F1E1"));
                wordTV.setTextColor(android.graphics.Color.parseColor(isNote ? "#8B5E00" : "#7A4A00"));
                meaningTV.setTextColor(android.graphics.Color.parseColor("#3A2E1A"));
                synonymsTV.setTextColor(android.graphics.Color.parseColor("#1F7A5C"));
                antonymsTV.setTextColor(android.graphics.Color.parseColor("#B0203A"));
            } else {
                rootCV.setCardBackgroundColor(isNote
                        ? android.graphics.Color.parseColor("#2A2010")
                        : itemView.getResources().getColor(R.color.bg_elevated));
                wordTV.setTextColor(itemView.getResources().getColor(isNote ? R.color.gold : R.color.text_primary));
                meaningTV.setTextColor(itemView.getResources().getColor(R.color.text_secondary));
                synonymsTV.setTextColor(itemView.getResources().getColor(R.color.teal));
                antonymsTV.setTextColor(itemView.getResources().getColor(R.color.red_wrong));
            }

            counterTV.setText(isNote ? "📌" : "" + (position + 1));

            String meaning = model.getMeaning();
            String syn = model.getSynonyms();
            String ant = model.getAntonyms();

            boolean hasMeaning = meaning != null && !meaning.isEmpty() && !"null".equals(meaning);
            boolean hasSyn = syn != null && !syn.isEmpty() && !"null".equals(syn);
            boolean hasAnt = ant != null && !ant.isEmpty() && !"null".equals(ant);
            if (hasSyn) synonymsTV.setText(syn);
            if (hasAnt) antonymsTV.setText(ant);

            if (isNote) {
                expandIV.setVisibility(View.VISIBLE);
                expandIV.setRotation(expanded ? 90f : 0f);
                wordTV.setMaxLines(expanded ? 2 : 1);
                if (expanded) {
                    meaningTV.setMaxLines(Integer.MAX_VALUE);
                    meaningTV.setEllipsize(null);
                    meaningTV.setVisibility(hasMeaning ? View.VISIBLE : View.GONE);
                    synonymsTV.setVisibility(hasSyn ? View.VISIBLE : View.GONE);
                    antonymsTV.setVisibility(hasAnt ? View.VISIBLE : View.GONE);
                } else {
                    meaningTV.setVisibility(View.GONE);
                    synonymsTV.setVisibility(View.GONE);
                    antonymsTV.setVisibility(View.GONE);
                }
                rootCV.setOnClickListener(v -> onToggle.onToggle());
            } else {
                expandIV.setVisibility(View.GONE);
                wordTV.setMaxLines(2);
                meaningTV.setMaxLines(2);
                meaningTV.setEllipsize(android.text.TextUtils.TruncateAt.END);
                meaningTV.setVisibility(hasMeaning ? View.VISIBLE : View.GONE);
                synonymsTV.setVisibility(hasSyn ? View.VISIBLE : View.GONE);
                antonymsTV.setVisibility(hasAnt ? View.VISIBLE : View.GONE);
                rootCV.setOnClickListener(null);
                rootCV.setClickable(false);
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