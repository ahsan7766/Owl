package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.models.FeedItem;
import com.ourwayoflife.owl.views.ProfilePictureView;

import java.util.ArrayList;

/**
 * Created by Zach on 5/22/17.
 */

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder> {

    private ArrayList<FeedItem> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ImageClickListener mImageClickListener;
    private ProfileClickListener mProfileClickListener;
    private ItemLongClickListener mLongClickListener;
    private ItemDragListener mDragListener;
    private ItemCheckedChangeListener mOnCheckedChangeListener;

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
        holder.mImage.setImageBitmap(feedItem.getPhoto());
        holder.mProfilePictureView.setBitmap(feedItem.getUserPicture());
        holder.mTextName.setText(feedItem.getUserName());
        holder.mToggleButtonLike.setChecked(feedItem.getLiked());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder
            implements //View.OnClickListener,
             View.OnLongClickListener, View.OnDragListener {


        public ImageView mImage;
        public ProfilePictureView mProfilePictureView;
        public TextView mTextName;
        public ToggleButton mToggleButtonLike;

        public ViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.image);
            mProfilePictureView = itemView.findViewById(R.id.profile_picture);
            mTextName = itemView.findViewById(R.id.text_name);
            mToggleButtonLike = itemView.findViewById(R.id.toggle_button_like);


            mToggleButtonLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    mOnCheckedChangeListener.onItemCheckedChange(compoundButton, isChecked, getAdapterPosition());
                }
            });


            //itemView.setOnClickListener(this);

            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mImageClickListener != null) mImageClickListener.onImageClick(view, getAdapterPosition());
                }
            });

            mProfilePictureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mProfileClickListener != null) mProfileClickListener.onProfileClick(view, getAdapterPosition());
                }
            });

            mTextName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mProfileClickListener != null) mProfileClickListener.onProfileClick(view, getAdapterPosition());
                }
            });


            //itemView.setOnLongClickListener(this);
            //itemView.setOnDragListener(this);

            mImage.setOnLongClickListener(this);
            mImage.setOnDragListener(this);


        }


        /*
        @Override
        public void onClick(View view) {
            if (mImageClickListener != null) mImageClickListener.onImageClick(view, getAdapterPosition());
        }
        */


        @Override
        public boolean onLongClick(View view) {
            return mLongClickListener != null && mLongClickListener.onItemLongClick(view, getAdapterPosition());
        }

        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            return mDragListener != null && mDragListener.onItemDrag(view, dragEvent, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public FeedItem getItem(int id) {
        return mData.get(id);
    }


    // allows click events to be caught
    public void setImageClickListener(ImageClickListener imageClickListener) {
        this.mImageClickListener = imageClickListener;
    }

    // allows click events to be caught
    public void setProfileClickListener(ProfileClickListener profileClickListener) {
        this.mProfileClickListener = profileClickListener;
    }

    // allows long click events to be caught
    public void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.mLongClickListener = itemLongClickListener;
    }

    // allows long click events to be caught
    public void setDragListener(ItemDragListener itemDragListener) {
        this.mDragListener = itemDragListener;
    }

    // allows catching of checking/unchecking the toggle button
    public void setOnCheckedChangeListener(ItemCheckedChangeListener itemCheckedChangeListener) {
        this.mOnCheckedChangeListener = itemCheckedChangeListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ImageClickListener {
        void onImageClick(View view, int position);
    }

    // parent activity will implement this method to respond to click events
    public interface ProfileClickListener {
        void onProfileClick(View view, int position);
    }

    // parent activity will implement this method to respond to click events
    public interface ItemLongClickListener {
        boolean onItemLongClick(View view, int position);
    }

    // parent activity will implement this method to respond to drags
    public interface ItemDragListener {
        boolean onItemDrag(View view, DragEvent dragEvent, int position);
    }

    // parent activity will implement this method to respond to drags
    public interface ItemCheckedChangeListener {
        void onItemCheckedChange(CompoundButton compoundButton, boolean isChecked, int position);
    }
}
