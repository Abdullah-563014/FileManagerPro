package filemanager.fileexplorer.pro.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import filemanager.fileexplorer.pro.model.RootInfo;
import filemanager.fileexplorer.pro.setting.SettingsActivity;
import filemanager.fileexplorer.pro.fragment.RootsFragment;

public class RootsAdapter extends ArrayAdapter<RootsFragment.Item> {

    public RootsAdapter(Context context, Collection<RootInfo> roots, Intent includeAppss) {
        super(context, 0);

        int defaultColor = SettingsActivity.getPrimaryColor(context);
        RootsFragment.RootItem recents = null;
        RootsFragment.RootItem images = null;
        RootsFragment.RootItem videos = null;
        RootsFragment.RootItem audio = null;
        RootsFragment.RootItem downloads = null;
        RootsFragment.RootItem root_root = null;
        RootsFragment.RootItem phone = null;

        final List<RootInfo> clouds = new ArrayList<>();
        final List<RootInfo> locals = new ArrayList<>();
        final List<RootInfo> extras = new ArrayList<>();
        final List<RootInfo> bookmarks = new ArrayList<>();

        for (RootInfo root : roots) {
            if (root.isRecents()) {
                recents = new RootsFragment.RootItem(root, defaultColor);
            } else if (root.isBluetoothFolder() || root.isDownloadsFolder() || root.isAppBackupFolder()) {
                extras.add(root);
            } else if (root.isBookmarkFolder()) {
                bookmarks.add(root);
            } else if (root.isPhoneStorage()) {
                phone = new RootsFragment.RootItem(root, defaultColor);
            } else if (root.isStorage()) {
                locals.add(root);
            } else if (root.isRootedStorage()) {
                root_root = new RootsFragment.RootItem(root, defaultColor);
            } else if (root.isDownloads()) {
                downloads = new RootsFragment.RootItem(root, defaultColor);
            } else if (root.isImages()) {
                images = new RootsFragment.RootItem(root, defaultColor);
            } else if (root.isVideos()) {
                videos = new RootsFragment.RootItem(root, defaultColor);
            } else if (root.isAudio()) {
                audio = new RootsFragment.RootItem(root, defaultColor);
            } else {
                clouds.add(root);
            }
        }

        final RootsFragment.RootComparator comp = new RootsFragment.RootComparator();
        Collections.sort(clouds, comp);
        //Collections.sort(locals, comp);
        //Collections.reverse(locals);

        for (RootInfo local : locals) {
            add(new RootsFragment.RootItem(local, defaultColor));
        }
        if (phone != null) add(phone);

        for (RootInfo extra : extras) {
            add(new RootsFragment.RootItem(extra, defaultColor));
        }

        if (root_root != null) {
            add(new RootsFragment.SpacerItem());
            add(root_root);
        }

        if (bookmarks.size() > 0) {
            add(new RootsFragment.SpacerItem());
            for (RootInfo bookmark : bookmarks) {
                add(new RootsFragment.BookmarkItem(bookmark));
            }
        }

        add(new RootsFragment.SpacerItem());
        if (recents != null) add(recents);
        if (images != null) add(images);
        if (videos != null) add(videos);
        if (audio != null) add(audio);
        if (downloads != null) add(downloads);

        //if (includeApps == null) {
        add(new RootsFragment.SpacerItem());
        for (RootInfo cloud : clouds) {
            add(new RootsFragment.RootItem(cloud, defaultColor));
        }
/*                final PackageManager pm = context.getPackageManager();
                final List<ResolveInfo> infos = pm.queryIntentActivities(
                        includeApps, PackageManager.MATCH_DEFAULT_ONLY);

                final List<AppItem> apps = Lists.newArrayList();

                // Omit ourselves from the list
                for (ResolveInfo info : infos) {
                    if (!context.getPackageName().equals(info.activityInfo.packageName)) {
                        apps.add(new AppItem(info));
                    }
                }

                if (apps.size() > 0) {
                    add(new SpacerItem());
                    for (Item item : apps) {
                        add(item);
                    }
                }*/
        //}
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final RootsFragment.Item item = getItem(position);
        return item.getView(convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != 1;
    }

    @Override
    public int getItemViewType(int position) {
        final RootsFragment.Item item = getItem(position);
        if (item instanceof RootsFragment.RootItem || item instanceof RootsFragment.AppItem) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}