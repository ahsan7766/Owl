package com.example.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.owl.R;
import com.example.owl.models.FeedItem;
import com.example.owl.views.FeedItemView;

import java.util.ArrayList;

/**
 * Created by Zach on 5/22/17.
 */

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private ArrayList<FeedItem> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;


    // data is passed into the constructor
    public FeedRecyclerAdapter(Context context, ArrayList<FeedItem> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // Clean all elements of the recycler
    public void clear() {
        mData = new ArrayList<>();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(ArrayList<FeedItem> data) {
        mData = data;
        notifyDataSetChanged();
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_feed, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FeedItem feedItem = mData.get(position);
        holder.mFeedItemView.setHeader(feedItem.getHeader());
        holder.mFeedItemView.setPhotoCount(feedItem.getPhotoCount());

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public FeedItemView mFeedItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            mFeedItemView = (FeedItemView) itemView.findViewById(R.id.feed_category);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public FeedItem getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
