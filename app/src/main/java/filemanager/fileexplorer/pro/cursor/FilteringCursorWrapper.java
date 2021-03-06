

package filemanager.fileexplorer.pro.cursor;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import filemanager.fileexplorer.pro.BaseActivity;
import filemanager.fileexplorer.pro.misc.MimePredicate;

import filemanager.fileexplorer.pro.model.DocumentInfo;
import filemanager.fileexplorer.pro.model.DocumentsContract;

/**
 * Cursor wrapper that filters MIME types not matching given list.
 */
public class FilteringCursorWrapper extends AbstractCursor {
    private final Cursor mCursor;

    private final int[] mPosition;
    private int mCount;

    public FilteringCursorWrapper(Cursor cursor, String[] acceptMimes) {
        this(cursor, acceptMimes, null, Long.MIN_VALUE);
    }

    public FilteringCursorWrapper(Cursor cursor, String[] acceptMimes, String[] rejectMimes) {
        this(cursor, acceptMimes, rejectMimes, Long.MIN_VALUE);
    }

    public FilteringCursorWrapper(
            Cursor cursor, String[] acceptMimes, String[] rejectMimes, long rejectBefore) {
        mCursor = cursor;

        final int count = cursor.getCount();
        mPosition = new int[count];

        cursor.moveToPosition(-1);
        while (cursor.moveToNext() && mCount < count) {
            final String mimeType = DocumentInfo.getCursorString(cursor, DocumentsContract.Document.COLUMN_MIME_TYPE);
            final String name = DocumentInfo.getCursorString(cursor, DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            final long lastModified = DocumentInfo.getCursorLong(cursor, DocumentsContract.Document.COLUMN_LAST_MODIFIED);
            if (rejectMimes != null && MimePredicate.mimeMatches(rejectMimes, mimeType)) {
                continue;
            }
            if (lastModified < rejectBefore) {
                continue;
            }

            if (MimePredicate.mimeMatches(acceptMimes, mimeType)) {
                mPosition[mCount++] = cursor.getPosition();
            }
        }

        Log.d(BaseActivity.TAG, "Before filtering " + cursor.getCount() + ", after " + mCount);
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
        return mCursor.moveToPosition(mPosition[newPosition]);
    }

    @Override
    public String[] getColumnNames() {
        return mCursor.getColumnNames();
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
        return mCursor.getString(column);
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
