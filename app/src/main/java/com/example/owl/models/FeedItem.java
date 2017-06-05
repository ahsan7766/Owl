package com.example.owl.models;

/**
 * Created by Zach on 5/23/17.
 */

public class FeedItem {

    private String mHeader;
    private int mPhotoCount;
    private int mPhotoId;

    public FeedItem() {
    }

    public FeedItem(int photoId, String header, int photoCount) {
        mPhotoId = photoId;
        mHeader = header;
        mPhotoCount = photoCount;
    }

    public int getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(int photoId) {
        mPhotoId = photoId;
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
