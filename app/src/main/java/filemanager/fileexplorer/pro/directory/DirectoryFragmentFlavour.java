package filemanager.fileexplorer.pro.directory;

import filemanager.fileexplorer.pro.common.RecyclerFragment;
import filemanager.fileexplorer.pro.model.DirectoryResult;

public abstract class DirectoryFragmentFlavour extends RecyclerFragment {
    public static final int AD_POSITION = 5;

    public void loadNativeAds(final DirectoryResult result) {
        int cursorCount = result.cursor != null ? result.cursor.getCount() : 0;
        showData(result);

        int numberOfAds = cursorCount / AD_POSITION;
    }


    public abstract void showData(DirectoryResult result);


}
