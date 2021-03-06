package filemanager.fileexplorer.pro.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import filemanager.fileexplorer.pro.model.GroupInfo;
import filemanager.fileexplorer.pro.model.RootInfo;
import filemanager.fileexplorer.pro.fragment.RootsFragment;


public class RootsExpandableAdapter extends BaseExpandableListAdapter {

    final List<GroupInfo> group = new ArrayList<>();

    public RootsExpandableAdapter(Context context, Collection<RootInfo> roots, Intent includeAppss) {
        processRoots(roots);
    }

    private void processRoots(Collection<RootInfo> roots) {
        List<GroupInfo> groupRoots = new ArrayList<>();
        final List<RootsFragment.Item> home = new ArrayList<>();
        final List<RootsFragment.Item> phone = new ArrayList<>();
        final List<RootsFragment.Item> recent = new ArrayList<>();
        final List<RootsFragment.Item> connection = new ArrayList<>();
        final List<RootsFragment.Item> transfer = new ArrayList<>();
        final List<RootsFragment.Item> receive = new ArrayList<>();
        final List<RootsFragment.Item> rooted = new ArrayList<>();
        final List<RootsFragment.Item> appbackup = new ArrayList<>();
        final List<RootsFragment.Item> usb = new ArrayList<>();

        final List<RootsFragment.Item> storage = new ArrayList<>();
        final List<RootsFragment.Item> secondaryStorage = new ArrayList<>();
        final List<RootsFragment.Item> network = new ArrayList<>();
        final List<RootsFragment.Item> cloud = new ArrayList<>();
        final List<RootsFragment.Item> apps = new ArrayList<>();
        final List<RootsFragment.Item> libraryMedia = new ArrayList<>();
        final List<RootsFragment.Item> libraryNonMedia = new ArrayList<>();
        final List<RootsFragment.Item> folders = new ArrayList<>();
        final List<RootsFragment.Item> bookmarks = new ArrayList<>();
        final List<RootsFragment.Item> messengers = new ArrayList<>();
        final List<RootsFragment.Item> cast = new ArrayList<>();

        for (RootInfo root : roots) {
            if (root.isHome()) {
                home.add(new RootsFragment.RootItem(root));
            } else if (root.isRecents()) {
                if (recent.size() == 0) {
                    recent.add(new RootsFragment.RootItem(root));
                }
            } else if (root.isConnections()) {
                connection.add(new RootsFragment.RootItem(root));
            } else if (root.isTransfer()) {
                transfer.add(new RootsFragment.RootItem(root));
            } else if (root.isReceiveFolder()) {
                receive.add(new RootsFragment.RootItem(root));
            } else if (root.isCast()) {
                cast.add(new RootsFragment.RootItem(root));
            } else if (root.isRootedStorage()) {
                rooted.add(new RootsFragment.RootItem(root));
            } else if (root.isPhoneStorage()) {
                phone.add(new RootsFragment.RootItem(root));
            } else if (root.isAppBackupFolder()) {
                appbackup.add(new RootsFragment.RootItem(root));
            } else if (root.isUsbStorage()) {
                usb.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isLibraryMedia(root)) {
                libraryMedia.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isLibraryNonMedia(root)) {
                libraryNonMedia.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isFolder(root)) {
                folders.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isBookmark(root)) {
                bookmarks.add(new RootsFragment.BookmarkItem(root));
            } else if (RootInfo.isStorage(root)) {
                if (root.isSecondaryStorage()) {
                    secondaryStorage.add(new RootsFragment.RootItem(root));
                } else {
                    storage.add(new RootsFragment.RootItem(root));
                }
            } else if (RootInfo.isApps(root)) {
                apps.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isNetwork(root)) {
                network.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isCloud(root)) {
                cloud.add(new RootsFragment.RootItem(root));
            } else if (RootInfo.isLibraryExtra(root)) {
                messengers.add(new RootsFragment.RootItem(root));
            }
        }

        if (!home.isEmpty() || !storage.isEmpty() || !phone.isEmpty() || !rooted.isEmpty()) {
            home.addAll(storage);
            home.addAll(secondaryStorage);
            home.addAll(usb);
            home.addAll(phone);
            home.addAll(rooted);
            groupRoots.add(new GroupInfo("Storage", home));
        }

        if (!messengers.isEmpty()) {
            groupRoots.add(new GroupInfo("Apps Media", messengers));
        }

        if (!transfer.isEmpty()) {
            transfer.addAll(receive);
            groupRoots.add(new GroupInfo("Transfer", transfer));
        }

             network.addAll(cast);

        network.addAll(connection);
        network.addAll(cloud);
        groupRoots.add(new GroupInfo("Network & Cloud", network));

        if (!apps.isEmpty()) {
            if (!appbackup.isEmpty()) {
                apps.addAll(appbackup);
            }
            groupRoots.add(new GroupInfo("Apps", apps));
        }

        if (!libraryMedia.isEmpty() || !libraryNonMedia.isEmpty()) {
            recent.addAll(libraryMedia);
            recent.addAll(libraryNonMedia);
            groupRoots.add(new GroupInfo("Library", recent));
        } else if (!recent.isEmpty()) {
            groupRoots.add(new GroupInfo("Library", recent));
        }

        if (!folders.isEmpty()) {
            folders.addAll(bookmarks);
            groupRoots.add(new GroupInfo("Folders", folders));
        }

        group.clear();
        group.addAll(groupRoots);
    }

    @Override
    public int getGroupCount() {
       // Log.e("group.size()", "===" + group.size());
        return group.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group.get(groupPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
       // Log.e("getGroupId", "===" + groupPosition);
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        RootsFragment.GroupItem item = new RootsFragment.GroupItem((GroupInfo) getGroup(groupPosition));

        //Log.e("getGroupView", "Label===" + item.mLabel);

        return item.getView(convertView, parent);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return group.get(groupPosition).itemList.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return group.get(groupPosition).itemList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final RootsFragment.Item item = (RootsFragment.Item) getChild(groupPosition, childPosition);

       // Log.e("getChildView", "Label===" );
        return item.getView(convertView, parent);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    public void setData(Collection<RootInfo> roots) {
        processRoots(roots);
        notifyDataSetChanged();
    }
}