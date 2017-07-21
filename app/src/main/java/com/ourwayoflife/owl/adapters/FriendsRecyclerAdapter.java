package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.fragments.FeedFragment;
import com.ourwayoflife.owl.models.User;
import com.ourwayoflife.owl.views.ProfilePictureView;

import java.util.ArrayList;

/**
 * Created by Zach on 5/25/17.
 */

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.ViewHolder> {

    private static final String TAG = FriendsRecyclerAdapter.class.getName();

    private ArrayList<User> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private FriendsRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public FriendsRecyclerAdapter(Context context, ArrayList<User> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public FriendsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_friends, parent, false);
        FriendsRecyclerAdapter.ViewHolder viewHolder = new FriendsRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(FriendsRecyclerAdapter.ViewHolder holder, int position) {
        User user = mData.get(position);

        holder.mTextName.setText(user.getName()); // Set the name text

        // Get the bitmap of the user's profile picture
        Bitmap userBitmap;
        BitmapFactory.Options optionsUser = new BitmapFactory.Options();
        //options.inSampleSize = 4;
        userBitmap = FeedFragment.getBitmapFromMemCache("u" + user.getUserId()); // Added a 'u' in front in case there is an overlap between a userId and photoId
        userPhoto:
        if (userBitmap == null) {
            //Bitmap is not cached.  Have to download

            // Convert the photo string to a bitmap
            String photoString = user.getPhoto();
            if (photoString == null || photoString.length() <= 0) {
                break userPhoto;
            }
            try {
                byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                userBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, optionsUser);

                //Add bitmap to the cache
                FeedFragment.addBitmapToMemoryCache(String.valueOf("u" + user.getUserId()), userBitmap);
            } catch (Exception e) {
                Log.e(TAG, "Conversion from String to Bitmap: " + e.getMessage());
            }
        }

        if(userBitmap != null) {
            holder.mProfilePictureView.setBitmap(userBitmap); // Set user's profile picture
        } else {
            // TODO set to default photo or something
        }


    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ProfilePictureView mProfilePictureView;
        public TextView mTextName;

        public ViewHolder(View itemView) {
            super(itemView);
            mProfilePictureView = (ProfilePictureView) itemView.findViewById(R.id.profile_picture);
            mTextName = itemView.findViewById(R.id.text_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public User getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(FriendsRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
