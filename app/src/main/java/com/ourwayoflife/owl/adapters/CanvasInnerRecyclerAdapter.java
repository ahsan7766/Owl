package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.models.CanvasTile;
import com.ourwayoflife.owl.views.CanvasTileView;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasInnerRecyclerAdapter extends RecyclerView.Adapter<CanvasInnerRecyclerAdapter.ViewHolder> {

    private CanvasTile[] mData = new CanvasTile[0];
    private LayoutInflater mInflater;
    private CanvasInnerRecyclerAdapter.InnerItemClickListener mInnerItemClickListener;
    private CanvasInnerRecyclerAdapter.ItemDragListener mDragListener;
    private int mRow;

    // data is passed into the constructor
    public CanvasInnerRecyclerAdapter(Context context, CanvasTile[] data, int row) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mRow = row;
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
        CanvasTile canvasTile =  mData[position];

        if(canvasTile == null ) {
            // Set it to whatever we want an empty tile to look like

            holder.mCanvasTile.setName("TEST");

        } else {
            holder.mCanvasTile.setName(canvasTile.getName());
            holder.mCanvasTile.setPhoto(canvasTile.getPhoto());
        }



        // Set sample images
        /*
        outer:
        switch (mRow) {

            case 0:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_0_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_0_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_0_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_0_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_0_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_0_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_0_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_0_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }

            case 1:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_1_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_1_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_1_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_1_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_1_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_1_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_1_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_1_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }


            case 2:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_2_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_2_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_2_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_2_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_2_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_2_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_2_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_2_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }

            case 3:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_3_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_3_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_3_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_3_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_3_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_3_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_3_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_3_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }

            case 4:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_4_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_4_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_4_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_4_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_4_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_4_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_4_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_4_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }

            case 5:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_5_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_5_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_5_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_5_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_5_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_5_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_5_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_5_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }

            case 6:
                switch (position) {
                    case 0:
                        holder.mImageView.setImageResource(R.drawable.sample_6_0);
                        break outer;
                    case 1:
                        holder.mImageView.setImageResource(R.drawable.sample_6_1);
                        break outer;
                    case 2:
                        holder.mImageView.setImageResource(R.drawable.sample_6_2);
                        break outer;
                    case 3:
                        holder.mImageView.setImageResource(R.drawable.sample_6_3);
                        break outer;
                    case 4:
                        holder.mImageView.setImageResource(R.drawable.sample_6_4);
                        break outer;
                    case 5:
                        holder.mImageView.setImageResource(R.drawable.sample_6_5);
                        break outer;
                    case 6:
                        holder.mImageView.setImageResource(R.drawable.sample_6_6);
                        break outer;
                    case 7:
                        holder.mImageView.setImageResource(R.drawable.sample_6_7);
                        break outer;
                    default:
                        holder.mImageView.setImageResource(R.drawable.trees);
                        break outer;
                }


            default:
                holder.mImageView.setImageResource(R.drawable.trees);
        }
        */

    }

    // total number of cells
    @Override
    public int getItemCount() {
        // If the dataset is null, return 0 for item count to avoid NullPointerException
        return mData == null ? 0 : mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnDragListener {
        public CanvasTileView mCanvasTile;

        public ViewHolder(View itemView) {
            super(itemView);
            mCanvasTile = itemView.findViewById(R.id.canvas_tile);

            itemView.setOnClickListener(this);
            itemView.setOnDragListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mInnerItemClickListener != null) mInnerItemClickListener.onInnerItemClick(view, mRow, getAdapterPosition());
        }

        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {

            return mDragListener != null && mDragListener.onItemDrag(view, dragEvent, mRow, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public CanvasTile getItem(int id) {
        return mData[id];
    }


    // allows click events to be caught
    public void setInnerClickListener(CanvasInnerRecyclerAdapter.InnerItemClickListener innerItemClickListener) {
        this.mInnerItemClickListener = innerItemClickListener;
    }

    // allows drag events to be caught
    public void setDragListener(CanvasInnerRecyclerAdapter.ItemDragListener itemDragListener) {
        this.mDragListener = itemDragListener;
    }

    // parent activity will implement this method to respond to click events
    public interface InnerItemClickListener {
        void onInnerItemClick(View view, int row, int column);
    }

    // parent activity will implement this method to respond to drags
    public interface ItemDragListener {
        boolean onItemDrag(View view, DragEvent dragEvent, int row, int column);
    }

}
