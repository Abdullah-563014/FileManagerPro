

package filemanager.fileexplorer.misc;


import android.text.TextUtils;

import filemanager.fileexplorer.libcore.util.Predicate;
import filemanager.fileexplorer.model.DocumentInfo;
import filemanager.fileexplorer.model.DocumentsContract;
import filemanager.fileexplorer.model.DocumentsContract.Document;

public class MimePredicate implements Predicate<DocumentInfo> {
    private final String[] mFilters;

    /**
     * MIME types that are visual in nature. For example, they should always be
     * shown as thumbnails in list mode.
     */
    public static final String[] VISUAL_MIMES = new String[] { 
    	"image/*",
    	"video/*",
    	"audio/*",
    	Document.MIME_TYPE_APK};

    public static final String[] MEDIA_MIMES = new String[] {
            "image/*",
            "video/*",
            "audio/*"};

    public static final String[] SPECIAL_MIMES = new String[] { 
    	"application/zip",
    	"application/rar",
    	"application/gzip",
    	Document.MIME_TYPE_APK};
    
    public static final String[] COMPRESSED_MIMES = new String[] { 
    	"application/zip",
    	"application/rar",
    	"application/gzip"};

    public static final String[] SHARE_SKIP_MIMES = new String[] {
            DocumentsContract.Document.MIME_TYPE_APK };

    public static final String[] TEXT_MIMES = new String[] {
            "text/*", };

    public MimePredicate(String[] filters) {
        mFilters = filters;
    }

    @Override
    public boolean apply(DocumentInfo doc) {
        if (doc.isDirectory()) {
            return true;
        }
        return mimeMatches(mFilters, doc.mimeType);
    }

    public static boolean mimeMatches(String[] filters, String[] tests) {
        if (tests == null) {
            return false;
        }
        for (String test : tests) {
            if (mimeMatches(filters, test)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mimeMatches(String filter, String[] tests) {
        if (tests == null) {
            return true;
        }
        for (String test : tests) {
            if (mimeMatches(filter, test)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mimeMatches(String[] filters, String test) {
        if (filters == null) {
            return true;
        }

        if( TextUtils.isEmpty(test)){
            return false;
        }

        for (String filter : filters) {
            if (mimeMatches(filter, test)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mimeMatches(String filter, String test) {
        if (test == null) {
            return false;
        } else if (filter == null || "*/*".equals(filter)) {
            return true;
        } else if (filter.equals(test)) {
            return true;
        } else if (filter.endsWith("/*")) {
            return filter.regionMatches(0, test, 0, filter.indexOf('/'));
        } else {
            return false;
        }
    }
}