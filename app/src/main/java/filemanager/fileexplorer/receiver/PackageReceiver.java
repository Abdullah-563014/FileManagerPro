

package filemanager.fileexplorer.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import filemanager.fileexplorer.provider.RecentsProvider;

/**
 * Clean up {@link RecentsProvider} when packages are removed.
 */
public class PackageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final ContentResolver resolver = context.getContentResolver();

        final String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
            resolver.call(RecentsProvider.buildRecent(), RecentsProvider.METHOD_PURGE, null, null);

        } else if (Intent.ACTION_PACKAGE_DATA_CLEARED.equals(action)) {
            final Uri data = intent.getData();
            if (data != null) {
                final String packageName = data.getSchemeSpecificPart();
                resolver.call(RecentsProvider.buildRecent(), RecentsProvider.METHOD_PURGE_PACKAGE,
                        packageName, null);
            }
        }
    }
}
