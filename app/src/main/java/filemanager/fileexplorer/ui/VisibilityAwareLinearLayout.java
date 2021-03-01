

package filemanager.fileexplorer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class VisibilityAwareLinearLayout extends LinearLayout {

    private int mUserSetVisibility;

    public VisibilityAwareLinearLayout(Context context) {
        this(context, null);
    }

    public VisibilityAwareLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisibilityAwareLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUserSetVisibility = getVisibility();
    }

    @Override
    public void setVisibility(int visibility) {
        internalSetVisibility(visibility, true);
    }

    final void internalSetVisibility(int visibility, boolean fromUser) {
        super.setVisibility(visibility);
        if (fromUser) {
            mUserSetVisibility = visibility;
        }
    }

    public final int getUserSetVisibility() {
        return mUserSetVisibility;
    }
}
