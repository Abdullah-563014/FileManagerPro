

package filemanager.fileexplorer.pro.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;

/**
 * Adapter that combines multiple adapters as sections, asking each section to
 * provide a header, and correctly handling item types across child adapters.
 */
public class SectionedListAdapter extends BaseAdapter {
    private ArrayList<SectionAdapter> mSections = new ArrayList<>();

    public interface SectionAdapter extends ListAdapter {
        View getHeaderView(View convertView, ViewGroup parent);
    }

    public void clearSections() {
        mSections.clear();
        notifyDataSetChanged();
    }

    /**
     * After mutating sections, you <em>must</em>
     * {@link AdapterView#setAdapter(android.widget.Adapter)} to correctly
     * recount view types.
     */
    public void addSection(SectionAdapter adapter) {
        mSections.add(adapter);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int count = 0;
        final int size = mSections.size();
        for (int i = 0; i < size; i++) {
            count += mSections.get(i).getCount() + 1;
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        final int size = mSections.size();
        for (int i = 0; i < size; i++) {
            final SectionAdapter section = mSections.get(i);
            final int sectionSize = section.getCount() + 1;

            // Check if position inside this section
            if (position == 0) {
                return section;
            } else if (position < sectionSize) {
                return section.getItem(position - 1);
            }

            // Otherwise jump into next section
            position -= sectionSize;
        }
        throw new IllegalStateException("Unknown position " + position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int size = mSections.size();
        for (int i = 0; i < size; i++) {
            final SectionAdapter section = mSections.get(i);
            final int sectionSize = section.getCount() + 1;

            // Check if position inside this section
            if (position == 0) {
                return section.getHeaderView(convertView, parent);
            } else if (position < sectionSize) {
                return section.getView(position - 1, convertView, parent);
            }

            // Otherwise jump into next section
            position -= sectionSize;
        }
        throw new IllegalStateException("Unknown position " + position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        final int size = mSections.size();
        for (int i = 0; i < size; i++) {
            final SectionAdapter section = mSections.get(i);
            final int sectionSize = section.getCount() + 1;

            // Check if position inside this section
            if (position == 0) {
                return false;
            } else if (position < sectionSize) {
                return section.isEnabled(position - 1);
            }

            // Otherwise jump into next section
            position -= sectionSize;
        }
        throw new IllegalStateException("Unknown position " + position);
    }

    @Override
    public int getItemViewType(int position) {
        int type = 1;
        final int size = mSections.size();
        for (int i = 0; i < size; i++) {
            final SectionAdapter section = mSections.get(i);
            final int sectionSize = section.getCount() + 1;

            // Check if position inside this section
            if (position == 0) {
                return 0;
            } else if (position < sectionSize) {
                return type + section.getItemViewType(position - 1);
            }

            // Otherwise jump into next section
            position -= sectionSize;
            type += section.getViewTypeCount();
        }
        throw new IllegalStateException("Unknown position " + position);
    }

    @Override
    public int getViewTypeCount() {
        int count = 1;
        final int size = mSections.size();
        for (int i = 0; i < size; i++) {
            count += mSections.get(i).getViewTypeCount();
        }
        return count;
    }
}
