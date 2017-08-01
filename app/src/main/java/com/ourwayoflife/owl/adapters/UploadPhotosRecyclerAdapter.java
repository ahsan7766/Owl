package com.ourwayoflife.owl.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.UploadActivity;
import com.ourwayoflife.owl.models.Photo;
import com.ourwayoflife.owl.models.PhotoVideoHolder;

import java.util.ArrayList;

/**
 * Created by Zach on 5/26/17.
 */

public class UploadPhotosRecyclerAdapter extends RecyclerView.Adapter<UploadPhotosRecyclerAdapter.ViewHolder> {

    private ArrayList<PhotoVideoHolder> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private UploadPhotosRecyclerAdapter.ItemClickListener mClickListener;

    private Context mContext;

    // data is passed into the constructor
    public UploadPhotosRecyclerAdapter(Context context, ArrayList<PhotoVideoHolder> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public UploadPhotosRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_upload_photos, parent, false);
        UploadPhotosRecyclerAdapter.ViewHolder viewHolder = new UploadPhotosRecyclerAdapter.ViewHolder(view);

        mContext = view.getContext();
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(UploadPhotosRecyclerAdapter.ViewHolder holder, int position) {

        // If it is the last item in the list, then it is the "Add Photo" item
        if (position == mData.size()) {
            holder.mImageView.setImageResource(R.drawable.ic_add);
            final int padding = 25;
            holder.mImageView.setPadding(padding, padding, padding, padding);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Allow user to select photo(s)
                    Intent intent = new Intent();
                    intent.setType("image/* video/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "video/*"});
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    ((Activity) mContext).startActivityForResult(
                            Intent.createChooser(intent, "Select Photos/Videos"),
                            UploadActivity.REQUEST_SELECT_PHOTOS);
                }
            });
        } else {
            // Otherwise, set the ImageView to the selected photo
            Bitmap bitmap = mData.get(position).getPhoto();

            holder.mImageView.setImageBitmap(bitmap);
        }

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public PhotoVideoHolder getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(UploadPhotosRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
