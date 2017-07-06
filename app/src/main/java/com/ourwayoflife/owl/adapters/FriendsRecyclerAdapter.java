package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.views.ProfilePictureView;

/**
 * Created by Zach on 5/25/17.
 */

public class FriendsRecyclerAdapter extends RecyclerView.Adapter<FriendsRecyclerAdapter.ViewHolder> {

    private String[] mData = new String[0];
    private LayoutInflater mInflater;
    private FriendsRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public FriendsRecyclerAdapter(Context context, String[] data) {
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
        String string = mData[position];
        //holder.mFeedCategoryView.setHeader(feedCategory.getHeader());
        //holder.mFeedCategoryView.setPhotoCount(feedCategory.getPhotoCount());
        holder.mProfilePictureView.setBackgroundPicture(R.drawable.trees);

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ProfilePictureView mProfilePictureView;

        public ViewHolder(View itemView) {
            super(itemView);
            //mFeedCategoryView = (FeedCategoryView) itemView.findViewById(R.id.feed_category);
            mProfilePictureView = (ProfilePictureView) itemView.findViewById(R.id.profile_picture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mData[id];
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
