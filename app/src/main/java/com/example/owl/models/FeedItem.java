package com.example.owl.models;

import android.graphics.Bitmap;

/**
 * Created by Zach on 5/23/17.
 */

public class FeedItem {

    private String mPhotoId;
    private Bitmap mPhoto;
    private String mHeader;
    private int mPhotoCount;

    public FeedItem() {
    }

    public FeedItem(String photoId, Bitmap photo, String header, int photoCount) {
        mPhotoId = photoId;
        mPhoto = photo;
        mHeader = header;
        mPhotoCount = photoCount;
    }

    public String getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(String photoId) {
        mPhotoId = photoId;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        mPhoto = photo;
    }

    public String getHeader() {
        return mHeader;
    }

    public void setHeader(String header) {
        mHeader = header;
    }

    public int getPhotoCount() {
        return mPhotoCount;
    }

    public void setPhotoCount(int photoCount) {
        mPhotoCount = photoCount;
    }
}
