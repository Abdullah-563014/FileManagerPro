

package filemanager.fileexplorer.pro.model;

import android.content.ContentResolver;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Collection;
import java.util.LinkedList;

import filemanager.fileexplorer.pro.provider.DocumentsProvider;

/**
 * Representation of a stack of {@link DocumentInfo}, usually the result of a
 * user-driven traversal.
 */
public class DocumentStack extends LinkedList<DocumentInfo> implements Durable, Parcelable {
    private static final int VERSION_INIT = 1;
    private static final int VERSION_ADD_ROOT = 2;

    public RootInfo root;

    public String getTitle() {
        if (size() == 1 && root != null) {
            return root.title;
        } else if (size() > 1) {
            return peek().displayName;
        } else {
            return null;
        }
    }

    public boolean isRecents() {
        return size() == 0;
    }

    public void updateRoot(Collection<RootInfo> matchingRoots) throws FileNotFoundException {
        for (RootInfo root : matchingRoots) {
            if (root.equals(this.root)) {
                this.root = root;
                return;
            }
        }
        throw new FileNotFoundException("Failed to find matching root for " + root);
    }

    /**
     * Update a possibly stale restored stack against a live
     * {@link DocumentsProvider}.
     */
    public void updateDocuments(ContentResolver resolver) throws FileNotFoundException {
        for (DocumentInfo info : this) {
            info.updateSelf(resolver);
        }
    }

    /**
     * Build key that uniquely identifies this stack. It omits most of the raw
     * details included in {@link #write(DataOutputStream)}, since they change
     * too regularly to be used as a key.
     */
    public String buildKey() {
        final StringBuilder builder = new StringBuilder();
        if (root != null) {
            builder.append(root.authority).append('#');
            builder.append(root.rootId).append('#');
        } else {
            builder.append("[null]").append('#');
        }
        for (DocumentInfo doc : this) {
            builder.append(doc.documentId).append('#');
        }
        return builder.toString();
    }

    @Override
    public void reset() {
        clear();
        root = null;
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        final int version = in.readInt();
        switch (version) {
            case VERSION_INIT:
                throw new ProtocolException("Ignored upgrade");
            case VERSION_ADD_ROOT:
                if (in.readBoolean()) {
                    root = new RootInfo();
                    root.read(in);
                }
                final int size = in.readInt();
                for (int i = 0; i < size; i++) {
                    final DocumentInfo doc = new DocumentInfo();
                    doc.read(in);
                    add(doc);
                }
                break;
            default:
                throw new ProtocolException("Unknown version " + version);
        }
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(VERSION_ADD_ROOT);
        if (root != null) {
            out.writeBoolean(true);
            root.write(out);
        } else {
            out.writeBoolean(false);
        }
        final int size = size();
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            final DocumentInfo doc = get(i);
            doc.write(out);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DurableUtils.writeToParcel(dest, this);
    }

    public static final Creator<DocumentStack> CREATOR = new Creator<DocumentStack>() {
        @Override
        public DocumentStack createFromParcel(Parcel in) {
            final DocumentStack stack = new DocumentStack();
            DurableUtils.readFromParcel(in, stack);
            return stack;
        }

        @Override
        public DocumentStack[] newArray(int size) {
            return new DocumentStack[size];
        }
    };
}
