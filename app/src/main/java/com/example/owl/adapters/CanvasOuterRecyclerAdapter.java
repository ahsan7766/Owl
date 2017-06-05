package com.example.owl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.owl.R;
import com.example.owl.activities.StackActivity;
import com.example.owl.models.CanvasTile;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasOuterRecyclerAdapter extends RecyclerView.Adapter<CanvasOuterRecyclerAdapter.ViewHolder>
        implements CanvasInnerRecyclerAdapter.ItemClickListener{


    //private static final int ROW_COUNT = 7;
    private int rowCount = 0; //iterator

    private RecyclerView mInnerRecyclerView;
    protected CanvasInnerRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private CanvasTile[][] mDataset = new CanvasTile[0][0];
    private LayoutInflater mInflater;
    private CanvasOuterRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public CanvasOuterRecyclerAdapter(Context context, CanvasTile[][] data) {
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
        mAdapter = new CanvasInnerRecyclerAdapter(parent.getContext(), mDataset[rowCount]);

        rowCount++; // iterate the row count so when (if) the next ViewHolder row is created it takes the next data row

        mAdapter.setClickListener(this);

        // Set CustomAdapter as the adapter for RecyclerView.
        mInnerRecyclerView.setAdapter(mAdapter);



        //initDataset();

        return viewHolder;
    }



    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(CanvasOuterRecyclerAdapter.ViewHolder holder, int position) {
        CanvasTile[] canvasTile = mDataset[position];
        //holder.mFeedCategoryView.setHeader(feedCategory.getHeader());
        //holder.mFeedCategoryView.setPhotoCount(feedCategory.getPhotoCount());

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
    public CanvasTile[] getItem(int id) {
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


    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(view.getContext(), StackActivity.class);
        view.getContext().startActivity(intent);
    }

    /*
    private void initDataset() {
        mDataset = new CanvasTile[CanvasFragment.COLUMN_COUNT];
        for (int i = 0; i < CanvasFragment.COLUMN_COUNT; i++) {
            mDataset[i] = new CanvasTile("#" + i);
        }
    }
    */

}
