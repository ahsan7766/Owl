package com.example.owl.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.owl.R;
import com.example.owl.views.FeedCategoryView;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasOuterRecyclerAdapter extends RecyclerView.Adapter<CanvasOuterRecyclerAdapter.ViewHolder> {


    private static final int DATASET_COUNT = 7;

    private RecyclerView mInnerRecyclerView;
    protected CanvasInnerRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String[] mDataset = new String[0];
    private LayoutInflater mInflater;
    private CanvasOuterRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public CanvasOuterRecyclerAdapter(Context context, String[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mDataset = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public CanvasOuterRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_canvas_outer, parent, false);
        CanvasOuterRecyclerAdapter.ViewHolder viewHolder = new CanvasOuterRecyclerAdapter.ViewHolder(view);


        mInnerRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_canvas_inner);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false);

        // set up the RecyclerView
        mInnerRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CanvasInnerRecyclerAdapter(parent.getContext(), mDataset);
        //mAdapter.setClickListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mInnerRecyclerView.setAdapter(mAdapter);


        //initDataset();

        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(CanvasOuterRecyclerAdapter.ViewHolder holder, int position) {
        String string = mDataset[position];
        //holder.mFeedCategoryView.setHeader(feedCategory.getHeader());
        //holder.mFeedCategoryView.setPostCount(feedCategory.getPostCount());



        //initDataset();
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mDataset.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public RecyclerView mRecycler;

        public ViewHolder(View itemView) {
            super(itemView);
            mRecycler = (RecyclerView) itemView.findViewById(R.id.recycler_canvas_inner);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mDataset[id];
    }

    // allows clicks events to be caught
    public void setClickListener(CanvasOuterRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private void initDataset() {
        mDataset = new String[DATASET_COUNT];
        for (int i = 0; i < DATASET_COUNT; i++) {
            mDataset[i] = "Category #" + i;
        }
    }
}
