

package filemanager.fileexplorer.cursor;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.Bundle;

/**
 * Cursor wrapper that adds columns to identify which root a document came from.
 */
public class RootCursorWrapper extends AbstractCursor {
    private final String mAuthority;
    private final String mRootId;

    private final Cursor mCursor;
    private final int mCount;

    private final String[] mColumnNames;

    private final int mAuthorityIndex;
    private final int mRootIdIndex;

    public static final String COLUMN_AUTHORITY = "android:authority";
    public static final String COLUMN_ROOT_ID = "android:rootId";

    public RootCursorWrapper(String authority, String rootId, Cursor cursor, int maxCount) {
        mAuthority = authority;
        mRootId = rootId;
        mCursor = cursor;

        final int count = cursor.getCount();
        if (maxCount > 0 && count > maxCount) {
            mCount = maxCount;
        } else {
            mCount = count;
        }

        if (cursor.getColumnIndex(COLUMN_AUTHORITY) != -1
                || cursor.getColumnIndex(COLUMN_ROOT_ID) != -1) {
            throw new IllegalArgumentException("Cursor contains internal columns!");
        }
        final String[] before = cursor.getColumnNames();
        mColumnNames = new String[before.length + 2];
        System.arraycopy(before, 0, mColumnNames, 0, before.length);
        mAuthorityIndex = before.length;
        mRootIdIndex = before.length + 1;
        mColumnNames[mAuthorityIndex] = COLUMN_AUTHORITY;
        mColumnNames[mRootIdIndex] = COLUMN_ROOT_ID;
    }

    @Override
    public Bundle getExtras() {
        return mCursor.getExtras();
    }

    @Override
    public void close() {
        super.close();
        mCursor.close();
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return mCursor.moveToPosition(newPosition);
    }

    @Override
    public String[] getColumnNames() {
        return mColumnNames;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public double getDouble(int column) {
        return mCursor.getDouble(column);
    }

    @Override
    public float getFloat(int column) {
        return mCursor.getFloat(column);
    }

    @Override
    public int getInt(int column) {
        return mCursor.getInt(column);
    }

    @Override
    public long getLong(int column) {
        return mCursor.getLong(column);
    }

    @Override
    public short getShort(int column) {
        return mCursor.getShort(column);
    }

    @Override
    public String getString(int column) {
        if (column == mAuthorityIndex) {
            return mAuthority;
        } else if (column == mRootIdIndex) {
            return mRootId;
        } else {
            return mCursor.getString(column);
        }
    }

    @Override
    public int getType(int column) {
        return mCursor.getType(column);
    }

    @Override
    public boolean isNull(int column) {
        return mCursor.isNull(column);
    }
}
