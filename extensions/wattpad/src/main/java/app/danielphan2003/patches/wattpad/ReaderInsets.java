package app.danielphan2003.patches.wattpad;

import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

/** Used by the "Keep reader size steady" patch. */
@SuppressWarnings("unused")
public final class ReaderInsets {
    private ReaderInsets() {
    }

    /**
     * The reader's root view fits the system windows, so its height follows every change in the
     * visible system bars, and paging mode lays the whole part out again on each change. Pad it with
     * insets that do not change while reading: the status bar only when it is meant to be shown, and
     * never the navigation bar, which is then drawn over the reader's own bottom margin.
     */
    public static void keepReaderHeightSteady(Window window, boolean showStatusBar) {
        View decor = window.getDecorView();
        int id = decor.getResources().getIdentifier("wattpad_activity_root_container", "id", decor.getContext().getPackageName());
        View root = id == 0 ? null : decor.findViewById(id);
        if (root == null) {
            return;
        }
        int barId = decor.getResources().getIdentifier("reader_bottom_bar", "id", decor.getContext().getPackageName());
        View bottomBar = barId == 0 ? null : root.findViewById(barId);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            liftBottomBar(bottomBar, barId, insets);
            int top = 0;
            int left;
            int right;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                int sides = WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout();
                android.graphics.Insets stable = insets.getInsetsIgnoringVisibility(sides);
                left = stable.left;
                right = stable.right;
                if (showStatusBar) {
                    top = insets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars() | WindowInsets.Type.displayCutout()).top;
                }
                view.setPadding(left, top, right, 0);
                return WindowInsets.CONSUMED;
            }
            left = insets.getSystemWindowInsetLeft();
            right = insets.getSystemWindowInsetRight();
            if (showStatusBar) {
                top = insets.getSystemWindowInsetTop();
            }
            view.setPadding(left, top, right, 0);
            return insets.consumeSystemWindowInsets();
        });
        root.requestApplyInsets();
    }

    /**
     * The reading menu's bottom bar used to sit above the navigation bar because the whole reader
     * shrank. Pad just the bar by the navigation bar that is showing, so its buttons stay above the
     * navigation pill and its background reaches the bottom of the screen.
     */
    private static void liftBottomBar(View bar, int tagKey, WindowInsets insets) {
        if (bar == null) {
            return;
        }
        Object tag = bar.getTag(tagKey);
        int basePadding;
        if (tag instanceof Integer) {
            basePadding = (Integer) tag;
        } else {
            basePadding = bar.getPaddingBottom();
            bar.setTag(tagKey, basePadding);
        }
        int navigation = android.os.Build.VERSION.SDK_INT >= 30
            ? insets.getInsets(WindowInsets.Type.navigationBars()).bottom
            : insets.getSystemWindowInsetBottom();
        bar.setPadding(bar.getPaddingLeft(), bar.getPaddingTop(), bar.getPaddingRight(), basePadding + navigation);
    }
}
