package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.fragments.FeedFragment;
import com.ourwayoflife.owl.models.Photo;

import java.util.ArrayList;

/**
 * Created by Zach on 5/24/17.
 */

public class StackPhotoPagerAdapter extends PagerAdapter { //extends FragmentStatePagerAdapter {

    private static final String TAG = StackPhotoPagerAdapter.class.getName();

    private StackPhotoPagerAdapter.PageClickListener mClickListener;

    private Context mContext;
    private ArrayList<Photo> mData = new ArrayList<>();

    /*
    public StackPhotoPagerAdapter(FragmentManager fm, ArrayList<Bitmap> data) {
        super(fm);
        this.mData = data;
    }
    */
    public StackPhotoPagerAdapter(Context context, ArrayList<Photo> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        Photo photo = mData.get(position);

        //Bitmap bitmap = mData.get(position);
        //LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        //ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(bitmap.getLa)

        ImageView imageView = new ImageView(mContext);
        imageView.findViewById(R.id.image);

        // Check if the photo or the photo string is null
        if (photo == null && photo.getPhoto() == null || photo.getPhoto().isEmpty()) {
            // If the photo string is not found, don't load the bitmap
            // Instead show the text view, and set the text
            TextView textView = generateMessageTextView(true);
            container.addView(textView);
            return textView;
        }

        // Check if photo is deleted
        if ( photo.isDeleted()) {
            // If the photo is deleted, don't load the bitmap
            // Instead show the text view, and set the text
            TextView textView = generateMessageTextView(true);
            container.addView(textView);
            return textView;
        }


        //imageView.setImageBitmap(mData.get(position));

        // Convert photo string to bitmap
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inSampleSize = 4;

        //Check if the bitmap is cached
        bitmap = FeedFragment.getBitmapFromMemCache(photo.getPhotoId());
        if (bitmap == null) {
            //Bitmap is not cached.  Have to download

            // Convert the photo string to a bitmap
            String photoString = photo.getPhoto();
            if (photoString == null || photoString.isEmpty()) {
                // Photo string not found
                // Generate TextView with error message, add to view and return it
                TextView textView = generateMessageTextView(false);
                container.addView(textView);
                return textView;
            }
            try {
                byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, options);

                //Add bitmap to the cache
                FeedFragment.addBitmapToMemoryCache(String.valueOf(photo.getPhotoId()), bitmap);

            } catch (Exception e) {
                Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
                // Generate TextView with error message, add to view and return it
                TextView textView = generateMessageTextView(false);
                container.addView(textView);
                return textView;
            }
        }

        if (bitmap == null) {
            // Bitmap should be loaded and not null now.  This is a double check
            TextView textView = generateMessageTextView(false);
            container.addView(textView);
            return textView;
        }

        imageView.setImageBitmap(bitmap);

        container.addView(imageView);

        //imageView.setTag(photo);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) mClickListener.onPageClick(view, position);
            }
        });


        return imageView;

    }

    // Returns a text view with a message for the viewpager to use for deleted/error photos
    private TextView generateMessageTextView(boolean isDeleted) {
        // If the photo string is not found, don't load the bitmap
        // Instead show the text view, and set the text
        TextView textView = new TextView(mContext);
        textView.findViewById(R.id.text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(24f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        // If the photo is deleted, show the "Deleted" message.  Otherwise, show the error message
        textView.setText(isDeleted ? mContext.getString(R.string.message_content_deleted) : mContext.getString(R.string.message_error_load_content));

        return textView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        Log.d(TAG, "Destroying Item In Position " + position);
        View view = (View) object;

        // Looks like we don't have to recycle the bitmap after all..
        /*
        //ImageView imageView = (ImageView) view.findViewById(R.id.image);
        ImageView imageView = (ImageView) object;
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null) {
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        */

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
    //@Override
    //public int getItemPosition(Object object) {
    //return super.getItemPosition(object);
        /*
        StackPhotoPagerFragment fragment = (StackPhotoPagerFragment) object;
        Bundle args = fragment.getArguments();
        Bitmap bitmap = args.getParcelable("imageBitmap");

        int position = mData.indexOf(bitmap);

        if (position >= 0) {
            return position;
        } else {
            return POSITION_NONE;
        }
        */

        /*
        View o = (View) object;
        int index = mData.indexOf(o.getTag());
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
            */
    //}


    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mData.size();
    }


    // allows clicks events to be caught
    public void setClickListener(StackPhotoPagerAdapter.PageClickListener pageClickListener) {
        this.mClickListener = pageClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface PageClickListener {
        void onPageClick(View view, int position);
    }

}
