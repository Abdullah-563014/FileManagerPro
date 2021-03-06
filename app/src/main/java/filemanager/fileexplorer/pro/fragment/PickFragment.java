

package filemanager.fileexplorer.pro.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Locale;

import filemanager.fileexplorer.pro.BaseActivity;
import filemanager.fileexplorer.pro.R;
import filemanager.fileexplorer.pro.common.BaseFragment;
import filemanager.fileexplorer.pro.model.DocumentInfo;

/**
 * Display pick confirmation bar, usually for selecting a directory.
 */
public class PickFragment extends BaseFragment {
    public static final String TAG = "PickFragment";

    private DocumentInfo mPickTarget;

    private View mContainer;
    private Button mPick;

    public static void show(FragmentManager fm) {
        final PickFragment fragment = new PickFragment();

        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container_save, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static PickFragment get(FragmentManager fm) {
        return (PickFragment) fm.findFragmentByTag(TAG);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContainer = inflater.inflate(R.layout.fragment_pick, container, false);

        mPick = (Button) mContainer.findViewById(android.R.id.button1);
        mPick.setOnClickListener(mPickListener);

        setPickTarget(null, null);

        return mContainer;
    }

    private View.OnClickListener mPickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final BaseActivity activity = BaseActivity.get(PickFragment.this);
            activity.onPickRequested(mPickTarget);
        }
    };

    public void setPickTarget(DocumentInfo pickTarget, CharSequence displayName) {
        mPickTarget = pickTarget;

        if (mContainer != null) {
            if (mPickTarget != null) {
                mContainer.setVisibility(View.VISIBLE);
                final Locale locale = getResources().getConfiguration().locale;
                final String raw = getString(R.string.menu_select).toUpperCase(locale);
                mPick.setText(TextUtils.expandTemplate(raw, displayName));
            } else {
                mContainer.setVisibility(View.GONE);
            }
        }
    }
}
