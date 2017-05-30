package com.example.owl.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.owl.fragments.StackPhotoPagerFragment;

/**
 * Created by admin on 5/24/17.
 */

public class StackPhotoPagerAdapter extends FragmentStatePagerAdapter {

    public StackPhotoPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        return new StackPhotoPagerFragment();
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return 6;
    }
}
