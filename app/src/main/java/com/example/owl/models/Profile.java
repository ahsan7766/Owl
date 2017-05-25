package com.example.owl.models;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * Created by Zach on 5/25/17.
 */

public class Profile {

    private String mName;
    private String mBio;
    private Drawable mPhoto;
    private Color mColor;
    private int mHoots;
    private int mFollowers;
    private int mFollowing;



    public Profile () {}



    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getBio() {
        return mBio;
    }

    public void setBio(String bio) {
        mBio = bio;
    }

    public Drawable getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Drawable photo) {
        mPhoto = photo;
    }

    public Color getColor() {
        return mColor;
    }

    public void setColor(Color color) {
        mColor = color;
    }

    public int getHoots() {
        return mHoots;
    }

    public void setHoots(int hoots) {
        mHoots = hoots;
    }

    public int getFollowers() {
        return mFollowers;
    }

    public void setFollowers(int followers) {
        mFollowers = followers;
    }

    public int getFollowing() {
        return mFollowing;
    }

    public void setFollowing(int following) {
        mFollowing = following;
    }
}
