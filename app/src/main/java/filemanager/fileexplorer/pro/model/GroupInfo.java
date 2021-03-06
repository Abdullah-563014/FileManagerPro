package filemanager.fileexplorer.pro.model;

import java.util.List;

import filemanager.fileexplorer.pro.fragment.RootsFragment;

/**
 * Created by HaKr on 07/08/16.
 */

public class GroupInfo {
    public String label;
    public List<RootsFragment.Item> itemList;

    public GroupInfo(String text, List<RootsFragment.Item> list){
        label = text;
        itemList = list;
    }
}
