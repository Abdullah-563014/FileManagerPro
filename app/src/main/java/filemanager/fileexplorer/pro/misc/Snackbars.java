

package filemanager.fileexplorer.pro.misc;

import android.app.Activity;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import filemanager.fileexplorer.pro.R;

public final class Snackbars {
    private Snackbars() {}

    public static final Snackbar makeSnackbar(Activity activity, int messageId, int duration) {
        return Snackbars.makeSnackbar(
                activity, activity.getResources().getText(messageId), duration);
    }

    public static final Snackbar makeSnackbar(
            Activity activity, CharSequence message, int duration) {
        final View view = activity.findViewById(R.id.content_view);
        return Snackbar.make(view, message, duration);
    }
}
