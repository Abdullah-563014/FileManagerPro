

package filemanager.fileexplorer.pro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import filemanager.fileexplorer.pro.misc.RootsCache;
import filemanager.fileexplorer.pro.misc.Utils;
import filemanager.fileexplorer.pro.provider.ExternalStorageProvider;
import filemanager.fileexplorer.pro.provider.UsbStorageProvider;

public class MountReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		RootsCache.updateRoots(context, ExternalStorageProvider.AUTHORITY);
		if(Utils.checkUSBDevices()) {
			RootsCache.updateRoots(context, UsbStorageProvider.AUTHORITY);
		}
	}
}
