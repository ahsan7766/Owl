package com.ourwayoflife.owl.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zach on 5/23/17.
 */

public class FeedItem implements Parcelable{

    private String mPhotoId;
    private Bitmap mPhoto;
    private String mUserId;
    private Bitmap mUserPicture;
    private String mUserName;
    private Boolean mIsLiked;

    //private String mHeader;
    //private int mPhotoCount;

    public FeedItem() {
    }

    public FeedItem(String photoId, Bitmap photo, String userId, Bitmap userPicture, String userName, Boolean isLiked) { //, String header, int photoCount) {
        mPhotoId = photoId;
        mPhoto = photo;
        mUserId = userId;
        mUserPicture = userPicture;
        mUserName = userName;
        mIsLiked = isLiked;
        //mHeader = header;
        //mPhotoCount = photoCount;
    }

    //Constructor using parcelable
    protected FeedItem(Parcel in) {
        mPhotoId = in.readString();
        mPhoto = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        mUserId = in.readString();
        mUserPicture = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        mUserName = in.readString();
        mIsLiked = in.readInt() != 0;

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

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public Bitmap getUserPicture() {
        return mUserPicture;
    }

    public void setUserPicture(Bitmap userPicture) {
        mUserPicture = userPicture;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public Boolean getLiked() {
        return mIsLiked;
    }

    public void setLiked(Boolean liked) {
        mIsLiked = liked;
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
    public boolean equals(Object obj) {
        //return super.equals(obj);
        FeedItem feedItem = (FeedItem) obj;

        // TODO may have to add a few other select parameters like LikeCount and Deleted and anything else that could change between refreshes of a page
        return (feedItem.getPhotoId().equals(this.getPhotoId()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPhotoId);
        dest.writeValue(mPhoto);
        dest.writeString(mUserId);
        dest.writeValue(mUserPicture);
        dest.writeString(mUserName);
        dest.writeInt(mIsLiked ? 1 : 0);
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
