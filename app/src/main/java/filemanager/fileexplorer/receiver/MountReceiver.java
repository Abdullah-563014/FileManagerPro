

package filemanager.fileexplorer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import filemanager.fileexplorer.misc.RootsCache;
import filemanager.fileexplorer.misc.Utils;
import filemanager.fileexplorer.provider.ExternalStorageProvider;
import filemanager.fileexplorer.provider.UsbStorageProvider;

public class MountReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		RootsCache.updateRoots(context, ExternalStorageProvider.AUTHORITY);
		if(Utils.checkUSBDevices()) {
			RootsCache.updateRoots(context, UsbStorageProvider.AUTHORITY);
		}
	}
}
