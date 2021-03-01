

package android.support.provider;

import android.content.Context;
import android.net.Uri;

class SingleDocumentFile extends DocumentFile {
    private Context mContext;
    private Uri mUri;

    SingleDocumentFile(DocumentFile parent, Context context, Uri uri) {
        super(parent);
        mContext = context;
        mUri = uri;
    }

    @Override
    public DocumentFile createFile(String mimeType, String displayName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DocumentFile createDirectory(String displayName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri getUri() {
        return mUri;
    }

    @Override
    public String getName() {
        return DocumentsContractApi19.getName(mContext, mUri);
    }

    @Override
    public String getType() {
        return DocumentsContractApi19.getType(mContext, mUri);
    }

    @Override
    public boolean isDirectory() {
        return DocumentsContractApi19.isDirectory(mContext, mUri);
    }

    @Override
    public boolean isFile() {
        return DocumentsContractApi19.isFile(mContext, mUri);
    }

    @Override
    public boolean isVirtual() {
        return DocumentsContractApi19.isVirtual(mContext, mUri);
    }

    @Override
    public long lastModified() {
        return DocumentsContractApi19.lastModified(mContext, mUri);
    }

    @Override
    public long length() {
        return DocumentsContractApi19.length(mContext, mUri);
    }

    @Override
    public boolean canRead() {
        return DocumentsContractApi19.canRead(mContext, mUri);
    }

    @Override
    public boolean canWrite() {
        return DocumentsContractApi19.canWrite(mContext, mUri);
    }

    @Override
    public boolean delete() {
        return DocumentsContractApi19.delete(mContext, mUri);
    }

    @Override
    public boolean exists() {
        return DocumentsContractApi19.exists(mContext, mUri);
    }

    @Override
    public DocumentFile[] listFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean renameTo(String displayName) {
        throw new UnsupportedOperationException();
    }
}
