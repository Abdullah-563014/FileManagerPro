

package filemanager.fileexplorer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.DateUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.collection.ArrayMap;

import com.cloudrail.si.CloudRail;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import filemanager.fileexplorer.cast.Casty;
import filemanager.fileexplorer.misc.ContentProviderClientCompat;
import filemanager.fileexplorer.misc.NotificationUtils;
import filemanager.fileexplorer.misc.RootsCache;
import filemanager.fileexplorer.misc.SAFManager;
import filemanager.fileexplorer.misc.ThumbnailCache;
import filemanager.fileexplorer.misc.Utils;
import filemanager.fileexplorer.server.SimpleWebServer;

public class DocumentsApplication extends Application {


	private static final long PROVIDER_ANR_TIMEOUT = 20 * DateUtils.SECOND_IN_MILLIS;
    private static DocumentsApplication sInstance;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private RootsCache mRoots;
    private ArrayMap<Integer, Long> mSizes = new ArrayMap<Integer, Long>();
    private SAFManager mSAFManager;
    private Point mThumbnailsSize;
    private ThumbnailCache mThumbnailCache;

    private SimpleWebServer simpleWebServer;
    private boolean isStarted;
    private Casty mCasty;

    public static RootsCache getRootsCache(Context context) {
        return ((DocumentsApplication) context.getApplicationContext()).mRoots;
    }

    public static RootsCache getRootsCache() {
        return ((DocumentsApplication) DocumentsApplication.getInstance().getApplicationContext()).mRoots;
    }

    public static ArrayMap<Integer, Long> getFolderSizes() {
        return getInstance().mSizes;
    }

    public static SAFManager getSAFManager(Context context) {
        return ((DocumentsApplication) context.getApplicationContext()).mSAFManager;
    }

    public static ThumbnailCache getThumbnailCache(Context context) {
        final DocumentsApplication app = (DocumentsApplication) context.getApplicationContext();
        return app.mThumbnailCache;
    }

    public static ThumbnailCache getThumbnailsCache(Context context, Point size) {
        return getThumbnailCache(context);
    }

    public static ContentProviderClient acquireUnstableProviderOrThrow(
            ContentResolver resolver, String authority) throws RemoteException {
    	final ContentProviderClient client = ContentProviderClientCompat.acquireUnstableContentProviderClient(resolver, authority);
        if (client == null) {
            throw new RemoteException("Failed to acquire provider for " + authority);
        }
        ContentProviderClientCompat.setDetectNotResponding(client, PROVIDER_ANR_TIMEOUT);
        return client;
    }

    @Override
    public void onCreate() {
        Utils.setAppThemeStyle(getBaseContext());
        super.onCreate();

        FirebaseAnalytics.getInstance(this);
        FirebaseCrashlytics.getInstance();
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);





        sInstance = this;
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
        CloudRail.setAppKey(BuildConfig.LICENSE_KEY);

        mRoots = new RootsCache(this);
        mRoots.updateAsync();

        mSAFManager = new SAFManager(this);

        mThumbnailCache = new ThumbnailCache(memoryClassBytes / 4);

        final IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
        packageFilter.addDataScheme("package");
        registerReceiver(mCacheReceiver, packageFilter);

        final IntentFilter localeFilter = new IntentFilter();
        localeFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(mCacheReceiver, localeFilter);



        if(Utils.hasOreo()) {
            NotificationUtils.createNotificationChannels(this);
        }
    }

    public static synchronized DocumentsApplication getInstance() {
        return sInstance;
    }

    public void initCasty(Activity activity) {
        mCasty = Casty.create(activity);
    }

    public Casty getCasty() {
        return mCasty;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        mThumbnailCache.onTrimMemory(level);
    }

    private BroadcastReceiver mCacheReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Uri data = intent.getData();
            if (data != null) {
                final String authority = data.getAuthority();
                mRoots.updateAuthorityAsync(authority);
            } else {
                mRoots.updateAsync();
            }
        }
    };


 }
