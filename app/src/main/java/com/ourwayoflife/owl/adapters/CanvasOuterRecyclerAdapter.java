package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.StackActivity;
import com.ourwayoflife.owl.fragments.CanvasFragment;
import com.ourwayoflife.owl.managers.CanvasInnerLinearLayoutManager;
import com.ourwayoflife.owl.models.CanvasTile;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasOuterRecyclerAdapter extends RecyclerView.Adapter<CanvasOuterRecyclerAdapter.ViewHolder>
        implements CanvasInnerRecyclerAdapter.InnerItemClickListener,
        CanvasInnerRecyclerAdapter.ItemDragListener {


    private static final String TAG = CanvasOuterRecyclerAdapter.class.getName();

    //private static final int ROW_COUNT = 7;
    private int rowCount = 0; //iterator

    private Context mContext;

    private RecyclerView mInnerRecyclerView;
    protected CanvasInnerRecyclerAdapter mAdapterInner;
    protected CanvasTile[] mDatasetInner;
    private LinearLayoutManager mLayoutManager;

    private CanvasTile[][] mDataset = new CanvasTile[0][0];
    private LayoutInflater mInflater;
    private CanvasOuterRecyclerAdapter.OuterItemClickListener mOuterItemClickListener;
    private CanvasOuterRecyclerAdapter.ItemInnerDragListener mInnerDragListener;


    // data is passed into the constructor
    public CanvasOuterRecyclerAdapter(Context context, CanvasTile[][] data) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mDataset = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public CanvasOuterRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_canvas_outer, parent, false);
        CanvasOuterRecyclerAdapter.ViewHolder viewHolder = new CanvasOuterRecyclerAdapter.ViewHolder(view);


        mInnerRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_canvas_inner);


        //mInnerRecyclerView.setHasFixedSize(true);  //TODO see if this works
        mInnerRecyclerView.setItemViewCacheSize(20);
        mInnerRecyclerView.setDrawingCacheEnabled(true);
        mInnerRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);



        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false);
        //mLayoutManager = new CanvasInnerLinearLayoutManager(parent.getContext(), LinearLayoutManager.HORIZONTAL, false);

        // set up the RecyclerView
        mInnerRecyclerView.setLayoutManager(mLayoutManager);

        mDatasetInner = new CanvasTile[CanvasFragment.COLUMN_COUNT];
        mAdapterInner = new CanvasInnerRecyclerAdapter(parent.getContext(), mDatasetInner, rowCount);


        rowCount++; // iterate the row count so when (if) the next ViewHolder row is created it takes the next data row

        mAdapterInner.setInnerClickListener(this);
        mAdapterInner.setDragListener(this);


        // Set CustomAdapter as the adapter for RecyclerView.
        mInnerRecyclerView.setAdapter(mAdapterInner);

        //initDataset();

        return viewHolder;
    }



    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(CanvasOuterRecyclerAdapter.ViewHolder holder, int position) {
        mDatasetInner = mDataset[position];
        mAdapterInner.notifyDataSetChanged();

        //holder.mFeedCategoryView.setHeader(feedCategory.getHeader());
        //holder.mFeedCategoryView.setPhotoCount(feedCategory.getPhotoCount());

        //mAdapterInner = new CanvasInnerRecyclerAdapter(mContext, mDatasetInner, position);
        //holder.mRecycler.setAdapter(mAdapterInner);

        //initDataset();
    }

    // total number of cells
    @Override
    public int getItemCount() {
        // If the dataset is null, return 0 for item count to avoid NullPointerException
        return mDataset == null ? 0 : mDataset.length ;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder
        implements  CanvasInnerRecyclerAdapter.InnerItemClickListener{
        public RecyclerView mRecycler;

        public ViewHolder(View itemView) {
            super(itemView);
            mRecycler = itemView.findViewById(R.id.recycler_canvas_inner);

        }

        @Override
        public void onInnerItemClick(View view, int column) {
            if (mOuterItemClickListener != null) mOuterItemClickListener.onOuterItemClick(view, getAdapterPosition(), column);
        }
    }

    // convenience method for getting data at click position
    public CanvasTile[] getItem(int id) {
        return mDataset[id];
    }

    /*
    // allows clicks events to be caught
    public void setClickListener(CanvasOuterRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    */


    // parent activity will implement this method to respond to drag events
    public void setInnerClickListener (CanvasOuterRecyclerAdapter.OuterItemClickListener outerItemClickListener) {
        this.mOuterItemClickListener = outerItemClickListener;
    }

    // parent activity will implement this method to respond to drag events
    public void setInnerDragListener (CanvasOuterRecyclerAdapter.ItemInnerDragListener itemInnerDragListener) {
        this.mInnerDragListener = itemInnerDragListener;
    }

    // parent activity will implement this method to respond to click events
    public interface OuterItemClickListener {
        void onOuterItemClick(View view, int row, int column);
    }


    // parent activity will implement this method to respond to drag events
    public interface ItemInnerDragListener {
        boolean onItemDrag(View view, DragEvent dragEvent, int row, int column);
    }







    @Override
    public void onInnerItemClick(View view, int column) {

        /*
        final String STACK_ID = mDatasetInner[position].getStackId();
        if(STACK_ID == null || STACK_ID.isEmpty()) {
            // Don't start stack activity if we don't have a stackId to pass it
            return;
        }

        Intent intent = new Intent(view.getContext(), StackActivity.class);
        intent.putExtra("STACK_ID", STACK_ID);
        view.getContext().startActivity(intent);
        */
        if (mOuterItemClickListener != null) mOuterItemClickListener.onOuterItemClick(view, 0, column);

    }





    @Override
    public boolean onItemDrag(View view, DragEvent dragEvent, int row, int column) {
        if(dragEvent.getAction() == DragEvent.ACTION_DRAG_LOCATION ) {

            int offset = mInnerRecyclerView.computeHorizontalScrollOffset();
            /*
            int extent = mInnerRecyclerView.computeHorizontalScrollExtent();
            int range = mInnerRecyclerView.computeHorizontalScrollRange();

            int percentage = (int)(100.0 * offset / (float)(range - extent));
            Log.d(TAG, "scroll percentage: " + percentage + "%");
            */

            int x = Math.round(dragEvent.getX());

            int translatedX = x - offset; // mAdapter.getScrollDistance();

            //Log.d(TAG, "x: " + x + ", HorizontalScrollOffset: " + offset);
            Log.d(TAG, "x: " + x + ", TranslatedX: " + translatedX);

            int threshold = 50;
            // make a scrolling up due the x has passed the threshold
            if (translatedX < threshold) {
                // make a scroll left
                mInnerRecyclerView.smoothScrollBy(-50, 0);

            } else {
                // make a autoscrolling down due y has passed the 500 px border
                if (translatedX + threshold > 100) {
                    // make a scroll right
                    mInnerRecyclerView.smoothScrollBy(50, 0);
                }
            }

        }

        return mInnerDragListener.onItemDrag(view, dragEvent, row, column);
    }





    public void notifyInnerDatasetRowsChanged() {

        for(int i = 0; i < CanvasFragment.ROW_COUNT; i++) {
            if(mDataset[i] != null) {
                //mAdapterInner = new CanvasInnerRecyclerAdapter(mContext, mDataset[i], i);
                //mAdapterInner.notifyDataSetChanged();

            }
        }
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
