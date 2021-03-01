package filemanager.fileexplorer.directory;

import android.content.Context;
import android.view.ViewGroup;

import filemanager.fileexplorer.R;
import filemanager.fileexplorer.common.RecyclerFragment.RecyclerItemClickListener.OnItemClickListener;
import filemanager.fileexplorer.directory.DocumentsAdapter.Environment;

public class GridDocumentHolder extends ListDocumentHolder {

    public GridDocumentHolder(Context context, ViewGroup parent,
                              OnItemClickListener onItemClickListener, Environment environment) {
        super(context, parent, R.layout.item_doc_grid, onItemClickListener, environment);
    }

}
