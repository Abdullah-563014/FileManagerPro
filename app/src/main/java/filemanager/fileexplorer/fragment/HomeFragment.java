

package filemanager.fileexplorer.fragment;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import filemanager.fileexplorer.BaseActivity;
import filemanager.fileexplorer.DocumentsActivity;
import filemanager.fileexplorer.DocumentsApplication;
import filemanager.fileexplorer.setting.SettingsActivity;
import filemanager.fileexplorer.R;
import filemanager.fileexplorer.adapter.CommonInfo;
import filemanager.fileexplorer.adapter.HomeAdapter;
import filemanager.fileexplorer.common.DialogBuilder;
import filemanager.fileexplorer.common.RecyclerFragment;
import filemanager.fileexplorer.cursor.LimitCursorWrapper;
import filemanager.fileexplorer.loader.RecentLoader;
import filemanager.fileexplorer.misc.AsyncTask;
import filemanager.fileexplorer.misc.IconHelper;
import filemanager.fileexplorer.misc.IconUtils;
import filemanager.fileexplorer.misc.RootsCache;
import filemanager.fileexplorer.misc.Utils;
import filemanager.fileexplorer.model.DirectoryResult;
import filemanager.fileexplorer.model.DocumentInfo;
import filemanager.fileexplorer.model.RootInfo;
import filemanager.fileexplorer.provider.AppsProvider;

import static filemanager.fileexplorer.BaseActivity.State.MODE_GRID;


import static filemanager.fileexplorer.adapter.HomeAdapter.TYPE_MAIN;
import static filemanager.fileexplorer.adapter.HomeAdapter.TYPE_RECENT;
import static filemanager.fileexplorer.adapter.HomeAdapter.TYPE_SHORTCUT;
import static filemanager.fileexplorer.misc.Utils.FILE_TYPE;
import static filemanager.fileexplorer.model.RootInfo.openRoot;
import static filemanager.fileexplorer.provider.AppsProvider.getRunningAppProcessInfo;

/**
 * Display home.
 */
public class HomeFragment extends RecyclerFragment implements HomeAdapter.OnItemClickListener {
    public static final String TAG = "HomeFragment";
    public static final String ROOTS_CHANGED = "android.intent.action.ROOTS_CHANGED";
    private static final int MAX_RECENT_COUNT = 10;

    private final int mLoaderId = 42;
    private RootsCache roots;
    private LoaderManager.LoaderCallbacks<DirectoryResult> mCallbacks;
    private RootInfo mHomeRoot;
    private BaseActivity mActivity;
    private IconHelper mIconHelper;
    private ArrayList<CommonInfo> mainData;
    private ArrayList<CommonInfo> shortcutsData;
    private HomeAdapter mAdapter;
    private RootInfo processRoot;
    private int totalSpanSize;

    public static void show(FragmentManager fm) {
        final HomeFragment fragment = new HomeFragment();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container_directory, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static HomeFragment get(FragmentManager fm) {
        return (HomeFragment) fm.findFragmentByTag(TAG);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        totalSpanSize = getResources().getInteger(R.integer.home_span);
        mActivity = ((BaseActivity) getActivity());
        mIconHelper = new IconHelper(mActivity, MODE_GRID);
        ArrayList<CommonInfo> data = new ArrayList<>();
        if (null == mAdapter) {
            mAdapter = new HomeAdapter(getActivity(), data, mIconHelper);
            mAdapter.setOnItemClickListener(this);
        }
        setListShown(false);
    }


    @Override
    public void onResume() {
        super.onResume();
        showData();
        registerReceiver();
    }

    @Override
    public void onPause() {
        unRegisterReceiver();
        super.onPause();
    }

    public void showData() {
        if (!Utils.isActivityAlive(getActivity())) {
            return;
        }
        roots = DocumentsApplication.getRootsCache(getActivity());
        if (null == roots) {
            return;
        }
        setListShown(false);
        mIconHelper.setThumbnailsEnabled(mActivity.getDisplayState().showThumbnail);
        getMainData();
        getShortcutsData();
        ArrayList<CommonInfo> data = new ArrayList<>();
        data.addAll(mainData);
        data.addAll(shortcutsData);
        mAdapter.setData(data);
        if (SettingsActivity.getDisplayRecentMedia()) {
            getRecentsData();
        } else {
            mAdapter.setRecentData(null);
            setListShown(true);
        }
    }

    private void getMainData() {
        mHomeRoot = roots.getHomeRoot();
        mainData = new ArrayList<>();
        final RootInfo primaryRoot = roots.getPrimaryRoot();
        final RootInfo secondaryRoot = roots.getSecondaryRoot();
        final RootInfo usbRoot = roots.getUSBRoot();
        final RootInfo deviceRoot = roots.getDeviceRoot();
        processRoot = roots.getProcessRoot();
        int type =  TYPE_MAIN ;
        if (null != primaryRoot) {
            mainData.add(CommonInfo.from(primaryRoot, type));
        }
        if (null != secondaryRoot) {
            mainData.add(CommonInfo.from(secondaryRoot, type));
        }
        if (null != usbRoot) {
            mainData.add(CommonInfo.from(usbRoot, type));
        }
        if (null != deviceRoot) {
            mainData.add(CommonInfo.from(deviceRoot, type));
        }
        if (null != processRoot) {
            mainData.add(CommonInfo.from(processRoot, type));
        }
    }

    private void getShortcutsData() {
        ArrayList<RootInfo> data = roots.getShortcutsInfo();
        shortcutsData = new ArrayList<>();
        for (RootInfo root : data) {
            shortcutsData.add(CommonInfo.from(root, TYPE_SHORTCUT));
        }


    }

    private void getRecentsData() {
        final BaseActivity.State state = getDisplayState(this);
        mCallbacks = new LoaderManager.LoaderCallbacks<DirectoryResult>() {

            @Override
            public Loader<DirectoryResult> onCreateLoader(int id, Bundle args) {
                return new RecentLoader(getActivity(), roots, state);
            }

            @Override
            public void onLoadFinished(Loader<DirectoryResult> loader, DirectoryResult result) {
                if (!isAdded())
                    return;
                if (null != result.cursor && result.cursor.getCount() != 0) {
                    mAdapter.setRecentData(new LimitCursorWrapper(result.cursor, MAX_RECENT_COUNT));
                }
                setListShown(true);
            }

            @Override
            public void onLoaderReset(Loader<DirectoryResult> loader) {
                mAdapter.setRecentData(null);
                setListShown(true);
            }
        };
        LoaderManager.getInstance(getActivity()).restartLoader(mLoaderId, null, mCallbacks);
    }

    public void reloadData() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showData();
            }
        }, 500);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(HomeAdapter.ViewHolder item, View view, int position) {
        switch (item.commonInfo.type) {
            case TYPE_MAIN:
            case TYPE_SHORTCUT:
                if (null == item.commonInfo.rootInfo) {
                    return;
                }
                if (item.commonInfo.rootInfo.rootId.equals("clean")) {
                    cleanRAM();
                } else {
                    DocumentsActivity activity = ((DocumentsActivity) getActivity());
                    openRoot(activity, item.commonInfo.rootInfo, mHomeRoot);
                }
                break;
            case TYPE_RECENT:
                try {
                    final DocumentInfo documentInfo = ((HomeAdapter.GalleryViewHolder) item).getItem(position);
                    openDocument(documentInfo);
                } catch (Exception ignore) {
                }
                break;
        }
    }

    @Override
    public void onItemLongClick(HomeAdapter.ViewHolder item, View view, int position) {

    }

    @Override
    public void onItemViewClick(HomeAdapter.ViewHolder item, View view, int position) {
        switch (view.getId()) {
            case R.id.recents:
                DocumentsActivity activity = ((DocumentsActivity) getActivity());
                openRoot(activity, roots.getRecentsRoot(), mHomeRoot);
                break;

            case R.id.action:
                Bundle params = new Bundle();
                if (item.commonInfo.rootInfo.isAppProcess()) {
                    cleanRAM();
                } else {
                    Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                    if (Utils.isIntentAvailable(getActivity(), intent)) {
                        getActivity().startActivity(intent);
                    } else {
                        Utils.showSnackBar(getActivity(), "Coming Soon!");
                    }

                }
                break;
        }

    }

    private void cleanRAM() {
        Bundle params = new Bundle();
        new OperationTask(processRoot).execute();
    }

    private class OperationTask extends AsyncTask<Void, Void, Boolean> {

        private Dialog progressDialog;
        private RootInfo root;
        private long currentAvailableBytes;

        public OperationTask(RootInfo root) {
            DialogBuilder builder = new DialogBuilder(getActivity());
            builder.setMessage("Cleaning up RAM...");
            builder.setIndeterminate(true);
            progressDialog = builder.create();
            this.root = root;
            currentAvailableBytes = root.availableBytes;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            cleanupMemory(getActivity());
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!Utils.isActivityAlive(getActivity())) {
                return;
            }
            AppsProvider.notifyDocumentsChanged(getActivity(), root.rootId);
            AppsProvider.notifyRootsChanged(getActivity());
            RootsCache.updateRoots(getActivity(), AppsProvider.AUTHORITY);
            roots = DocumentsApplication.getRootsCache(getActivity());
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentAvailableBytes != 0) {
                        long availableBytes = processRoot.availableBytes - currentAvailableBytes;
                        String summaryText = availableBytes <= 0 ? "Already cleaned up!" :
                                getActivity().getString(R.string.root_available_bytes,
                                        Formatter.formatFileSize(getActivity(), availableBytes));
                        Utils.showSnackBar(getActivity(), summaryText);
                    }
                    progressDialog.dismiss();

                }
            }, 500);
        }
    }

    private static BaseActivity.State getDisplayState(Fragment fragment) {
        return ((BaseActivity) fragment.getActivity()).getDisplayState();
    }

    public void cleanupMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcessesList = getRunningAppProcessInfo(context);
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcessesList) {
            try {
                activityManager.killBackgroundProcesses(processInfo.processName);
            } catch (Exception e) {
            }
        }
    }

    private void openDocument(DocumentInfo doc) {
        ((BaseActivity) getActivity()).onDocumentPicked(doc);
        Bundle params = new Bundle();
        String type = IconUtils.getTypeNameFromMimeType(doc.mimeType);
        params.putString(FILE_TYPE, type);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setListAdapter(mAdapter);

        ((GridLayoutManager) getListView().getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int spanSize = 1;
                switch (mAdapter.getItem(position).type) {
                    case TYPE_MAIN:
                        spanSize = totalSpanSize;
                        break;
                    case TYPE_SHORTCUT:
                        spanSize =2;
                        break;
                    case TYPE_RECENT:
                        spanSize = totalSpanSize;
                        break;
                }
                return spanSize;
            }
        });
    }

    private void registerReceiver() {
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(ROOTS_CHANGED));
    }

    private void unRegisterReceiver() {
        if (null != broadcastReceiver) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showData();
        }
    };
}