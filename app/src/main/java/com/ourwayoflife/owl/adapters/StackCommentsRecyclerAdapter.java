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
import com.ourwayoflife.owl.models.StackComment;
import com.ourwayoflife.owl.models.User;
import com.ourwayoflife.owl.views.ProfilePictureView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 7/25/17.
 */

public class StackCommentsRecyclerAdapter extends RecyclerView.Adapter<StackCommentsRecyclerAdapter.ViewHolder> {

    private static final String TAG = StackCommentsRecyclerAdapter.class.getName();

    private ArrayList<StackComment> mData = new ArrayList<>();
    private HashMap<String, User> mUserHashMap = new HashMap<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private StackCommentsRecyclerAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public StackCommentsRecyclerAdapter(Context context, ArrayList<StackComment> data, HashMap<String, User> userHashMap) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mUserHashMap = userHashMap;
    }

    // inflates the cell layout from xml when needed
    @Override
    public StackCommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_item_comment, parent, false);
        StackCommentsRecyclerAdapter.ViewHolder viewHolder = new StackCommentsRecyclerAdapter.ViewHolder(view);
        return viewHolder;
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(StackCommentsRecyclerAdapter.ViewHolder holder, int position) {
        StackComment stackComment = mData.get(position);

        User user = mUserHashMap.get(stackComment.getUserId());
        if (user != null) {
            String photoString = user.getPhoto();
            // Only try getting the bitmap is we have a string for the profile picture
            if (photoString != null && !photoString.isEmpty()) {
                try {
                    byte[] encodeByte = Base64.decode(photoString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    holder.mProfilePictureView.setBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Conversion from String to Bitmap: " + e);
                    // TODO set profile picture to "not found" or something
                }
            }

            holder.mTextName.setText(user.getName());

            // Need to convert date string to a more readable format
            DateTime dateTime = ISODateTimeFormat.basicDateTime().parseDateTime(stackComment.getCommentDate());
            DateTimeFormatter fmt = DateTimeFormat.forPattern("h:mm aa");
            holder.mTextDate.setText(fmt.print(dateTime));

            holder.mTextComment.setText(stackComment.getComment());
        } else {
            holder.mTextName.setText(mContext.getString(R.string.user_not_found));
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
    public StackComment getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(StackCommentsRecyclerAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
