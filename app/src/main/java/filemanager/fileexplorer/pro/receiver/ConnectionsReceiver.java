package filemanager.fileexplorer.pro.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import filemanager.fileexplorer.pro.misc.ConnectionUtils;
import filemanager.fileexplorer.pro.misc.NotificationUtils;
import filemanager.fileexplorer.pro.service.ConnectionsService;
import filemanager.fileexplorer.pro.service.TransferService;
import filemanager.fileexplorer.pro.transfer.TransferHelper;

import static filemanager.fileexplorer.pro.misc.ConnectionUtils.ACTION_FTPSERVER_STARTED;
import static filemanager.fileexplorer.pro.misc.ConnectionUtils.ACTION_FTPSERVER_STOPPED;
import static filemanager.fileexplorer.pro.misc.ConnectionUtils.ACTION_START_FTPSERVER;
import static filemanager.fileexplorer.pro.misc.ConnectionUtils.ACTION_STOP_FTPSERVER;
import static filemanager.fileexplorer.pro.misc.NotificationUtils.FTP_NOTIFICATION_ID;

public class ConnectionsReceiver extends BroadcastReceiver {

    static final String TAG = ConnectionsReceiver.class.getSimpleName();

    public ConnectionsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ACTION_START_FTPSERVER.equals(action)) {
            Intent serverService = new Intent(context, ConnectionsService.class);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                serverService.putExtras(extras);
            }
            if (!ConnectionUtils.isServerRunning(context)) {
                context.startService(serverService);
            }
        } else if (ACTION_STOP_FTPSERVER.equals(action)) {
            Intent serverService = new Intent(context, ConnectionsService.class);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                serverService.putExtras(extras);
            }
            context.stopService(serverService);
        } else if (ACTION_FTPSERVER_STARTED.equals(action)) {
            NotificationUtils.createFtpNotification(context, intent, FTP_NOTIFICATION_ID);
        } else if (ACTION_FTPSERVER_STOPPED.equals(action)) {
            NotificationUtils.removeNotification(context, FTP_NOTIFICATION_ID);
        } else if (TransferHelper.ACTION_START_LISTENING.equals(action)) {
            Intent serverService = new Intent(context, TransferService.class);
            serverService.setAction(action);
            if (!TransferHelper.isServerRunning(context)) {
                context.startService(serverService);
            }
        } else if (TransferHelper.ACTION_STOP_LISTENING.equals(action)) {
            Intent serverService = new Intent(context, TransferService.class);
            serverService.setAction(action);
            context.startService(serverService);
        }
    }
}
