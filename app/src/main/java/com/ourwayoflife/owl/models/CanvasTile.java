package com.ourwayoflife.owl.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zach on 5/24/17.
 */

public class CanvasTile implements Parcelable{

    private String mStackId;
    private String mName;
    private Bitmap mPhoto;


    public CanvasTile() {

    }

    public CanvasTile(String stackId, String text, Bitmap photo) {
        this.mStackId = stackId;
        this.mName = text;
        this.mPhoto = photo;
    }

    //Constructor using parcelable
    protected CanvasTile(Parcel in) {
        mStackId = in.readString();
        mName = in.readString();
        mPhoto = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    }


    public String getStackId() {
        return mStackId;
    }

    public void setStackId(String stackId) {
        mStackId = stackId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public Bitmap getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Bitmap photo) {
        mPhoto = photo;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mStackId);
        dest.writeValue(mPhoto);
        dest.writeString(mName);
    }

    //@SuppressWarnings("unused")
    public static final Parcelable.Creator<CanvasTile> CREATOR = new Parcelable.Creator<CanvasTile>() {
        @Override
        public CanvasTile createFromParcel(Parcel in) {
            return new CanvasTile(in);
        }

        @Override
        public CanvasTile[] newArray(int size) {
            return new CanvasTile[size];
        }
    };

}
