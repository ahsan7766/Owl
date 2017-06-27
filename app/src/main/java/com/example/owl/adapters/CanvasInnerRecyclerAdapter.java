package com.example.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.owl.R;
import com.example.owl.models.CanvasTile;

/**
 * Created by Zach on 5/23/17.
 */

public class CanvasInnerRecyclerAdapter extends RecyclerView.Adapter<CanvasInnerRecyclerAdapter.ViewHolder> {

    private CanvasTile[] mData = new CanvasTile[0];
    private LayoutInflater mInflater;
    private CanvasInnerRecyclerAdapter.ItemClickListener mClickListener;
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
        CanvasTile canvasTile = mData[position];
        //holder.mTextView.setText(canvasTile.getText());


        //holder.mImageView.setImageResource(R.drawable.trees);

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

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnDragListener {
        //public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            //mTextView = (TextView) itemView.findViewById(R.id.text);
            mImageView = (ImageView) itemView.findViewById(R.id.image);

            itemView.setOnClickListener(this);
            itemView.setOnDragListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
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
    public void setClickListener(CanvasInnerRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // allows drag events to be caught
    public void setDragListener(CanvasInnerRecyclerAdapter.ItemDragListener itemDragListener) {
        this.mDragListener = itemDragListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    // parent activity will implement this method to respond to drags
    public interface ItemDragListener {
        boolean onItemDrag(View view, DragEvent dragEvent, int row, int column);
    }

}
