

package filemanager.fileexplorer.pro.fragment;

import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.FragmentManager;

import filemanager.fileexplorer.pro.DocumentsActivity;
import filemanager.fileexplorer.pro.R;
import filemanager.fileexplorer.pro.common.DialogBuilder;
import filemanager.fileexplorer.pro.common.DialogFragment;
import filemanager.fileexplorer.pro.misc.AsyncTask;
import filemanager.fileexplorer.pro.misc.ContentProviderClientCompat;
import filemanager.fileexplorer.pro.misc.FileUtils;
import filemanager.fileexplorer.pro.misc.ProviderExecutor;
import filemanager.fileexplorer.pro.misc.TintUtils;
import filemanager.fileexplorer.pro.misc.Utils;
import filemanager.fileexplorer.pro.model.DocumentInfo;
import filemanager.fileexplorer.pro.model.DocumentsContract;

import static filemanager.fileexplorer.pro.BaseActivity.TAG;

/**
 * Dialog to create a new directory.
 */
public class RenameFragment extends DialogFragment {
    private static final String TAG_RENAME = "rename";
	private static final String EXTRA_DOC = "document";
	private boolean editExtension = true;
	private DocumentInfo doc;
	
    public static void show(FragmentManager fm, DocumentInfo doc) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_DOC, doc);
		
        final RenameFragment dialog = new RenameFragment();
        dialog.setArguments(args);
        dialog.show(fm, TAG_RENAME);
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(null != args){
			doc = args.getParcelable(EXTRA_DOC);
		}
	}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();

        final DocumentsActivity activity = (DocumentsActivity) getActivity();

        final DialogBuilder builder = new DialogBuilder(context);
        final LayoutInflater dialogInflater = LayoutInflater.from(context);

        final View view = dialogInflater.inflate(R.layout.dialog_create_dir, null, false);
        final EditText text1 = (EditText) view.findViewById(android.R.id.text1);
        TintUtils.tintWidget(text1);

        String nameOnly = editExtension ? doc.displayName : FileUtils.removeExtension(doc.mimeType, doc.displayName);
        text1.setText(nameOnly);
        text1.setSelection(text1.getText().length());
        
        builder.setTitle(R.string.menu_rename);
        builder.setView(view);

        builder.setPositiveButton(R.string.menu_rename, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String displayName = text1.getText().toString();
                final String fileName = editExtension ? displayName : FileUtils.addExtension(doc.mimeType, displayName);
                		
                new RenameTask(activity, doc, fileName).executeOnExecutor(
                        ProviderExecutor.forAuthority(doc.authority));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.create();
    }
    
    private class RenameTask extends AsyncTask<Void, Void, DocumentInfo> {
        private final DocumentsActivity mActivity;
        private final DocumentInfo mDoc;
		private final String mFileName;

        public RenameTask(
                DocumentsActivity activity, DocumentInfo doc, String fileName) {
            mActivity = activity;
            mDoc = doc;
            mFileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            mActivity.setPending(true);
        }

        @Override
        protected DocumentInfo doInBackground(Void... params) {
            final ContentResolver resolver = mActivity.getContentResolver();
            ContentProviderClient client = null;
            try {
                final Uri childUri = DocumentsContract.renameDocument(
                		resolver, mDoc.derivedUri, mFileName);
                return DocumentInfo.fromUri(resolver, childUri);
            } catch (Exception e) {
                Log.w(TAG, "Failed to rename directory", e);

                return null;
            } finally {
            	ContentProviderClientCompat.releaseQuietly(client);
            }
        }

        @Override
        protected void onPostExecute(DocumentInfo result) {
            if (!Utils.isActivityAlive(mActivity)){
               return;
            }
            if (result == null) {
                if(!mActivity.isSAFIssue(mDoc.documentId)) {
                    Utils.showError(mActivity, R.string.rename_error);
                }
            }
            mActivity.setPending(false);
        }
    }
}