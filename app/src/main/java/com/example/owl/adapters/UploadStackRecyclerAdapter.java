package com.example.owl.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.owl.R;
import com.example.owl.models.Stack;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Zach on 5/26/17.
 */

public class UploadStackRecyclerAdapter extends RecyclerView.Adapter<UploadStackRecyclerAdapter.ViewHolder> {

    private ArrayList<Stack> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private UploadStackRecyclerAdapter.ItemClickListener mClickListener;

    private int mSelectedPos = -1;

    // data is passed into the constructor
    public UploadStackRecyclerAdapter(Context context, ArrayList<Stack> data) {
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
        Stack stack = mData.get(position);

        holder.mTextStackName.setText(stack.getName());

        // If the StackId is -1, then it is the "Create New Stack Option"
        // In this case, set the text to bold style, otherwise set to default
        if(stack.getStackId() == "-1") {
            holder.mTextStackName.setTypeface(Typeface.DEFAULT_BOLD);
        }else{
            holder.mTextStackName.setTypeface(Typeface.DEFAULT);
        }

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
        return mData.size();
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
    public Stack getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(UploadStackRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public int getSelectedPos() {
        return mSelectedPos;
    }
}
