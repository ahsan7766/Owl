package com.example.owl.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.owl.R;
import com.example.owl.fragments.StackPhotoPagerFragment;

import java.util.ArrayList;

/**
 * Created by Zach on 5/24/17.
 */

public class StackPhotoPagerAdapter extends PagerAdapter { //extends FragmentStatePagerAdapter {

    private Context mContext;
    private ArrayList<Bitmap> mData = new ArrayList<>();

    /*
    public StackPhotoPagerAdapter(FragmentManager fm, ArrayList<Bitmap> data) {
        super(fm);
        this.mData = data;
    }
    */
    public StackPhotoPagerAdapter(Context context, ArrayList<Bitmap> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //Bitmap bitmap = mData.get(position);
        //LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        //ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(bitmap.getLa)
        ImageView imageView = new ImageView(mContext);
        imageView.findViewById(R.id.image);
        imageView.setImageBitmap(mData.get(position));

        container.addView(imageView);

        return imageView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        Drawable drawable = imageView.getDrawable();
        if(drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable != null) {
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if(bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
            }
        }
        ((ViewPager) container).removeView(view);
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    /*
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
    */

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
    /*
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
    */

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mData.size();
    }



}
