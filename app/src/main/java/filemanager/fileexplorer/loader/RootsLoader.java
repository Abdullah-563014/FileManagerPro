

package filemanager.fileexplorer.loader;

import android.content.Context;

import androidx.loader.content.AsyncTaskLoader;

import java.util.Collection;

import filemanager.fileexplorer.misc.RootsCache;
import filemanager.fileexplorer.model.RootInfo;
import filemanager.fileexplorer.BaseActivity;

public class RootsLoader extends AsyncTaskLoader<Collection<RootInfo>> {
    private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

    private final RootsCache mRoots;
    private final BaseActivity.State mState;

    private Collection<RootInfo> mResult;

    public RootsLoader(Context context, RootsCache roots, BaseActivity.State state) {
        super(context);
        mRoots = roots;
        mState = state;

        getContext().getContentResolver()
                .registerContentObserver(RootsCache.sNotificationUri, false, mObserver);
    }

    @Override
    public final Collection<RootInfo> loadInBackground() {
        return mRoots.getMatchingRootsBlocking(mState);
    }

    @Override
    public void deliverResult(Collection<RootInfo> result) {
        if (isReset()) {
            return;
        }

        mResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        }
        if (takeContentChanged() || mResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        mResult = null;

        getContext().getContentResolver().unregisterContentObserver(mObserver);
    }
}