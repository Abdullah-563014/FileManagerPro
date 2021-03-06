package filemanager.fileexplorer.pro.model;

import android.content.ContentProviderClient;
import android.database.Cursor;

import java.io.Closeable;

import filemanager.fileexplorer.pro.libcore.io.IoUtils;
import filemanager.fileexplorer.pro.misc.ContentProviderClientCompat;

import filemanager.fileexplorer.pro.BaseActivity;

public class DirectoryResult implements Closeable {
	public ContentProviderClient client;
    public Cursor cursor;
    public Exception exception;

    public int mode = BaseActivity.State.MODE_UNKNOWN;
    public int sortOrder = BaseActivity.State.SORT_ORDER_UNKNOWN;

    @Override
    public void close() {
        IoUtils.closeQuietly(cursor);
        ContentProviderClientCompat.releaseQuietly(client);
        cursor = null;
        client = null;
    }
}