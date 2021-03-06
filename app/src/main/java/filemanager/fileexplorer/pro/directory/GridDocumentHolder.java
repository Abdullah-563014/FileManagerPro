package filemanager.fileexplorer.pro.directory;

import android.content.Context;
import android.view.ViewGroup;

import filemanager.fileexplorer.pro.R;
import filemanager.fileexplorer.pro.common.RecyclerFragment.RecyclerItemClickListener.OnItemClickListener;
import filemanager.fileexplorer.pro.directory.DocumentsAdapter.Environment;

public class GridDocumentHolder extends ListDocumentHolder {

    public GridDocumentHolder(Context context, ViewGroup parent,
                              OnItemClickListener onItemClickListener, Environment environment) {
        super(context, parent, R.layout.item_doc_grid, onItemClickListener, environment);
    }

}
