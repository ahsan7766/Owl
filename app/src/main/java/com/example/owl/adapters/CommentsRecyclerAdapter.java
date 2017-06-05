package com.example.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.owl.R;
import com.example.owl.models.Comment;
import com.example.owl.views.ProfilePictureView;

/**
 * Created by Zach on 5/25/17.
 */

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    private Comment[] mData = new Comment[0];
    private LayoutInflater mInflater;
    private CommentsRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public CommentsRecyclerAdapter(Context context, Comment[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_comment, parent, false);
        CommentsRecyclerAdapter.ViewHolder viewHolder = new CommentsRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(CommentsRecyclerAdapter.ViewHolder holder, int position) {
        Comment comment = mData[position];
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
            mProfilePictureView = (ProfilePictureView) itemView.findViewById(R.id.profile_picture);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Comment getItem(int id) {
        return mData[id];
    }

    // allows clicks events to be caught
    public void setClickListener(CommentsRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
