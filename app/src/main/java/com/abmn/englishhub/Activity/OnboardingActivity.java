package com.abmn.englishhub.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.abmn.englishhub.R;
import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    private static final String[][] PAGES = {
        {"📚", "প্রতিদিন শেখো", "Grammar, Vocabulary, Speaking —\nসব কিছু এক জায়গায়।"},
        {"🔥", "Streak ধরে রাখো", "প্রতিদিন quiz দাও, XP জমাও,\nleaderboard এ উঠে আসো।"},
        {"⚔️", "বন্ধুকে চ্যালেঞ্জ করো", "গ্রুপ বানাও, 1v1 battle করো,\nনিজের ক্ষমতা প্রমাণ করো।"},
    };

    private ViewPager2 pager;
    private MaterialButton nextBtn;
    private LinearLayout dotsLayout;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        com.abmn.englishhub.Helper.WindowInsetsHelper.applyStatusBarInsetsAsMargin(
                findViewById(R.id.skipTV));

        pager      = findViewById(R.id.onboardingPager);
        nextBtn    = findViewById(R.id.onboardingNextBtn);
        dotsLayout = findViewById(R.id.dotsLayout);

        pager.setAdapter(new OnboardingAdapter());
        buildDots(0);

        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                currentPage = position;
                buildDots(position);
                nextBtn.setText(position == PAGES.length - 1 ? "শুরু করো" : "পরবর্তী");
            }
        });

        nextBtn.setOnClickListener(v -> {
            if (currentPage < PAGES.length - 1) {
                pager.setCurrentItem(currentPage + 1);
            } else {
                finish();
            }
        });

        findViewById(R.id.skipTV).setOnClickListener(v -> finish());
    }

    @Override
    public void finish() {
        // Mark onboarding done
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit().putBoolean("onboarding_done", true).apply();
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void buildDots(int selected) {
        dotsLayout.removeAllViews();
        float dp = getResources().getDisplayMetrics().density;
        for (int i = 0; i < PAGES.length; i++) {
            CardView dot = new CardView(this);
            int size = (int) ((i == selected ? 10 : 6) * dp);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins((int)(4*dp), 0, (int)(4*dp), 0);
            dot.setLayoutParams(lp);
            dot.setRadius(size / 2f);
            dot.setCardElevation(0);
            dot.setCardBackgroundColor(i == selected
                    ? Color.parseColor("#8B7FFF")  // active: bright purple
                    : Color.parseColor("#5A5A7E")); // inactive: lighter gray-purple for better contrast
            dotsLayout.addView(dot);
        }
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            h.emojiTV.setText(PAGES[position][0]);
            h.titleTV.setText(PAGES[position][1]);
            h.descTV.setText(PAGES[position][2]);
        }

        @Override public int getItemCount() { return PAGES.length; }

        class VH extends RecyclerView.ViewHolder {
            TextView emojiTV, titleTV, descTV;
            VH(@NonNull View v) {
                super(v);
                emojiTV = v.findViewById(R.id.pageEmojiTV);
                titleTV = v.findViewById(R.id.pageTitleTV);
                descTV  = v.findViewById(R.id.pageDescTV);
            }
        }
    }
}
