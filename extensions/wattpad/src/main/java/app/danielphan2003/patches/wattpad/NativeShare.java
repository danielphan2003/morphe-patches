package app.danielphan2003.patches.wattpad;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.Method;

/** Used by the "Use Android share sheet" patch. */
@SuppressWarnings("unused")
public final class NativeShare {
    private static final String TAG = "NativeShare";

    private NativeShare() {
    }

    /**
     * Shares the text Wattpad would send to any other app through Android's share sheet.
     * Building the share link can ask Wattpad's server for a short link, so it runs off the main
     * thread, like Wattpad's own share code does.
     */
    public static void share(Dialog dialog, Object shareable, Object action, Object medium, Object campaign) {
        Context context = dialog.getContext();
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String title = null;
            String message = null;
            try {
                title = (String) find(shareable, "getShareTitle", 2).invoke(shareable, action, medium);
                message = (String) find(shareable, "getShareMessage", 3).invoke(shareable, action, medium, campaign);
            } catch (Throwable throwable) {
                Log.e(TAG, "Could not build the share message", throwable);
            }
            if (message == null || message.isEmpty()) {
                return;
            }
            String finalTitle = title;
            String finalMessage = stripTracking(message);
            main.post(() -> open(context, finalTitle, finalMessage));
        }, "MorpheNativeShare").start();
    }

    /**
     * Wattpad links carry utm_* campaign parameters and wp_uname, the sharer's username.
     * Story, part and profile links work without any query string, so drop it.
     */
    static String stripTracking(String message) {
        return message.replaceAll("(https?://(?:[a-z0-9-]+\\.)*wattpad\\.com/[^\\s?#]*)\\?[^\\s#]*", "$1");
    }

    private static Method find(Object target, String name, int parameterCount) throws NoSuchMethodException {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && method.getParameterTypes().length == parameterCount) {
                return method;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static void open(Context context, String title, String message) {
        Intent send = new Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, message);
        if (title != null && !title.isEmpty()) {
            send.putExtra(Intent.EXTRA_SUBJECT, title);
            send.putExtra(Intent.EXTRA_TITLE, title);
        }
        Intent chooser = Intent.createChooser(send, null);
        if (findActivity(context) == null) {
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(chooser);
        } catch (ActivityNotFoundException ignored) {
            // Nothing can receive text; there is nothing to show.
        }
    }

    private static Activity findActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
