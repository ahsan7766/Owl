package com.example.owl.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zach on 5/23/17.
 */

public class FeedItem implements Parcelable{

    private String mPhotoId;
    private Bitmap mPhoto;
    //private String mHeader;
    //private int mPhotoCount;

    public FeedItem() {
    }

    public FeedItem(String photoId, Bitmap photo) { //, String header, int photoCount) {
        mPhotoId = photoId;
        mPhoto = photo;
        //mHeader = header;
        //mPhotoCount = photoCount;
    }

    //Constructor using parcelable
    protected FeedItem(Parcel in) {
        mPhotoId = in.readString();
        mPhoto = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        //mHeader = in.readString();
        //mPhotoCount = in.readInt();
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

    /*
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

    */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPhotoId);
        dest.writeValue(mPhoto);
        //dest.writeString(mHeader);
        //dest.writeInt(mPhotoCount);
    }

    //@SuppressWarnings("unused")
    public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }

        @Override
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };

}
