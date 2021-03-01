package filemanager.fileexplorer.directory;

import filemanager.fileexplorer.directory.DocumentsAdapter.Environment;

public class LoadingFooter extends Footer {

    public LoadingFooter(Environment environment, int type) {
        super(type);
        mEnv = environment;
        mIcon = 0;
        mMessage = "";
    }
}