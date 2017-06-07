package com.example.owl.adapters;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.owl.fragments.StackPhotoPagerFragment;

import java.util.ArrayList;

/**
 * Created by Zach on 5/24/17.
 */

public class StackPhotoPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Bitmap> mData = new ArrayList<>();

    public StackPhotoPagerAdapter(FragmentManager fm, ArrayList<Bitmap> data) {
        super(fm);
        this.mData = data;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new StackPhotoPagerFragment();

        // Attach data to fragment that will be used to populate the fragment layouts
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putParcelable("imageBitmap", mData.get(position));

        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Called when the host view is attempting to determine if an item's position
     * has changed. Returns {@link #POSITION_UNCHANGED} if the position of the given
     * item has not changed or {@link #POSITION_NONE} if the item is no longer present
     * in the adapter.
     * <p>
     * <p>The default implementation assumes that items will never
     * change position and always returns {@link #POSITION_UNCHANGED}.
     *
     * @param object Object representing an item, previously returned by a call to
     *               {@link #instantiateItem(View, int)}.
     * @return object's new position index from [0, {@link #getCount()}),
     * {@link #POSITION_UNCHANGED} if the object's position has not changed,
     * or {@link #POSITION_NONE} if the item is no longer present.
     */
    @Override
    public int getItemPosition(Object object) {
        //return super.getItemPosition(object);
        StackPhotoPagerFragment fragment = (StackPhotoPagerFragment) object;
        Bundle args = fragment.getArguments();
        Bitmap bitmap = args.getParcelable("imageBitmap");

        int position = mData.indexOf(bitmap);

        if (position >= 0) {
            return position;
        } else {
            return POSITION_NONE;
        }
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mData.size();
    }
}
