

package filemanager.fileexplorer.loader;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;

import filemanager.fileexplorer.DocumentsApplication;
import filemanager.fileexplorer.cursor.FilteringCursorWrapper;
import filemanager.fileexplorer.cursor.RootCursorWrapper;
import filemanager.fileexplorer.cursor.SortingCursorWrapper;
import filemanager.fileexplorer.fragment.DirectoryFragment;
import filemanager.fileexplorer.libcore.io.IoUtils;
import filemanager.fileexplorer.misc.AsyncTaskLoader;
import filemanager.fileexplorer.misc.ContentProviderClientCompat;
import filemanager.fileexplorer.misc.ProviderExecutor;
import filemanager.fileexplorer.model.DirectoryResult;
import filemanager.fileexplorer.model.DocumentInfo;
import filemanager.fileexplorer.model.DocumentsContract;
import filemanager.fileexplorer.model.RootInfo;
import filemanager.fileexplorer.provider.RecentsProvider;

import filemanager.fileexplorer.BaseActivity;

public class DirectoryLoader extends AsyncTaskLoader<DirectoryResult> {

    private static final String[] SEARCH_REJECT_MIMES = new String[] { };//Document.MIME_TYPE_DIR };

    private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

    private final int mType;
    private final RootInfo mRoot;
    private DocumentInfo mDoc;
    private final Uri mUri;
    private final int mUserSortOrder;

    private CancellationSignal mSignal;
    private DirectoryResult mResult;

    public DirectoryLoader(Context context, int type, RootInfo root, DocumentInfo doc, Uri uri,
            int userSortOrder) {
        super(context, ProviderExecutor.forAuthority(root.authority));
        mType = type;
        mRoot = root;
        mDoc = doc;
        mUri = uri;
        mUserSortOrder = userSortOrder;
    }

    @Override
    public final DirectoryResult loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mSignal = new CancellationSignal();
        }

        final ContentResolver resolver = getContext().getContentResolver();
        final String authority = mUri.getAuthority();

        final DirectoryResult result = new DirectoryResult();

        int userMode = BaseActivity.State.MODE_UNKNOWN;
        int userSortOrder = BaseActivity.State.SORT_ORDER_UNKNOWN;

        // Use default document when searching
        if (mType == DirectoryFragment.TYPE_SEARCH) {
            final Uri docUri = DocumentsContract.buildDocumentUri(
                    mRoot.authority, mRoot.documentId);
            try {
                mDoc = DocumentInfo.fromUri(resolver, docUri);
            } catch (FileNotFoundException e) {
                Log.w(BaseActivity.TAG, "Failed to query", e);
                result.exception = e;

                return result;
            }
        }

        // Pick up any custom modes requested by user
        Cursor cursor = null;
        try {
            final Uri stateUri = RecentsProvider.buildState(
                    mRoot.authority, mRoot.rootId, mDoc.documentId);
            cursor = resolver.query(stateUri, null, null, null, null);
            if (null != cursor && cursor.moveToFirst()) {
                userMode = DocumentInfo.getCursorInt(cursor, RecentsProvider.StateColumns.MODE);
                userSortOrder = DocumentInfo.getCursorInt(cursor, RecentsProvider.StateColumns.SORT_ORDER);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
        }

        if (userMode != BaseActivity.State.MODE_UNKNOWN) {
            result.mode = userMode;
        } else {
            if ((mDoc.flags & DocumentsContract.Document.FLAG_DIR_PREFERS_GRID) != 0) {
                result.mode = BaseActivity.State.MODE_GRID;
            } else {
                result.mode = BaseActivity.State.MODE_LIST;
            }
        }

        if (userSortOrder != BaseActivity.State.SORT_ORDER_UNKNOWN) {
            result.sortOrder = userSortOrder;
        } else {
            if ((mDoc.flags & DocumentsContract.Document.FLAG_DIR_PREFERS_LAST_MODIFIED) != 0) {
                result.sortOrder = BaseActivity.State.SORT_ORDER_LAST_MODIFIED;
            } else {
                result.sortOrder = BaseActivity.State.SORT_ORDER_DISPLAY_NAME;
            }
        }

        // Search always uses ranking from provider
        if (mType == DirectoryFragment.TYPE_SEARCH) {
            //result.sortOrder = State.SORT_ORDER_UNKNOWN;
        }

        Log.d(BaseActivity.TAG, "userMode=" + userMode + ", userSortOrder=" + userSortOrder + " --> mode="
                + result.mode + ", sortOrder=" + result.sortOrder);

        ContentProviderClient client = null;
        try {
            client = DocumentsApplication.acquireUnstableProviderOrThrow(resolver, authority);

            cursor = client.query(
                    mUri, null, null, null, getQuerySortOrder(result.sortOrder));
            cursor.registerContentObserver(mObserver);

            cursor = new RootCursorWrapper(mUri.getAuthority(), mRoot.rootId, cursor, -1);

            if (mType == DirectoryFragment.TYPE_SEARCH) {
                cursor = new SortingCursorWrapper(cursor, result.sortOrder);
                // Filter directories out of search results, for now
                cursor = new FilteringCursorWrapper(cursor, null, SEARCH_REJECT_MIMES);
            } else {
                // Normal directories should have sorting applied
                cursor = new SortingCursorWrapper(cursor, result.sortOrder);
            }

            result.client = client;
            result.cursor = cursor;
        } catch (Exception e) {
            Log.w(BaseActivity.TAG, "Failed to query", e);

            result.exception = e;
        } finally {
            synchronized (this) {
                mSignal = null;
            }
            // TODO: Remove this call.
            ContentProviderClientCompat.releaseQuietly(client);
        }

        return result;
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mSignal != null) {
                mSignal.cancel();
            }
        }
    }

    @Override
    public void deliverResult(DirectoryResult result) {
        if (isReset()) {
            IoUtils.closeQuietly(result);
            return;
        }
        DirectoryResult oldResult = mResult;
        mResult = result;

        if (isStarted()) {
            super.deliverResult(result);
        }

        if (oldResult != null && oldResult != result) {
            IoUtils.closeQuietly(oldResult);
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
    public void onCanceled(DirectoryResult result) {
        IoUtils.closeQuietly(result);
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        IoUtils.closeQuietly(mResult);
        mResult = null;

        getContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    public static String getQuerySortOrder(int sortOrder) {
        switch (sortOrder) {
            case BaseActivity.State.SORT_ORDER_DISPLAY_NAME:
                return DocumentsContract.Document.COLUMN_DISPLAY_NAME + " ASC";
            case BaseActivity.State.SORT_ORDER_LAST_MODIFIED:
                return DocumentsContract.Document.COLUMN_LAST_MODIFIED + " DESC";
            case BaseActivity.State.SORT_ORDER_SIZE:
                return DocumentsContract.Document.COLUMN_SIZE + " DESC";
            default:
                return null;
        }
    }

    public final class ForceLoadContentObserver extends ContentObserver {
        public ForceLoadContentObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            final String path  = null != uri ? uri.getPath() : "";
            if(!TextUtils.isEmpty(path)){
                return;
            }
            super.onChange(selfChange, uri);
        }
    }

}