package filemanager.fileexplorer.pro;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudrail.si.CloudRail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.urapp.myappratinglibrary.AppRatingDialog;
import com.urapp.myappratinglibrary.listener.RatingDialogListener;

import filemanager.fileexplorer.pro.BaseActivity;
import filemanager.fileexplorer.pro.fragment.ConnectionsFragment;
import filemanager.fileexplorer.pro.fragment.CreateDirectoryFragment;
import filemanager.fileexplorer.pro.fragment.CreateFileFragment;
import filemanager.fileexplorer.pro.fragment.DirectoryFragment;
import filemanager.fileexplorer.pro.fragment.HomeFragment;
import filemanager.fileexplorer.pro.fragment.MoveFragment;
import filemanager.fileexplorer.pro.fragment.PickFragment;
import filemanager.fileexplorer.pro.fragment.QueueFragment;
import filemanager.fileexplorer.pro.fragment.RecentsCreateFragment;
import filemanager.fileexplorer.pro.fragment.SaveFragment;
import filemanager.fileexplorer.pro.fragment.ServerFragment;
import filemanager.fileexplorer.pro.fragment.TransferFragment;
import filemanager.fileexplorer.pro.misc.PreferenceUtils;
import filemanager.fileexplorer.pro.model.AdsModel;
import filemanager.fileexplorer.pro.model.VersionInfoModel;
import filemanager.fileexplorer.pro.network.NetworkConnection;
import filemanager.fileexplorer.pro.provider.ExternalStorageProvider;
import filemanager.fileexplorer.pro.provider.MediaDocumentsProvider;
import filemanager.fileexplorer.pro.provider.RecentsProvider;
import filemanager.fileexplorer.pro.setting.SettingsActivity;
import filemanager.fileexplorer.pro.transfer.TransferHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;

import filemanager.fileexplorer.pro.archive.DocumentArchiveHelper;
import filemanager.fileexplorer.pro.cast.CastUtils;
import filemanager.fileexplorer.pro.cast.Casty;
import filemanager.fileexplorer.pro.common.RootsCommonFragment;
import filemanager.fileexplorer.pro.libcore.io.IoUtils;
import filemanager.fileexplorer.pro.misc.AppRate;
import filemanager.fileexplorer.pro.misc.AsyncTask;
import filemanager.fileexplorer.pro.misc.ConnectionUtils;
import filemanager.fileexplorer.pro.misc.ContentProviderClientCompat;
import filemanager.fileexplorer.pro.misc.FileUtils;
import filemanager.fileexplorer.pro.misc.IntentUtils;
import filemanager.fileexplorer.pro.misc.MimePredicate;
import filemanager.fileexplorer.pro.misc.PermissionUtil;
import filemanager.fileexplorer.pro.misc.ProviderExecutor;
import filemanager.fileexplorer.pro.misc.RootsCache;
import filemanager.fileexplorer.pro.misc.SAFManager;
import filemanager.fileexplorer.pro.misc.SecurityHelper;
import filemanager.fileexplorer.pro.misc.SystemBarTintManager;
import filemanager.fileexplorer.pro.misc.Utils;
import filemanager.fileexplorer.pro.model.DocumentInfo;
import filemanager.fileexplorer.pro.model.DocumentStack;
import filemanager.fileexplorer.pro.model.DocumentsContract;
import filemanager.fileexplorer.pro.model.DocumentsContract.Root;
import filemanager.fileexplorer.pro.model.DurableUtils;
import filemanager.fileexplorer.pro.model.RootInfo;
import filemanager.fileexplorer.pro.ui.DirectoryContainerView;
import filemanager.fileexplorer.pro.ui.DrawerLayoutHelper;
import filemanager.fileexplorer.pro.ui.FloatingActionsMenu;
import filemanager.fileexplorer.pro.ui.fabs.SimpleMenuListenerAdapter;



import static filemanager.fileexplorer.pro.misc.SAFManager.ADD_STORAGE_REQUEST_CODE;
import static filemanager.fileexplorer.pro.misc.SecurityHelper.REQUEST_CONFIRM_CREDENTIALS;
import static filemanager.fileexplorer.pro.misc.Utils.EXTRA_ROOT;
import static filemanager.fileexplorer.pro.misc.Utils.FILE_COUNT;
import static filemanager.fileexplorer.pro.misc.Utils.FILE_MOVE;
import static filemanager.fileexplorer.pro.misc.Utils.FILE_TYPE;

public class DocumentsActivity extends BaseActivity implements MenuItem.OnMenuItemClickListener, RatingDialogListener {

    private static final String EXTRA_STATE = "state";
    private static final String EXTRA_AUTHENTICATED = "authenticated";
    private static final String EXTRA_ACTIONMODE = "actionmode";
    private static final String EXTRA_SEARCH_STATE = "searchsate";
    public static final String BROWSABLE = "android.intent.category.BROWSABLE";
    private static final int UPLOAD_FILE = 99;

    private static final int CODE_FORWARD = 42;
    private static final int CODE_SETTINGS = 92;

    private static final boolean SHOW_NATIVE_ADS = false;

    private boolean mShowAsDialog;

    private SearchView mSearchView;

    private Toolbar mToolbar;
    private Spinner mToolbarStack;

    private DrawerLayoutHelper mDrawerLayoutHelper;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mRootsContainer;
    private View mInfoContainer;

    private DirectoryContainerView mDirectoryContainer;

    private boolean mIgnoreNextNavigation;
    private boolean mIgnoreNextClose;
    private boolean mIgnoreNextCollapse;

    private boolean mSearchExpanded;
    private boolean mSearchResultShown;

    private RootsCache mRoots;
    private State mState;
    private boolean mAuthenticated;
    private FrameLayout mRateContainer;
    private boolean mActionMode;
    private FloatingActionsMenu mActionMenu;
    private RootInfo mParentRoot;
    private boolean SAFPermissionRequested;
    // public boolean showBannerAds = !isPurchased();
    public boolean showBannerAds = true;
    private DatabaseReference databaseReference;



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle icicle) {
        setTheme(R.style.DocumentsTheme_Document);
        if (Utils.hasLollipop()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else if (Utils.hasKitKat()) {
            setTheme(R.style.DocumentsTheme_Translucent);
        }
        setUpStatusBar();

/*		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
				.penaltyLog()
				.build());
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
				.penaltyLog()
				.build())x;
*/
        super.onCreate(icicle);


        databaseReference= FirebaseDatabase.getInstance().getReference();


        mRoots = DocumentsApplication.getRootsCache(this);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity);


        final Context context = this;
        final Resources res = getResources();
        mShowAsDialog = res.getBoolean(R.bool.show_as_dialog);

        mDirectoryContainer = (DirectoryContainerView) findViewById(R.id.container_directory);
        mRateContainer = (FrameLayout) findViewById(R.id.container_rate);

        initControls();

        if (icicle != null) {
            mState = icicle.getParcelable(EXTRA_STATE);
            mAuthenticated = icicle.getBoolean(EXTRA_AUTHENTICATED);
            mActionMode = icicle.getBoolean(EXTRA_ACTIONMODE);
        } else {
            buildDefaultState();
            loadAdsConditionFromDatabase();
            loadVersionNameFromDatabase();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextAppearance(context, R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);
        if (Utils.hasKitKat() && !Utils.hasLollipop()) {
            ((LinearLayout.LayoutParams) mToolbar.getLayoutParams()).setMargins(0, getStatusBarHeight(this), 0, 0);
            mToolbar.setPadding(0, getStatusBarHeight(this), 0, 0);
        }


        mToolbarStack = (Spinner) findViewById(R.id.stack);
        mToolbarStack.setOnItemSelectedListener(mStackListener);

        setSupportActionBar(mToolbar);

        mRootsContainer = findViewById(R.id.drawer_roots);
        mInfoContainer = findViewById(R.id.container_info);

        if (!mShowAsDialog) {
            // Non-dialog means we have a drawer
            mDrawerLayoutHelper = new DrawerLayoutHelper(findViewById(R.id.drawer_layout));
            View view = findViewById(R.id.drawer_layout);
            if (view instanceof DrawerLayout) {
                DrawerLayout mDrawerLayout = (DrawerLayout) view;

                mDrawerToggle = new ActionBarDrawerToggle(
                        this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
                mDrawerLayout.addDrawerListener(mDrawerToggle);
                mDrawerToggle.syncState();
                lockInfoContainter();
            }
        }

        changeActionBarColor();

        // Hide roots when we're managing a specific root
        if (mState.action == State.ACTION_MANAGE) {
            if (mShowAsDialog) {
                findViewById(R.id.container_roots).setVisibility(View.GONE);
            } else {
                mDrawerLayoutHelper.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }

        if (mState.action == State.ACTION_CREATE) {
            final String mimeType = getIntent().getType();
            final String title = getIntent().getStringExtra(IntentUtils.EXTRA_TITLE);
            SaveFragment.show(getSupportFragmentManager(), mimeType, title);
        } else if (mState.action == State.ACTION_OPEN_TREE) {
            PickFragment.show(getSupportFragmentManager());
        }

        if (mState.action == State.ACTION_BROWSE) {
            final Intent moreApps = new Intent(getIntent());
            moreApps.setComponent(null);
            moreApps.setPackage(null);
            RootsCommonFragment.show(getSupportFragmentManager(), moreApps);

            Log.e(" moreApps", "moreApps");
        } else if (mState.action == State.ACTION_OPEN || mState.action == State.ACTION_CREATE
                || mState.action == State.ACTION_GET_CONTENT || mState.action == State.ACTION_OPEN_TREE) {
            RootsCommonFragment.show(getSupportFragmentManager(), new Intent());

            Log.e(" RootsCommon", "RootsCommonFragment");
        }

        if (!mState.restored) {
            if (mState.action == State.ACTION_MANAGE) {
                final Uri rootUri = getIntent().getData();
                new RestoreRootTask(rootUri).executeOnExecutor(getCurrentExecutor());
            } else {
                if (ExternalStorageProvider.isDownloadAuthority(getIntent())) {
                    onRootPicked(getDownloadRoot(), true);
                } else if (ConnectionUtils.isServerAuthority(getIntent())
                        || TransferHelper.isTransferAuthority(getIntent())) {
                    RootInfo root = getIntent().getExtras().getParcelable(EXTRA_ROOT);
                    onRootPicked(root, true);
                } else if (Utils.isQSTile(getIntent())) {
                    NetworkConnection networkConnection = NetworkConnection.getDefaultServer(this);
                    RootInfo root = mRoots.getRootInfo(networkConnection);
                    onRootPicked(root, true);
                } else {
                    try {
                        new RestoreStackTask().execute();
                    } catch (SQLiteFullException e) {

                    }
                }
            }
        } else {
            onCurrentDirectoryChanged(DirectoryFragment.ANIM_NONE);
        }

        if (!PermissionUtil.hasStoragePermission(this)) {
            requestStoragePermissions();
        }





    }


    private void loadAdsConditionFromDatabase() {
        databaseReference.child("AdsCondition").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue()!=null) {
                    AdsModel adsModel=snapshot.getValue(AdsModel.class);
                    if (adsModel!=null) {
                        PreferenceUtils.set(Utils.willShowAdsKey,adsModel.getWillShowAds());
                        PreferenceUtils.set(Utils.interstitialAdsIntervalInHoursKey,adsModel.getAdsIntervalInHours());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadVersionNameFromDatabase() {
        databaseReference.child("VersionInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue()!=null) {
                    VersionInfoModel versionInfoModel = dataSnapshot.getValue(VersionInfoModel.class);
                    if (versionInfoModel!=null) {
                        PreferenceUtils.set(getApplicationContext(),Utils.versionNameKey,String.valueOf(versionInfoModel.getVersionName()));
                        PreferenceUtils.set(getApplicationContext(),Utils.versionNumberKey,versionInfoModel.getVersionNumber());
                        PreferenceUtils.set(getApplicationContext(),Utils.versionMessageKey,versionInfoModel.getVersionMessage());
                        checkMandatoryUpdate();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkMandatoryUpdate() {
        int versionNumber= PreferenceUtils.getIntegerPrefs(getApplicationContext(),Utils.versionNumberKey,1);
        double versionName= Double.parseDouble(PreferenceUtils.getStringPrefs(getApplicationContext(),Utils.versionNameKey,"1.0"));
        String versionMessage= PreferenceUtils.getStringPrefs(getApplicationContext(),Utils.versionMessageKey,getResources().getString(R.string.update_message));

        double deviceVersionName=1.0;
        PackageManager manager = getApplicationContext().getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info != null) {
            deviceVersionName = Double.parseDouble(info.versionName);
        }

        if (deviceVersionName < versionName) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentsActivity.this);
            builder.setTitle(getResources().getString(R.string.update_title));
            builder.setMessage(versionMessage);
            builder.setCancelable(false);
            builder.setPositiveButton(getResources().getString(R.string.update_now), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    openAppLink();
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            if (!isFinishing()) {
                alertDialog.show();
            }
        }
    }

    private void openAppLink() {
        final String appPackageName = getApplicationContext().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private AppRatingDialog appFeedbackDialog() {
        return new AppRatingDialog.Builder()
                .setCancelable(false)
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Never")
                .setNeutralButtonText("Later")
                .setTitle(getResources().getString(R.string.app_feedback_title))
                .setDescription(getResources().getString(R.string.app_feedback_message))
                .setStarColor(R.color.accentColor)
                .setAfterInstallDay(2)
                .setDefaultRating(3)
                .setNumberOfLaunches(4)
                .setRemindIntervalDay(1)
                .setCanceledOnTouchOutside(false)
                .create(this);
    }

    @Override
    public void onNegativeButtonClicked() {
        Toast.makeText(this, getResources().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    @Override
    public void onNeutralButtonClicked() {
        Toast.makeText(this, getResources().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    @Override
    public void onPositiveButtonClicked(int i) {
        if (i==5) {
            openAppLink();
        } else {
            Toast.makeText(this, getResources().getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (null != categories && categories.contains(BROWSABLE)) {
            try {
                // Here we pass the response to the SDK which will automatically
                // complete the authentication process
                CloudRail.setAuthenticationResponse(intent);
            } catch (Exception ignore) {
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public void again() {
        if (Utils.hasMarshmallow()) {
            RootsCache.updateRoots(this);
            mRoots = DocumentsApplication.getRootsCache(this);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRoots.updateAsync();
                    final RootInfo root = getCurrentRoot();
                    if (root.isHome()) {
                        HomeFragment homeFragment = HomeFragment.get(getSupportFragmentManager());
                        if (null != homeFragment) {
                            homeFragment.reloadData();
                        }
                    }
                }
            }, 500);
        }
    }

    private void lockInfoContainter() {
        if (mDrawerLayoutHelper.isDrawerOpen(mInfoContainer)) {
            mDrawerLayoutHelper.closeDrawer(mInfoContainer);
        }

        mDrawerLayoutHelper.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mInfoContainer);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public int getGravity() {
        if (Utils.hasJellyBeanMR1()) {
            Configuration config = getResources().getConfiguration();
            if (config.getLayoutDirection() != View.LAYOUT_DIRECTION_LTR) {
                return Gravity.LEFT;
            }
        }
        return Gravity.RIGHT;
    }

    public static boolean isRTL(Locale locale) {
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    private void initProtection() {

        if (mAuthenticated || !SettingsActivity.isSecurityEnabled(this)) {
            return;
        }

        if (Utils.hasMarshmallow()) {
            SecurityHelper securityHelper = new SecurityHelper(this);
            securityHelper.authenticate(getResources().getString(R.string.app_name), "Use device pattern to continue");
        }
    }

    private void buildDefaultState() {
        mState = new State();

        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (IntentUtils.ACTION_OPEN_DOCUMENT.equals(action)) {
            mState.action = State.ACTION_OPEN;
        } else if (IntentUtils.ACTION_CREATE_DOCUMENT.equals(action)) {
            mState.action = State.ACTION_CREATE;
        } else if (IntentUtils.ACTION_GET_CONTENT.equals(action)) {
            mState.action = State.ACTION_GET_CONTENT;
        } else if (IntentUtils.ACTION_OPEN_DOCUMENT_TREE.equals(action)) {
            mState.action = State.ACTION_OPEN_TREE;
        } else if (DocumentsContract.ACTION_MANAGE_ROOT.equals(action)) {
            //mState.action = ACTION_MANAGE;
            mState.action = State.ACTION_BROWSE;
            Log.e(" mState.1", "ACTION_BROWSE");

        } else {

            mState.action = State.ACTION_BROWSE;
            Log.e(" mState.2", "ACTION_BROWSE");
        }

        if (mState.action == State.ACTION_OPEN || mState.action == State.ACTION_GET_CONTENT) {
            mState.allowMultiple = intent.getBooleanExtra(IntentUtils.EXTRA_ALLOW_MULTIPLE, false);
        }

        if (mState.action == State.ACTION_GET_CONTENT || mState.action == State.ACTION_BROWSE) {
            mState.acceptMimes = new String[]{"*/*"};
            mState.allowMultiple = true;
        } else if (intent.hasExtra(IntentUtils.EXTRA_MIME_TYPES)) {
            mState.acceptMimes = intent.getStringArrayExtra(IntentUtils.EXTRA_MIME_TYPES);
        } else {
            mState.acceptMimes = new String[]{intent.getType()};
        }

        mState.localOnly = intent.getBooleanExtra(IntentUtils.EXTRA_LOCAL_ONLY, true);
        mState.forceAdvanced = intent.getBooleanExtra(DocumentsContract.EXTRA_SHOW_ADVANCED, false);
        mState.showAdvanced = mState.forceAdvanced
                | SettingsActivity.getDisplayAdvancedDevices(this);

        mState.rootMode = SettingsActivity.getRootMode(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (menuAction(item)) {
            closeDrawer();
            return true;
        }
        return false;
    }

    public void closeDrawer() {
        mDrawerLayoutHelper.closeDrawer(Utils.getActionDrawer(this));
    }

    private class RestoreRootTask extends AsyncTask<Void, Void, RootInfo> {
        private Uri mRootUri;

        public RestoreRootTask(Uri rootUri) {
            mRootUri = rootUri;
        }

        @Override
        protected RootInfo doInBackground(Void... params) {
            final String rootId = DocumentsContract.getRootId(mRootUri);
            return mRoots.getRootOneshot(mRootUri.getAuthority(), rootId);
        }

        @Override
        protected void onPostExecute(RootInfo root) {
            if (!Utils.isActivityAlive(DocumentsActivity.this)) {
                return;
            }
            mState.restored = true;

            if (root != null) {
                onRootPicked(root, true);
            } else {
                Log.w(TAG, "Failed to find root: " + mRootUri);
                finish();
            }
        }
    }

    private class RestoreStackTask extends AsyncTask<Void, Void, Void> {
        private volatile boolean mRestoredStack;
        private volatile boolean mExternal;

        @Override
        protected Void doInBackground(Void... params) {
            // Restore last stack for calling package
            final String packageName = getCallingPackageMaybeExtra();
            final Cursor cursor = getContentResolver()
                    .query(RecentsProvider.buildResume(packageName), null, null, null, null);
            try {
                if (null != cursor && cursor.moveToFirst()) {
                    mExternal = cursor.getInt(cursor.getColumnIndex(RecentsProvider.ResumeColumns.EXTERNAL)) != 0;
                    final byte[] rawStack = cursor.getBlob(
                            cursor.getColumnIndex(RecentsProvider.ResumeColumns.STACK));
                    DurableUtils.readFromArray(rawStack, mState.stack);
                    mRestoredStack = true;
                }
            } catch (IOException e) {
                Log.w(TAG, "Failed to resume: " + e);

            } finally {
                IoUtils.closeQuietly(cursor);
            }

            if (mRestoredStack) {
                // Update the restored stack to ensure we have freshest data
                final Collection<RootInfo> matchingRoots = mRoots.getMatchingRootsBlocking(mState);
                try {
                    mState.stack.updateRoot(matchingRoots);
                    mState.stack.updateDocuments(getContentResolver());
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "Failed to restore stack: " + e);

                    mState.stack.reset();
                    mRestoredStack = false;
                }
            } else {
                RootInfo root = getCurrentRoot();
                if (null == root) {
                    return null;
                }
                final Uri uri = DocumentsContract.buildDocumentUri(root.authority, root.documentId);
                DocumentInfo result;
                try {
                    result = DocumentInfo.fromUri(getContentResolver(), uri);
                    if (result != null) {
                        mState.stack.push(result);
                        mState.stackTouched = true;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!Utils.isActivityAlive(DocumentsActivity.this)) {
                return;
            }
            mState.restored = true;

            // Show drawer when no stack restored, but only when requesting
            // non-visual content. However, if we last used an external app,
            // drawer is always shown.

            boolean showDrawer = false;
            if (!mRestoredStack) {
                showDrawer = false;
            }
            if (MimePredicate.mimeMatches(MimePredicate.VISUAL_MIMES, mState.acceptMimes)) {
                showDrawer = false;
            }
            if (mExternal && (mState.action == State.ACTION_GET_CONTENT || mState.action == State.ACTION_BROWSE)) {
                showDrawer = false;
            }

            if (showDrawer) {
                setRootsDrawerOpen(true);
            }

            onCurrentDirectoryChanged(DirectoryFragment.ANIM_NONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeActionBarColor();
        if (mState.action == State.ACTION_MANAGE) {
            mState.showSize = true;
            mState.showFolderSize = false;
            mState.showThumbnail = true;
        } else {
            mState.showSize = SettingsActivity.getDisplayFileSize(this);
            mState.showFolderSize = SettingsActivity.getDisplayFolderSize(this);
            mState.showThumbnail = SettingsActivity.getDisplayFileThumbnail(this);
            mState.showHiddenFiles = SettingsActivity.getDisplayFileHidden(this);
            invalidateMenu();
        }
        initProtection();
        checkMandatoryUpdate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateActionBar();
    }

    public void setRootsDrawerOpen(boolean open) {
        if (!mShowAsDialog) {
            if (open) {
                mDrawerLayoutHelper.openDrawer(mRootsContainer);
            } else {
                mDrawerLayoutHelper.closeDrawer(mRootsContainer);
            }
        }
    }

    public void setInfoDrawerOpen(boolean open) {
        if (!mShowAsDialog) {
            setRootsDrawerOpen(false);
            if (open) {
                mDrawerLayoutHelper.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mInfoContainer);
                mDrawerLayoutHelper.openDrawer(mInfoContainer);
            } else {
                lockInfoContainter();
            }
        }
    }

    private boolean isRootsDrawerOpen() {
        if (mShowAsDialog) {
            return false;
        } else {
            return mDrawerLayoutHelper.isDrawerOpen(mRootsContainer);
        }
    }

    public void updateActionBar() {

        final RootInfo root = getCurrentRoot();
        //final boolean showRootIcon = mShowAsDialog || (mState.action == DocumentsActivity.State.ACTION_MANAGE);
        final boolean showIndicator = !mShowAsDialog && (mState.action != State.ACTION_MANAGE);
        if (mShowAsDialog) {
            //getSupportActionBar().setDisplayHomeAsUpEnabled(showIndicator);
            //mToolbar.setLogo(R.drawable.logo);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mToolbar.setNavigationContentDescription(R.string.drawer_open);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setRootsDrawerOpen(!isRootsDrawerOpen());
            }
        });

        if (mSearchExpanded) {
            setTitle(null);
            mToolbarStack.setVisibility(View.GONE);
            mToolbarStack.setAdapter(null);
        } else {
            if (mState.stack.size() <= 1) {
                if (null != root) {
                    setTitle(root.title);
                }
                mToolbarStack.setVisibility(View.GONE);
                mToolbarStack.setAdapter(null);
            } else {
                setTitle(null);
                mToolbarStack.setVisibility(View.VISIBLE);
                mToolbarStack.setAdapter(mStackAdapter);
                mIgnoreNextNavigation = true;
                mToolbarStack.setSelection(mStackAdapter.getCount() - 1);
            }
        }
    }

    public void setTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity, menu);

        final MenuItem searchMenu = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) searchMenu.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchExpanded = mSearchResultShown = true;
                mState.currentSearch = query;
                mSearchView.clearFocus();
                onCurrentDirectoryChanged(DirectoryFragment.ANIM_NONE);
                Bundle params = new Bundle();
                params.putString("query", query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchMenu.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSearchExpanded = true;
                updateActionBar();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                mSearchExpanded = mSearchResultShown = false;
                if (mIgnoreNextCollapse) {
                    mIgnoreNextCollapse = false;
                    updateActionBar();
                    return true;
                }

                mState.currentSearch = null;
                onCurrentDirectoryChanged(DirectoryFragment.ANIM_NONE);
                return true;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mSearchExpanded = mSearchResultShown = false;
                if (mIgnoreNextClose) {
                    mIgnoreNextClose = false;
                    updateActionBar();
                    return false;
                }

                mState.currentSearch = null;
                onCurrentDirectoryChanged(DirectoryFragment.ANIM_NONE);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenuItems(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateMenuItems(Menu menu) {
        final FragmentManager fm = getSupportFragmentManager();
        final RootInfo root = getCurrentRoot();
        final DocumentInfo cwd = getCurrentDirectory();


        final MenuItem search = menu.findItem(R.id.menu_search);
        final MenuItem sort = menu.findItem(R.id.menu_sort);
        final MenuItem sortSize = menu.findItem(R.id.menu_sort_size);
        final MenuItem grid = menu.findItem(R.id.menu_grid);
        final MenuItem list = menu.findItem(R.id.menu_list);
        final MenuItem settings = menu.findItem(R.id.menu_settings);
        final MenuItem support = menu.findItem(R.id.menu_support);

//        if (!isPurchased() && !isSpecialDevice()) {
//            support.setVisible(true);
//        }
        // Open drawer means we hide most actions
        if (isRootsDrawerOpen()) {
            search.setVisible(false);
            sort.setVisible(false);
            grid.setVisible(false);
            list.setVisible(false);
            mIgnoreNextCollapse = true;
            search.collapseActionView();
            return;
        }

        sort.setVisible(cwd != null);
        grid.setVisible(!RootInfo.isOtherRoot(getCurrentRoot()) && mState.derivedMode != State.MODE_GRID);
        list.setVisible(mState.derivedMode != State.MODE_LIST);

        if (mState.currentSearch != null) {
            // Search uses backend ranking; no sorting
            //sort.setVisible(false);

            search.expandActionView();

            mSearchView.setIconified(false);
            mSearchView.clearFocus();
            mSearchView.setQuery(mState.currentSearch, false);
        } else {
            mIgnoreNextClose = true;
            mSearchView.setIconified(true);
            mSearchView.clearFocus();

            mIgnoreNextCollapse = true;
            search.collapseActionView();
        }

        // Only sort by size when visible
        sortSize.setVisible(mState.showSize);

        final boolean searchVisible;
        if (mState.action == State.ACTION_CREATE || mState.action == State.ACTION_OPEN_TREE) {
            searchVisible = false;

            // No display options in recent directories
            if (cwd == null) {
                grid.setVisible(false);
                list.setVisible(false);
            }
            if (mState.action == State.ACTION_CREATE) {
                if (null != SaveFragment.get(fm))
                    SaveFragment.get(fm).setSaveEnabled(cwd != null && cwd.isCreateSupported());
            }
        } else {
            searchVisible = root != null
                    && ((root.flags & Root.FLAG_SUPPORTS_SEARCH) != 0);
            // TODO: Is this useful?
            if (null != SaveFragment.get(fm))
                SaveFragment.get(fm).setSaveEnabled(cwd != null && cwd.isCreateSupported());

            if (null != MoveFragment.get(fm))
                MoveFragment.get(fm).setSaveEnabled(cwd != null && cwd.isMoveSupported());
        }

        // TODO: close any search in-progress when hiding
        search.setVisible(searchVisible);

        settings.setVisible(mState.action != State.ACTION_MANAGE);

        Utils.inflateActionMenu(this, this, false, root, cwd);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null) {
            if (mDrawerLayoutHelper.isDrawerOpen(mInfoContainer)) {
                mDrawerLayoutHelper.closeDrawer(mInfoContainer);
            }
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        if (menuAction(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean menuAction(MenuItem item) {

        final int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_create_dir) {
            createFolder();
            return true;
        } else if (id == R.id.menu_create_file) {
            onStateChanged();
            createFile();
            return true;
        } else if (id == R.id.menu_search) {
            return false;
        } else if (id == R.id.menu_sort_name) {
            setUserSortOrder(State.SORT_ORDER_DISPLAY_NAME);
            Bundle params = new Bundle();
            params.putString("type", "name");

            return true;
        } else if (id == R.id.menu_sort_date) {
            setUserSortOrder(State.SORT_ORDER_LAST_MODIFIED);
            Bundle params = new Bundle();
            params.putString("type", "modified");

            return true;
        } else if (id == R.id.menu_sort_size) {
            setUserSortOrder(State.SORT_ORDER_SIZE);
            Bundle params = new Bundle();
            params.putString("type", "size");

            return true;
        } else if (id == R.id.menu_grid) {
            setUserMode(State.MODE_GRID);
            Bundle params = new Bundle();
            params.putString("type", "grid");

            return true;
        } else if (id == R.id.menu_list) {
            setUserMode(State.MODE_LIST);
            Bundle params = new Bundle();
            params.putString("type", "list");
            return true;
        } else if (id == R.id.menu_settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), CODE_SETTINGS);

            return true;
        } else if (id == R.id.menu_privacy_policy) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getResources().getString(R.string.privacy_policy_url)));
            if (Utils.isIntentAvailable(this,i)) {
                startActivity(i);
            } else {
                Toast.makeText(this, "Sorry, Not available apps to open privacy policy url.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_exit) {
            Bundle params = new Bundle();
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return false;
    }

    private void createFolder() {
        CreateDirectoryFragment.show(getSupportFragmentManager());
        Bundle params = new Bundle();
        params.putString(FILE_TYPE, "folder");
    }

    private void createFile() {
        CreateFileFragment.show(getSupportFragmentManager(), "text/plain", "File");
        Bundle params = new Bundle();
        params.putString(FILE_TYPE, "file");
    }

    private void uploadFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            this.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), UPLOAD_FILE);
        } catch (ActivityNotFoundException e) {
            Utils.showError(this, R.string.upload_error);
        }
        Bundle params = new Bundle();
        params.putString(FILE_TYPE, "file");
    }

    /**
     * Update UI to reflect internal state changes not from user.
     */
    public void onStateChanged() {
        invalidateMenu();
    }

    /**
     * Set state sort order based on explicit user action.
     */
    private void setUserSortOrder(int sortOrder) {
        mState.userSortOrder = sortOrder;
        Fragment fragment = DirectoryFragment.get(getSupportFragmentManager());
        if (fragment instanceof DirectoryFragment) {
            final DirectoryFragment directory = (DirectoryFragment) fragment;
            if (directory != null) {
                directory.onUserSortOrderChanged();
            }
        }
    }

    /**
     * Set state mode based on explicit user action.
     */
    private void setUserMode(int mode) {
        mState.userMode = mode;
        Fragment fragment = DirectoryFragment.get(getSupportFragmentManager());
        if (fragment instanceof DirectoryFragment) {
            final DirectoryFragment directory = (DirectoryFragment) fragment;
            if (directory != null) {
                directory.onUserModeChanged();
            }
        }
    }

    /**
     * refresh Data currently shown
     */
    private void refreshData() {
        Fragment fragment = DirectoryFragment.get(getSupportFragmentManager());
        if (fragment instanceof DirectoryFragment) {
            final DirectoryFragment directory = (DirectoryFragment) fragment;
            if (directory != null) {
                directory.onUserSortOrderChanged();
            }
        }
    }


    public void setPending(boolean pending) {
        final SaveFragment save = SaveFragment.get(getSupportFragmentManager());
        if (save != null) {
            save.setPending(pending);
        }

        final RootInfo root = getCurrentRoot();
        if (root != null && (root.isRootedStorage() || root.isUsbStorage())) {
            refreshData();
        }
    }

    @Override
    public void onBackPressed() {
        appFeedbackDialog().monitor();
        if (appFeedbackDialog().shouldShowRateDialog()) {
            appFeedbackDialog().showRateDialogIfMeetsConditions();
        } else {
            if (isRootsDrawerOpen() && !mShowAsDialog) {
                mDrawerLayoutHelper.closeDrawer(mRootsContainer);
                return;
            }
            if (mSearchExpanded) {

            }
            if (!mState.stackTouched) {
                super.onBackPressed();
                return;
            }

            final int size = mState.stack.size();
            if (size > 1) {
                mState.stack.pop();
                onCurrentDirectoryChanged(DirectoryFragment.ANIM_UP);
            } else if (size == 1 && !isRootsDrawerOpen()) {
                // TODO: open root drawer once we can capture back key
                if (null != mParentRoot) {
                    onRootPicked(mParentRoot, true);
                    mParentRoot = null;
                    return;
                }
                super.onBackPressed();
            } else {
                if (null != mParentRoot) {
                    onRootPicked(mParentRoot, true);
                    mParentRoot = null;
                    return;
                }
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putParcelable(EXTRA_STATE, mState);
        state.putBoolean(EXTRA_AUTHENTICATED, mAuthenticated);
        state.putBoolean(EXTRA_ACTIONMODE, mActionMode);
        state.putBoolean(EXTRA_SEARCH_STATE, mSearchResultShown);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }

    private BaseAdapter mStackAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mState.stack.size();
        }

        @Override
        public DocumentInfo getItem(int position) {
            if (mState.stack.size() == 0) {
                return new DocumentInfo();
            }
            return mState.stack.get(mState.stack.size() - position - 1);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_subdir_title, parent, false);
            }

            final TextView title = (TextView) convertView.findViewById(android.R.id.title);
            final DocumentInfo doc = getItem(position);

            if (position == 0) {
                final RootInfo root = getCurrentRoot();
                if (null != root) {
                    title.setText(root.title);
                }
            } else {
                title.setText(doc.displayName);
            }

            // No padding when shown in actionbar
            //convertView.setPadding(0, 0, 0, 0);
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_subdir, parent, false);
            }

            final ImageView subdir = (ImageView) convertView.findViewById(R.id.subdir);
            final TextView title = (TextView) convertView.findViewById(android.R.id.title);
            final DocumentInfo doc = getItem(position);

            if (position == 0) {
                final RootInfo root = getCurrentRoot();
                if (null != root) {
                    title.setText(root.title);
                    subdir.setVisibility(View.GONE);
                }
            } else {
                title.setText(doc.displayName);
                subdir.setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    };

    private AdapterView.OnItemSelectedListener mStackListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (mIgnoreNextNavigation) {
                mIgnoreNextNavigation = false;
                return;
            }

            while (mState.stack.size() > position + 1) {
                mState.stackTouched = true;
                mState.stack.pop();
            }
            onCurrentDirectoryChanged(DirectoryFragment.ANIM_UP);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Ignored
        }
    };

    public RootInfo getCurrentRoot() {
        if (mState.stack.root != null) {
            return mState.stack.root;
        } else {
            return mState.action == State.ACTION_BROWSE ? mRoots.getDefaultRoot() : mRoots.getStorageRoot();
        }
    }

    public RootInfo getDownloadRoot() {
        return mRoots.getDownloadRoot();
    }

    public RootInfo getAppsBackupRoot() {
        return mRoots.getAppsBackupRoot();
    }

    public RootsCache getRoots() {
        return mRoots;
    }

    public DocumentInfo getCurrentDirectory() {
        return mState.stack.peek();
    }

    private String getCallingPackageMaybeExtra() {
        final String extra = getIntent().getStringExtra(DocumentsContract.EXTRA_PACKAGE_NAME);
        return (extra != null) ? extra : getCallingPackage();
    }

    public Executor getCurrentExecutor() {
        final DocumentInfo cwd = getCurrentDirectory();
        if (cwd != null && cwd.authority != null) {
            return ProviderExecutor.forAuthority(cwd.authority);
        } else {
            return AsyncTask.THREAD_POOL_EXECUTOR;
        }
    }

    public State getDisplayState() {
        return mState;
    }

    public boolean isShowAsDialog() {
        return mShowAsDialog;
    }

    public boolean isCreateSupported() {
        final DocumentInfo cwd = getCurrentDirectory();
        if (mState.action == State.ACTION_OPEN_TREE) {
            return cwd != null && cwd.isCreateSupported();
        } else if (mState.action == State.ACTION_CREATE || mState.action == State.ACTION_GET_CONTENT) {
            return false;
        } else {
            return cwd != null && cwd.isCreateSupported();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onCurrentDirectoryChanged(int anim) {
        //FIX for java.lang.IllegalStateException ("Activity has been destroyed")
        if (!Utils.isActivityAlive(DocumentsActivity.this)) {
            return;
        }
        final FragmentManager fm = getSupportFragmentManager();
        final RootInfo root = getCurrentRoot();
        DocumentInfo cwd = getCurrentDirectory();

        //TODO : this has to be done nicely
        boolean isExtra = (null != root && !root.isServerStorage() && !root.isTransfer());
        if (cwd == null && isExtra) {
            final Uri uri = DocumentsContract.buildDocumentUri(
                    root.authority, root.documentId);
            DocumentInfo result;
            try {
                result = DocumentInfo.fromUri(getContentResolver(), uri);
                if (result != null) {
                    mState.stack.push(result);
                    mState.stackTouched = true;
                    cwd = result;
                }
            } catch (FileNotFoundException e) {

            }
        }
        if (!SettingsActivity.getFolderAnimation(this)) {
            anim = 0;
        }

        if (cwd == null) {
            // No directory means recents
            if (mState.action == State.ACTION_CREATE || mState.action == State.ACTION_OPEN_TREE) {
                enableBannerAds();
                RecentsCreateFragment.show(fm);
            } else {
                if (null != root && root.isHome()) {
                    HomeFragment.show(fm);
                } else if (null != root && root.isConnections()) {
                    ConnectionsFragment.show(fm);
                } else if (null != root && root.isTransfer()) {
                    TransferFragment.show(fm);
                } else if (null != root && root.isCast()) {
                    QueueFragment.show(fm);
                } else if (null != root && root.isServerStorage()) {
                    ServerFragment.show(fm, root);
                } else {
                    enableBannerAds();
                    DirectoryFragment.showRecentsOpen(fm, anim, root);

                }
            }
        } else {
            enableBannerAds();
            if (mState.currentSearch != null && mSearchResultShown) {
                // Ongoing search
                DirectoryFragment.showSearch(fm, root, cwd, mState.currentSearch, anim);
                mSearchResultShown = false;
            } else {
                // Normal boring directory
                DirectoryFragment.showNormal(fm, root, cwd, anim);
            }
        }

        // Forget any replacement target
        if (mState.action == State.ACTION_CREATE) {
            final SaveFragment save = SaveFragment.get(fm);
            if (save != null) {
                save.setReplaceTarget(null);
            }
        }

        if (mState.action == State.ACTION_OPEN_TREE) {
            final PickFragment pick = PickFragment.get(fm);
            if (pick != null && null != cwd) {
                final CharSequence displayName = (mState.stack.size() <= 1) && null != root
                        ? root.title : cwd.displayName;
                pick.setPickTarget(cwd, displayName);
            }
        }

        final MoveFragment move = MoveFragment.get(fm);
        if (move != null) {
            move.setReplaceTarget(cwd);
        }

        final RootsCommonFragment roots = RootsCommonFragment.get(fm);
        if (roots != null) {
            roots.onCurrentRootChanged();
        }

        updateActionBar();
        invalidateMenu();
        dumpStack();

    }

    private void enableBannerAds() {
        /*showBannerAds = !isPurchased() && !SHOW_NATIVE_ADS;*/

        showBannerAds = !SHOW_NATIVE_ADS;
    }

    private AppRate.OnShowListener mOnShowListener = new AppRate.OnShowListener() {
        @Override
        public void onRateAppShowing() {
            // View is shown
        }

        @Override
        public void onRateAppDismissed() {
            // User has dismissed it
        }

        @Override
        public void onRateAppClicked() {

        }
    };

    public void onStackPicked(DocumentStack stack) {
        try {
            // Update the restored stack to ensure we have freshest data
            stack.updateDocuments(getContentResolver());

            mState.stack = stack;
            mState.stackTouched = true;
            onCurrentDirectoryChanged(DirectoryFragment.ANIM_SIDE);

        } catch (FileNotFoundException e) {
            Log.w(TAG, "Failed to restore stack: " + e);

        }
    }

    public void onRootPicked(RootInfo root, RootInfo parentRoot) {
        mParentRoot = parentRoot;
        onRootPicked(root, true);
    }

    public void onRootPicked(RootInfo root, boolean closeDrawer) {

        if (null == root) {
            return;
        }
        // Clear entire backstack and start in new root
        mState.stack.root = root;
        mState.stack.clear();
        mState.stackTouched = true;

        if (RootInfo.isOtherRoot(root) || mRoots.isRecentsRoot(root)) {
            onCurrentDirectoryChanged(DirectoryFragment.ANIM_SIDE);
        } else {
            new PickRootTask(root).executeOnExecutor(getCurrentExecutor());
        }

        if (closeDrawer) {
            setRootsDrawerOpen(false);
        }


    }

    private class PickRootTask extends AsyncTask<Void, Void, DocumentInfo> {
        private RootInfo mRoot;

        public PickRootTask(RootInfo root) {
            mRoot = root;
        }

        @Override
        protected DocumentInfo doInBackground(Void... params) {
            try {
                final Uri uri = DocumentsContract.buildDocumentUri(
                        mRoot.authority, mRoot.documentId);
                return DocumentInfo.fromUri(getContentResolver(), uri);
            } catch (FileNotFoundException e) {
                Log.w(TAG, "Failed to find root", e);

                return null;
            }
        }

        @Override
        protected void onPostExecute(DocumentInfo result) {
            if (!Utils.isActivityAlive(DocumentsActivity.this)) {
                return;
            }
            if (result != null) {
                mState.stack.push(result);
                mState.stackTouched = true;
                onCurrentDirectoryChanged(DirectoryFragment.ANIM_SIDE);
            }
        }
    }

    private class UploadFileTask extends AsyncTask<Void, Void, Boolean> {
        private final DocumentInfo mCwd;
        private final String mMimeType;
        private final String mDisplayName;
        private final Uri mUri;

        public UploadFileTask(Uri uri, String name, String mimeType) {
            mCwd = getCurrentDirectory();
            mDisplayName = name;
            mMimeType = mimeType;
            mUri = uri;
        }

        @Override
        protected void onPreExecute() {
            setPending(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final ContentResolver resolver = getContentResolver();
            ContentProviderClient client = null;
            Boolean hadTrouble = false;
            try {
                client = DocumentsApplication.acquireUnstableProviderOrThrow(
                        resolver, mCwd.derivedUri.getAuthority());
                hadTrouble = !DocumentsContract.uploadDocument(
                        resolver, mCwd.derivedUri, mUri, mMimeType, mDisplayName);
            } catch (Exception e) {
                Log.w(DocumentsActivity.TAG, "Failed to upload document", e);

            } finally {
                ContentProviderClientCompat.releaseQuietly(client);
            }

            return hadTrouble;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Utils.showError(DocumentsActivity.this, R.string.upload_error);
            }
            setPending(false);
        }
    }

    public void onAppPicked(ResolveInfo info) {
        final Intent intent = new Intent(getIntent());
        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.setComponent(new ComponentName(
                info.activityInfo.applicationInfo.packageName, info.activityInfo.name));
        startActivityForResult(intent, CODE_FORWARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() code=" + resultCode);

        // Only relay back results when not canceled; otherwise stick around to
        // let the user pick another app/backend.
        if (requestCode == CODE_FORWARD && resultCode != RESULT_CANCELED) {

            // Remember that we last picked via external app
            final String packageName = getCallingPackageMaybeExtra();
            final ContentValues values = new ContentValues();
            values.put(RecentsProvider.ResumeColumns.EXTERNAL, 1);
            getContentResolver().insert(RecentsProvider.buildResume(packageName), values);

            // Pass back result to original caller
            setResult(resultCode, data);
            finish();
        } else if (requestCode == CODE_SETTINGS) {
            if (resultCode == RESULT_FIRST_USER) {
                recreate();
            }
        } else if (requestCode == ADD_STORAGE_REQUEST_CODE) {
            SAFManager.onActivityResult(this, requestCode, resultCode, data);
        } else if (requestCode == UPLOAD_FILE) {
            if (resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                final String name = FileUtils.getFilenameFromContentUri(this, uri);
                new UploadFileTask(uri, name,
                        FileUtils.getTypeForName(name)).executeOnExecutor(getCurrentExecutor());
            }
        } else if (requestCode == REQUEST_CONFIRM_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                mAuthenticated = true;
            } else {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onDocumentPicked(DocumentInfo doc) {
        final FragmentManager fm = getSupportFragmentManager();
        if (doc.isDirectory() || DocumentArchiveHelper.isSupportedArchiveType(doc.mimeType)) {
            mState.stack.push(doc);
            mState.stackTouched = true;
            onCurrentDirectoryChanged(DirectoryFragment.ANIM_DOWN);
            final MoveFragment move = MoveFragment.get(fm);
            if (move != null) {
                move.setReplaceTarget(doc);
            }
        } else if (mState.action == State.ACTION_OPEN || mState.action == State.ACTION_GET_CONTENT) {
            // Explicit file picked, return
            new ExistingFinishTask(doc.derivedUri).executeOnExecutor(getCurrentExecutor());
        } else if (mState.action == State.ACTION_BROWSE) {

            // Fall back to viewing
            final RootInfo rootInfo = getCurrentRoot();
            final Intent view = new Intent(Intent.ACTION_VIEW);
            view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            view.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (RootInfo.isMedia(rootInfo)) {
                view.setDataAndType(MediaDocumentsProvider.getMediaUriForDocumentId(doc.documentId), doc.mimeType);
            } else {
                Uri contentUri = null;
                if ((rootInfo.isStorage() || doc.isMedia()) && !TextUtils.isEmpty(doc.path)) {
                    contentUri = FileUtils.getContentUriFromFilePath(this, new File(doc.path).getAbsolutePath());
                }
                if (null == contentUri) {
                    contentUri = doc.derivedUri;
                }
                view.setDataAndType(contentUri, doc.mimeType);
            }
            if ((MimePredicate.mimeMatches(MimePredicate.SPECIAL_MIMES, doc.mimeType)
                    || !Utils.isIntentAvailable(this, view)) && !Utils.hasNougat()) {
                try {
                    File file = new File(doc.path);
                    view.setDataAndType(Uri.fromFile(file), doc.mimeType);
                } catch (Exception e) {
                    view.setDataAndType(doc.derivedUri, doc.mimeType);

                }
            }

            if (Utils.isIntentAvailable(this, view)) {
                //TODO: This temporarily fixes crash when the Activity that is opened is not
                // exported gives java.lang.SecurityException: Permission Denial:
                try {
                    Casty casty = DocumentsApplication.getInstance().getCasty();
                    if (casty.isConnected() && doc.isMedia()) {
                        CastUtils.addToQueue(casty,
                                CastUtils.buildMediaInfo(doc, getRoots().getPrimaryRoot()));
                        invalidateMenu();
                    } else {
                        startActivity(view);
                    }
                } catch (Exception e) {

                }
            } else {
                Utils.showError(this, R.string.toast_no_application);
            }
        } else if (mState.action == State.ACTION_CREATE) {
            // Replace selected file
            // TODO: null pointer crash
            SaveFragment.get(fm).setReplaceTarget(doc);
        } else if (mState.action == State.ACTION_MANAGE) {
            // First try managing the document; we expect manager to filter
            // based on authority, so we don't grant.
            final Intent manage = new Intent(DocumentsContract.ACTION_MANAGE_DOCUMENT);
            manage.setData(doc.derivedUri);

            if (Utils.isIntentAvailable(this, manage)) {
                try {
                    startActivity(manage);
                } catch (ActivityNotFoundException ex) {
                    // Fall back to viewing

                    final Intent view = new Intent(Intent.ACTION_VIEW);
                    view.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    view.setData(doc.derivedUri);

                    try {
                        startActivity(view);
                    } catch (ActivityNotFoundException ex2) {
                        Utils.showError(this, R.string.toast_no_application);

                    }
                }
            } else {
                Utils.showError(this, R.string.toast_no_application);
            }
        }
    }

    public void onDocumentsPicked(List<DocumentInfo> docs) {
        if (mState.action == State.ACTION_OPEN || mState.action == State.ACTION_GET_CONTENT || mState.action == State.ACTION_BROWSE) {
            final int size = docs.size();
            final Uri[] uris = new Uri[size];
            for (int i = 0; i < size; i++) {
                uris[i] = docs.get(i).derivedUri;
            }
            new ExistingFinishTask(uris).executeOnExecutor(getCurrentExecutor());
        }
    }

    public void onSaveRequested(DocumentInfo replaceTarget) {
        new ExistingFinishTask(replaceTarget.derivedUri).executeOnExecutor(getCurrentExecutor());
    }

    public void onSaveRequested(String mimeType, String displayName) {
        new CreateFinishTask(mimeType, displayName).executeOnExecutor(getCurrentExecutor());
    }

    public void onPickRequested(DocumentInfo pickTarget) {
        final Uri viaUri = DocumentsContract.buildTreeDocumentUri(pickTarget.authority,
                pickTarget.documentId);
        new PickFinishTask(viaUri).executeOnExecutor(getCurrentExecutor());
    }

    public void onMoveRequested(ArrayList<DocumentInfo> docs, DocumentInfo toDoc, boolean deleteAfter) {
        new MoveTask(docs, toDoc, deleteAfter).executeOnExecutor(getCurrentExecutor());
    }

    private void saveStackBlocking() {
        final ContentResolver resolver = getContentResolver();
        final ContentValues values = new ContentValues();

        final byte[] rawStack = DurableUtils.writeToArrayOrNull(mState.stack);
        if (mState.action == State.ACTION_CREATE || mState.action == State.ACTION_OPEN_TREE) {
            // Remember stack for last create
            values.clear();
            values.put(RecentsProvider.RecentColumns.KEY, mState.stack.buildKey());
            values.put(RecentsProvider.RecentColumns.STACK, rawStack);
            resolver.insert(RecentsProvider.buildRecent(), values);
        }

        // Remember location for next app launch
        final String packageName = getCallingPackageMaybeExtra();
        values.clear();
        values.put(RecentsProvider.ResumeColumns.STACK, rawStack);
        values.put(RecentsProvider.ResumeColumns.EXTERNAL, 0);
        resolver.insert(RecentsProvider.buildResume(packageName), values);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void onFinished(Uri... uris) {
        Log.d(TAG, "onFinished() " + Arrays.toString(uris));

        final Intent intent = new Intent();
        if (uris.length == 1) {
            intent.setData(uris[0]);
        } else if (uris.length > 1) {
            final ClipData clipData = new ClipData(
                    null, mState.acceptMimes, new ClipData.Item(uris[0]));
            for (int i = 1; i < uris.length; i++) {
                clipData.addItem(new ClipData.Item(uris[i]));
            }
            if (Utils.hasJellyBean()) {
                intent.setClipData(clipData);
            } else {
                intent.setData(uris[0]);
            }
        }

        if (mState.action == DocumentsActivity.State.ACTION_GET_CONTENT) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else if (mState.action == State.ACTION_OPEN_TREE) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    private class CreateFinishTask extends AsyncTask<Void, Void, Uri> {
        private final String mMimeType;
        private final String mDisplayName;

        public CreateFinishTask(String mimeType, String displayName) {
            mMimeType = mimeType;
            mDisplayName = displayName;
        }

        @Override
        protected void onPreExecute() {
            setPending(true);
        }

        @Override
        protected Uri doInBackground(Void... params) {
            final ContentResolver resolver = getContentResolver();
            final DocumentInfo cwd = getCurrentDirectory();

            ContentProviderClient client = null;
            Uri childUri = null;
            try {
                client = DocumentsApplication.acquireUnstableProviderOrThrow(
                        resolver, cwd.derivedUri.getAuthority());
                childUri = DocumentsContract.createDocument(
                        resolver, cwd.derivedUri, mMimeType, mDisplayName);
            } catch (Exception e) {
                Log.w(TAG, "Failed to create document", e);

            } finally {
                ContentProviderClientCompat.releaseQuietly(client);
            }

            if (childUri != null) {
                saveStackBlocking();
            }

            return childUri;
        }

        @Override
        protected void onPostExecute(Uri result) {
            if (!Utils.isActivityAlive(DocumentsActivity.this)) {
                return;
            }
            if (result != null) {
                onFinished(result);
            } else {
                final DocumentInfo cwd = getCurrentDirectory();
                if (!isSAFIssue(cwd.documentId)) {
                    Utils.showError(DocumentsActivity.this, R.string.save_error);
                }
            }
            setPending(false);
        }
    }

    private class ExistingFinishTask extends AsyncTask<Void, Void, Void> {
        private final Uri[] mUris;

        public ExistingFinishTask(Uri... uris) {
            mUris = uris;
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveStackBlocking();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            onFinished(mUris);
        }
    }

    private class PickFinishTask extends android.os.AsyncTask<Void, Void, Void> {
        private final Uri mUri;

        public PickFinishTask(Uri uri) {
            mUri = uri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveStackBlocking();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            onFinished(mUri);
        }
    }

    private class MoveTask extends AsyncTask<Void, Void, Boolean> {
        private final DocumentInfo toDoc;
        private final ArrayList<DocumentInfo> docs;
        private boolean deleteAfter;

        public MoveTask(ArrayList<DocumentInfo> docs, DocumentInfo toDoc, boolean deleteAfter) {
            this.docs = docs;
            this.toDoc = toDoc;
            this.deleteAfter = deleteAfter;
        }

        @Override
        protected void onPreExecute() {
            setMovePending(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            final ContentResolver resolver = getContentResolver();
            final DocumentInfo cwd = null == toDoc ? getCurrentDirectory() : toDoc;

            boolean hadTrouble = false;
            for (DocumentInfo doc : docs) {

                if (!doc.isMoveSupported()) {
                    Log.w(TAG, "Skipping " + doc);
                    hadTrouble = true;
                    continue;
                }

                try {
                    if (deleteAfter) {
                        hadTrouble = DocumentsContract.moveDocument(resolver, doc.derivedUri, null,
                                cwd.derivedUri) == null;
                    } else {
                        hadTrouble = DocumentsContract.copyDocument(resolver, doc.derivedUri,
                                cwd.derivedUri) == null;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to move " + doc);
                    hadTrouble = true;

                }
            }

            Bundle params2 = new Bundle();
            params2.putBoolean(FILE_MOVE, deleteAfter);
            params2.putInt(FILE_COUNT, docs.size());

            return hadTrouble;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!Utils.isActivityAlive(DocumentsActivity.this)) {
                return;
            }
            if (result) {
                //if(!isSAFIssue(toDoc.documentId)){
                Utils.showError(DocumentsActivity.this, R.string.save_error);
                //}
            }
            MoveFragment.hide(getSupportFragmentManager());
            setMovePending(false);
            refreshData();
        }
    }

    public void setMovePending(boolean pending) {
        final MoveFragment move = MoveFragment.get(getSupportFragmentManager());
        if (move != null) {
            move.setPending(pending);
        }
    }

    private void dumpStack() {
        Log.d(TAG, "Current stack: ");
        Log.d(TAG, " * " + mState.stack.root);
        for (DocumentInfo doc : mState.stack) {
            Log.d(TAG, " +-- " + doc);
        }
    }

    public static DocumentsActivity get(Fragment fragment) {
        return (DocumentsActivity) fragment.getActivity();
    }

    private final Handler handler = new Handler();
    private Drawable oldBackground;

    private void changeActionBarColor() {

        int color = SettingsActivity.getPrimaryColor(this);
        Drawable colorDrawable = new ColorDrawable(color);

        if (oldBackground == null) {
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        } else {
            TransitionDrawable td = new TransitionDrawable(new Drawable[]{oldBackground, colorDrawable});
            getSupportActionBar().setBackgroundDrawable(td);
            td.startTransition(200);
        }

        oldBackground = colorDrawable;

        setUpStatusBar();
    }

    private Drawable.Callback drawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            getSupportActionBar().setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            handler.postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            handler.removeCallbacks(what);
        }
    };

    public boolean getActionMode() {
        return mActionMode;
    }

    public void setActionMode(boolean actionMode) {
        mActionMode = actionMode;
        mToolbar.setVisibility(actionMode ? View.INVISIBLE : View.VISIBLE);
    }

    public void invalidateMenu() {
        supportInvalidateOptionsMenu();
        mActionMenu.setVisibility(showActionMenu() ? View.VISIBLE : View.GONE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpStatusBar() {
        int color = Utils.getStatusBarColor(SettingsActivity.getPrimaryColor(this));
        if (Utils.hasLollipop()) {
            getWindow().setStatusBarColor(color);
        } else if (Utils.hasKitKat()) {
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setTintColor(color);
            systemBarTintManager.setStatusBarTintEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setUpDefaultStatusBar() {
        int color = ContextCompat.getColor(this, R.color.alertColor);
        if (Utils.hasLollipop()) {
            getWindow().setStatusBarColor(color);
        } else if (Utils.hasKitKat()) {
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setTintColor(Utils.getStatusBarColor(color));
            systemBarTintManager.setStatusBarTintEnabled(true);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void initControls() {
        mActionMenu = (FloatingActionsMenu) findViewById(R.id.fabs);
        mActionMenu.setMenuListener(mMenuListener);
    }

    public void upadateActionItems(RecyclerView currentView) {

        mActionMenu.attachToListView(currentView);
        RootInfo root = getCurrentRoot();
        if (null != root && root.isCloudStorage()) {
            mActionMenu.newNavigationMenu(R.menu.menu_fab_cloud);
        }
        int defaultColor = SettingsActivity.getPrimaryColor(this);
        ViewCompat.setNestedScrollingEnabled(currentView, true);
        mActionMenu.show();
        mActionMenu.setVisibility(showActionMenu() ? View.VISIBLE : View.GONE);
        mActionMenu.setBackgroundTintList(SettingsActivity.getAccentColor());
        mActionMenu.setSecondaryBackgroundTintList(Utils.getActionButtonColor(defaultColor));
    }


    private boolean showActionMenu() {
        final RootInfo root = getCurrentRoot();
        return !RootInfo.isOtherRoot(root) &&
                isCreateSupported() &&
                (null != root && (!root.isRootedStorage() || Utils.isRooted()))
                && mState.currentSearch == null;
    }



    private SimpleMenuListenerAdapter mMenuListener = new SimpleMenuListenerAdapter() {

        @Override
        public boolean onMenuItemSelected(MenuItem menuItem) {
            Bundle params = new Bundle();
            switch (menuItem.getItemId()) {
                case R.id.fab_create_file:
                    onStateChanged();
                    createFile();
                    mActionMenu.closeMenu();
                    break;

                case R.id.fab_upload_file:
                    onStateChanged();
                    uploadFile();
                    mActionMenu.closeMenu();
                    break;

                case R.id.fab_create_folder:
                    createFolder();
                    mActionMenu.closeMenu();
                    break;

            }
            return false;
        }
    };

    public void setSAFPermissionRequested(boolean SAFPermissionRequested) {
        this.SAFPermissionRequested = SAFPermissionRequested;
    }

    public boolean getSAFPermissionRequested() {
        return SAFPermissionRequested;
    }
}
