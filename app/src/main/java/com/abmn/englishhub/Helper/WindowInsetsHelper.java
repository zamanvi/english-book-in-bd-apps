package com.abmn.englishhub.Helper;

import android.view.View;
import android.view.ViewGroup;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Helper to apply status-bar insets to any view.
 * Use in fragments/activities to prevent text from overlapping the system status bar.
 */
public class WindowInsetsHelper {

    /**
     * Apply status-bar top inset as padding to the given view.
     * Call this once in onCreateView() or onCreate() for each fragment/activity.
     *
     * @param view the root view to apply insets to
     */
    public static void applyStatusBarInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,      // <-- status bar height
                    systemBars.right,
                    v.getPaddingBottom() // preserve existing bottom padding
            );
            return insets;
        });
    }

    /**
     * Apply status-bar top inset as margin to the given view (for children of containers).
     * Useful when direct padding would affect the whole container.
     *
     * @param view the view to apply insets to
     */
    public static void applyStatusBarInsetsAsMargin(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (params != null) {
                params.topMargin = systemBars.top;
                v.setLayoutParams(params);
            }
            return insets;
        });
    }
}
