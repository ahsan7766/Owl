package com.ourwayoflife.owl.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.ourwayoflife.owl.R;
import com.ourwayoflife.owl.activities.UploadActivity;
import com.ourwayoflife.owl.models.PhotoVideoHolder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Zach on 5/26/17.
 */

public class UploadPhotosRecyclerAdapter extends RecyclerView.Adapter<UploadPhotosRecyclerAdapter.ViewHolder> {

    private static final String TAG = UploadPhotosRecyclerAdapter.class.getName();

    private ArrayList<PhotoVideoHolder> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private UploadPhotosRecyclerAdapter.ItemClickListener mClickListener;

    private String mCapturedPhotoPath;

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

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(UploadPhotosRecyclerAdapter.ViewHolder holder, int position) {

        if (position == getItemCount() - 3) {
            // If it is the next to next to last item in the list, then it is the "Add" item
            holder.mImageView.setImageResource(R.drawable.ic_add);
            final int padding = 25;
            holder.mImageView.setPadding(padding, padding, padding, padding);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Allow user to select photos/videos
                    Intent intent = new Intent();
                    intent.setType("image/* video/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    ((Activity) mContext).startActivityForResult(
                            Intent.createChooser(intent, "Select Photos/Videos"),
                            UploadActivity.REQUEST_SELECT_PHOTOS);
                }
            });

            holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // On long click, make a toast explaining the action that a click does
                    Toast.makeText(mContext, "Add a Photo or Video from your Gallery", Toast.LENGTH_SHORT).show();
                    return true; // return true so the event receiver and the event is not propagated to the other views in the tree
                }
            });

        } else if(position == getItemCount() - 2) {
            // If it is the next to last item in the list, then it is the "Take Photo" item
            holder.mImageView.setImageResource(R.drawable.ic_camera);
            final int padding = 40;
            holder.mImageView.setPadding(padding, padding, padding, padding);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Allow user to take a new photo
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            // Create an image file name
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "OWL_" + timeStamp;
                            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            //File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                            photoFile = File.createTempFile(
                                    imageFileName,  /* prefix */
                                    ".jpg",         /* suffix */
                                    storageDir      /* directory */
                            );

                            // Save a file: path for use with ACTION_VIEW intents
                            setCapturedPhotoPath(photoFile.getAbsolutePath());

                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Log.e(TAG, "Failed to create temp file for photo capture");
                        }

                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(mContext,
                                    "com.ourwayoflife.owl.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            ((Activity) mContext).startActivityForResult(takePictureIntent, UploadActivity.REQUEST_IMAGE_CAPTURE);
                        }
                    }
                }
            });

            holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // On long click, make a toast explaining the action that a click does
                    Toast.makeText(mContext, "Take a new Photo", Toast.LENGTH_SHORT).show();
                    return true; // return true so the event receiver and the event is not propagated to the other views in the tree
                }
            });

        } else if (position == getItemCount() - 1) {
            // If it is the last item in the list, then it is the "Take Video" item
            holder.mImageView.setImageResource(R.drawable.ic_videocam);
            final int padding = 35;
            holder.mImageView.setPadding(padding, padding, padding, padding);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Allow user to take a new video
                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    // Make sure we have an app installed to handle this intent.
                    if (takeVideoIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        ((Activity) mContext).startActivityForResult(takeVideoIntent, UploadActivity.REQUEST_VIDEO_CAPTURE);
                    } else {
                        Toast.makeText(mContext, "Unable to capture video.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // On long click, make a toast explaining the action that a click does
                    Toast.makeText(mContext, "Take a new Video", Toast.LENGTH_SHORT).show();
                    return true; // return true so the event receiver and the event is not propagated to the other views in the tree
                }
            });

        } else {
            // Otherwise, it is a picture that they added.  Set the ImageView to the selected photo
            // TODO maybe scale it down before displaying it?
            Bitmap bitmap = mData.get(position).getPhoto();
            holder.mImageView.setImageBitmap(bitmap);

        }

    }

    public String getCapturedPhotoPath() {
        return mCapturedPhotoPath;
    }

    public void setCapturedPhotoPath(String capturedPhotoPath) {
        mCapturedPhotoPath = capturedPhotoPath;
    }

    // total number of cells
    @Override
    public int getItemCount() {
        // Add 2 for the "Add" and "Take Picture" options
        return mData.size() + 3;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView mImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image);
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
