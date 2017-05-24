package com.example.owl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.owl.R;
import com.example.owl.activities.StackActivity;
import com.example.owl.models.CanvasTile;
import com.example.owl.views.FeedCategoryView;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasInnerRecyclerAdapter extends RecyclerView.Adapter<CanvasInnerRecyclerAdapter.ViewHolder> {

    private CanvasTile[] mData = new CanvasTile[0];
    private LayoutInflater mInflater;
    private CanvasInnerRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public CanvasInnerRecyclerAdapter(Context context, CanvasTile[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public CanvasInnerRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_canvas_inner, parent, false);
        CanvasInnerRecyclerAdapter.ViewHolder viewHolder = new CanvasInnerRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(CanvasInnerRecyclerAdapter.ViewHolder holder, int position) {
        CanvasTile canvasTile = mData[position];
        //holder.mFeedCategoryView.setHeader(feedCategory.getHeader());
        //holder.mFeedCategoryView.setPostCount(feedCategory.getPostCount());

        holder.mTextView.setText(canvasTile.getText());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            //mFeedCategoryView = (FeedCategoryView) itemView.findViewById(R.id.feed_category);
            mTextView = (TextView) itemView.findViewById(R.id.text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public CanvasTile getItem(int id) {
        return mData[id];
    }

    // allows clicks events to be caught
    public void setClickListener(CanvasInnerRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
