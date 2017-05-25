package com.example.owl.models;

import java.util.Date;

/**
 * Created by Zach on 5/25/17.
 */

public class Comment {

    private Profile mProfile;
    private String mText;
    private Date mDate;


    public Comment () {}


    public Profile getProfile() {
        return mProfile;
    }

    public void setProfile(Profile profile) {
        mProfile = profile;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
