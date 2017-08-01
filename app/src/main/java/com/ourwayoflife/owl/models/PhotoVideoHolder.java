package com.ourwayoflife.owl.models;

import android.graphics.Bitmap;

/**
 * Created by Zach on 8/1/17.
 */

public class PhotoVideoHolder {

    private boolean isPhotoType;
    private Bitmap mPhoto;
    private String mVideoPath;


    public PhotoVideoHolder() {
    }

    public PhotoVideoHolder(boolean isPhotoType, Bitmap photo, String videoPath) {
        this.isPhotoType = isPhotoType;
        mPhoto = photo;
        mVideoPath = videoPath;
    }


    public boolean isPhotoType() {
        return isPhotoType;
    }

    public void setPhotoType(boolean photoType) {
        isPhotoType = photoType;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        mPhoto = photo;
    }

    public String getVideoPath() {
        return mVideoPath;
    }

    public void setVideoPath(String videoPath) {
        mVideoPath = videoPath;
    }
}
