package filemanager.fileexplorer.pro.cloud;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;

import java.io.InputStream;

import filemanager.fileexplorer.pro.BaseActivity;
import filemanager.fileexplorer.pro.fragment.ConnectionsFragment;
import filemanager.fileexplorer.pro.misc.AsyncTask;
import filemanager.fileexplorer.pro.misc.RootsCache;
import filemanager.fileexplorer.pro.network.NetworkConnection;
import filemanager.fileexplorer.pro.provider.CloudStorageProvider;
import filemanager.fileexplorer.pro.provider.ExplorerProvider;
import filemanager.fileexplorer.pro.BuildConfig;

import static filemanager.fileexplorer.pro.BuildConfig.DROPBOX_CLIENT_ID;
import static filemanager.fileexplorer.pro.BuildConfig.DROPBOX_CLIENT_KEY;
import static filemanager.fileexplorer.pro.BuildConfig.GOOGLE_DRIVE_CLIENT_ID;
import static filemanager.fileexplorer.pro.model.DocumentInfo.getCursorInt;
import static filemanager.fileexplorer.pro.model.DocumentInfo.getCursorString;

public class CloudConnection {

    private static final String TAG = NetworkConnection.class.getSimpleName();
    private static final String ROOT = "/";

    public final static String GOOGLE_DRIVE_REDIRECT_URI = "filemanager.fileexplorer.pro"+":/oauth2redirect";
    public final static String DROPBOX_REDIRECT_URI = "https://auth.cloudrail.com/"+"filemanager.fileexplorer.pro";

    public CloudStorage cloudStorage;
    public CloudFile file;
    public String path;
    public String name;
    public String username;
    public boolean isLoggedIn = false;
    public String clientId;

    public CloudConnection(CloudStorage cloudStorage, String name, String path, String id){
        this.cloudStorage = cloudStorage;
        this.path = path;
        this.file = new CloudFile(path, id);
        this.name = name;
        this.clientId = id;
    }

    public static CloudConnection fromCursor(Context context, Cursor cursor){
        int id = getCursorInt(cursor, BaseColumns._ID);
        String name = getCursorString(cursor, ExplorerProvider.ConnectionColumns.NAME);
        String username = getCursorString(cursor, ExplorerProvider.ConnectionColumns.USERNAME);
        String result = getCursorString(cursor, ExplorerProvider.ConnectionColumns.PASSWORD);
        String path = getCursorString(cursor, ExplorerProvider.ConnectionColumns.PATH);
        String type = getCursorString(cursor, ExplorerProvider.ConnectionColumns.TYPE);
        CloudRail.setAppKey(BuildConfig.LICENSE_KEY);

        String clientId = CloudConnection.getCloudStorageId(type, id);
        CloudConnection cloudConnection = new CloudConnection(createCloudStorage(context, type), type, path, clientId);
        cloudConnection.username = username;
        cloudConnection.name = name;
        cloudConnection.clientId = clientId;
        cloudConnection.load(context, result);
        return cloudConnection;
    }

    public static CloudConnection createCloudConnections(Context context, String type){
        CloudConnection cloudConnection = new CloudConnection(createCloudStorage(context, type), getTypeName(type), "/", "");
        cloudConnection.load(context, "");
        return cloudConnection;
    }

    public static CloudStorage createCloudStorage(Context context, String type) {
        CloudStorage cloudStorage = null;

        if (type.equals(CloudStorageProvider.TYPE_GDRIVE)) {
            cloudStorage = new GoogleDrive(context, GOOGLE_DRIVE_CLIENT_ID, "",
                    GOOGLE_DRIVE_REDIRECT_URI, "");
        } else if (type.equals(CloudStorageProvider.TYPE_DROPBOX)) {
            cloudStorage = new Dropbox(context, DROPBOX_CLIENT_ID, DROPBOX_CLIENT_KEY,
                    DROPBOX_REDIRECT_URI, "");
        }

        return cloudStorage;
    }

    public void prepare(){
        this.name = cloudStorage.getUserName();
        this.username = cloudStorage.getUserLogin();
    }

    public String getPath() {
        return path;
    }

    public String getSummary() {
        return username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getType(){
        if (cloudStorage instanceof GoogleDrive) {
            return CloudStorageProvider.TYPE_GDRIVE;
        } else if (cloudStorage instanceof Dropbox) {
            return CloudStorageProvider.TYPE_DROPBOX;
        } if (cloudStorage instanceof OneDrive) {
            return CloudStorageProvider.TYPE_ONEDRIVE;
        } if (cloudStorage instanceof Box) {
            return CloudStorageProvider.TYPE_BOX;
        } else {
            return CloudStorageProvider.TYPE_CLOUD;
        }
    }

    public String getTypeName(){
        return getTypeName(getType());
    }

    public static String getTypeName(String type){
        if (type.equals(CloudStorageProvider.TYPE_GDRIVE)) {
            return "Google Drive";
        } else if (type.equals(CloudStorageProvider.TYPE_DROPBOX)) {
            return "Drop Box";
        } else if (type.equals(CloudStorageProvider.TYPE_ONEDRIVE)) {
            return "One Drive";
        } else if (type.equals(CloudStorageProvider.TYPE_BOX)) {
            return "Box";
        } else {
            return "Cloud";
        }
    }

    public void login() {
        String type = getType();
        if (type.equals(CloudStorageProvider.TYPE_GDRIVE)) {
            ((GoogleDrive) cloudStorage).useAdvancedAuthentication();
        } else if (type.equals(CloudStorageProvider.TYPE_DROPBOX)) {
           // ((Dropbox) cloudStorage).useAdvancedAuthentication();
        }
        cloudStorage.login();
    }

    public boolean load(Context context, String result){
        if(!TextUtils.isEmpty(result)){
            try {
                cloudStorage.loadAsString(result);
                isLoggedIn = true;
                return isLoggedIn;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public InputStream getInputStream(CloudFile file) {
        return cloudStorage.download(file.getAbsolutePath());
    }

    public InputStream getThumbnailInputStream(CloudFile file) {
        return cloudStorage.getThumbnail(file.getAbsolutePath());
    }

    public static class CreateConnectionTask extends AsyncTask<Void, Void, Boolean> {
        private final BaseActivity mActivity;
        private final CloudConnection mCloudConnection;

        public CreateConnectionTask(
                BaseActivity activity, CloudConnection cloudConnection) {
            mActivity = activity;
            mCloudConnection = cloudConnection;
        }

        @Override
        protected void onPreExecute() {
            mActivity.setPending(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mCloudConnection.login();
                mCloudConnection.prepare();
                return CloudStorageProvider.addUpdateConnection(mActivity, mCloudConnection);
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                RootsCache.updateRoots(mActivity, CloudStorageProvider.AUTHORITY);
                ConnectionsFragment connectionsFragment = ConnectionsFragment.get(mActivity.getSupportFragmentManager());
                if(null != connectionsFragment){
                    connectionsFragment.reload();
                    connectionsFragment.openConnectionRoot(mCloudConnection);
                }
            }
        }
    }

    public static String getCloudStorageId(String type, int id){
        return type+"_"+id;
    }
}
