package com.abmn.englishhub.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.abmn.englishhub.Activity.BookActivity;
import com.abmn.englishhub.Activity.VocabularyActivity;
import com.abmn.englishhub.R;

public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        define(view);

        return view;
    }

    private void define(View view) {

        Activity activity = getActivity();

        CardView menuBookCV = view.findViewById(R.id.menuBookCV);
        CardView menuVocabularyCV = view.findViewById(R.id.menuVocabularyCV);
        CardView menuShareCV = view.findViewById(R.id.menuShareCV);
        CardView menuOtherAppsCV = view.findViewById(R.id.menuOtherAppsCV);
        CardView menuReviewCV = view.findViewById(R.id.menuReviewCV);
        CardView menuContactCV = view.findViewById(R.id.menuContactCV);

        menuBookCV.setOnClickListener(v -> startActivity(new Intent(activity, BookActivity.class)));
        menuVocabularyCV.setOnClickListener(v -> startActivity(new Intent(activity, VocabularyActivity.class)));
        menuShareCV.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            assert activity != null;
            String shareMessage = getString(R.string.vocabulary_https_play_google_com_store_apps_details_id) + activity.getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
        menuOtherAppsCV.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=MD+,+norozzaman")));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, "Unable to open store", Toast.LENGTH_SHORT).show();
            }
        });
        menuReviewCV.setOnClickListener(v -> {
            try {
                assert activity != null;
                Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
        });
        menuContactCV.setOnClickListener(v -> {
            assert activity != null;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Contact Us").setMessage("norozzaman996@gmail.com").setPositiveButton("Copy", (dialog, which) -> {
                Context context = activity.getApplicationContext(); // Use application context
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Email", "norozzaman996@gmail.com");
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Email copied to clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Clipboard not available", Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setCancelable(true).show();
        });
    }
}