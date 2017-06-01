package com.example.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.owl.R;

import org.w3c.dom.Text;

/**
 * Created by Zach on 5/26/17.
 */

public class UploadStackRecyclerAdapter extends RecyclerView.Adapter<UploadStackRecyclerAdapter.ViewHolder> {

    private String[] mData = new String[0];
    private LayoutInflater mInflater;
    private UploadStackRecyclerAdapter.ItemClickListener mClickListener;

    private int mSelectedPos = -1;

    // data is passed into the constructor
    public UploadStackRecyclerAdapter(Context context, String[] data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public UploadStackRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_upload_stack, parent, false);
        UploadStackRecyclerAdapter.ViewHolder viewHolder = new UploadStackRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(final UploadStackRecyclerAdapter.ViewHolder holder, final int position) {
        String string = mData[position];

        holder.mTextStackName.setText(string);
        holder.mRadioButton.setChecked(position == mSelectedPos);
        holder.mRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPos = position;
                notifyDataSetChanged();
            }
        });

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTextStackName;
        public RadioButton mRadioButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextStackName = (TextView) itemView.findViewById(R.id.text_stack_name);
            mRadioButton = (RadioButton) itemView.findViewById(R.id.radio_button);
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
    public void setClickListener(UploadStackRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
