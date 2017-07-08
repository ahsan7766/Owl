package com.ourwayoflife.owl.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.models.PhotoComment;
import com.ourwayoflife.owl.models.User;
import com.ourwayoflife.owl.views.ProfilePictureView;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Zach on 5/25/17.
 */

public class PhotoCommentsRecyclerAdapter extends RecyclerView.Adapter<PhotoCommentsRecyclerAdapter.ViewHolder> {

    private static final String TAG = PhotoCommentsRecyclerAdapter.class.getName();

    private ArrayList<PhotoComment> mData = new ArrayList<>();
    private HashMap<String, User> mUserHashMap = new HashMap<>();
    private LayoutInflater mInflater;
    private PhotoCommentsRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public PhotoCommentsRecyclerAdapter(Context context, ArrayList<PhotoComment> data, HashMap<String, User> userHashMap) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mUserHashMap = userHashMap;
    }

    // inflates the cell layout from xml when needed
    @Override
    public PhotoCommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_comment, parent, false);
        PhotoCommentsRecyclerAdapter.ViewHolder viewHolder = new PhotoCommentsRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(PhotoCommentsRecyclerAdapter.ViewHolder holder, int position) {
        PhotoComment photoComment = mData.get(position);

        User user =  mUserHashMap.get(photoComment.getUserId());
        if(user != null) {
            String photoString = user.getPhoto();
            try {
                byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                holder.mProfilePictureView.setBitmap(bitmap);
            } catch (Exception e) {
                Log.e(TAG, "Conversion from String to Bitmap: " + e);
                // TODO set profile picture to "not found" or something
            }

            holder.mTextName.setText(user.getName());
            holder.mTextDate.setText(photoComment.getCommentDate());
            holder.mTextComment.setText(photoComment.getComment());
        } else {
            holder.mTextName.setText("User Not Found");
        }

    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ProfilePictureView mProfilePictureView;
        public TextView mTextName;
        public TextView mTextDate;
        public TextView mTextComment;

        public ViewHolder(View itemView) {
            super(itemView);
            mProfilePictureView = itemView.findViewById(R.id.profile_picture);
            mTextName = itemView.findViewById(R.id.text_name);
            mTextDate = itemView.findViewById(R.id.text_date);
            mTextComment = itemView.findViewById(R.id.text_comment);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public PhotoComment getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(PhotoCommentsRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
